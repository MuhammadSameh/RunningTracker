package com.androiddevs.runningappyt.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.TrackingUtils
import com.androiddevs.runningappyt.database.Run
import com.androiddevs.runningappyt.others.Constants.ACTION_PAUSE
import com.androiddevs.runningappyt.others.Constants.ACTION_START_OR_RESUME
import com.androiddevs.runningappyt.others.Constants.ACTION_STOP
import com.androiddevs.runningappyt.others.Constants.COLOR_RED
import com.androiddevs.runningappyt.services.TrackingService
import com.androiddevs.runningappyt.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    //Initializing viewModel
    private val viewModel: MainViewModel by viewModels()

    private var menu: Menu? = null

    private var pathPoints = mutableListOf<MutableList<LatLng>>()
    private var isTracking = false

    private var timeInMillies = 0L

    @set:Inject
    var weight = 65f

    //This is the map itself not the mapView and we need to set it to null initially
    private var map: GoogleMap? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        //mapView is just a view that has its own Lifecycle so we need to handle it
        mapView.onCreate(savedInstanceState)

        //here we get the map from the mapView and save it in our map instance so we can make changes on it
        mapView.getMapAsync {
            map = it
            drawAllPoints()
        }

        btnToggleRun.setOnClickListener{
            toggleRun(isTracking)
        }


        btnFinishRun.setOnClickListener {
            zoomOutToWholeTrack()
            addToDp()
        }

        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            this.isTracking = it
            handlingButtons(isTracking)
        })

        TrackingService.pointsPath.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            drawLatestTwoPoints()
            handleCameraZoom()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            timeInMillies = it
            val timeString = TrackingUtils.getFormattedStopWatchTime(timeInMillies,true)
            tvTimer.text = timeString
        })


    }

    //This function is overridden when the fragment should inflate a view (Menu in our case)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    //This function is responsible for zooming out the camera so we can screenShot the whole track
    private fun zoomOutToWholeTrack(){
        val bounds = LatLngBounds.builder()
        for (polyLine in pathPoints){
            for (pos in polyLine){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height *0.05f).toInt()
            )
        )
    }

    private fun addToDp(){
        map?.snapshot { bitMap ->
            var distanceInMeter = 0
            for (polyLine in pathPoints){
                distanceInMeter += TrackingUtils.calculatePolyDistance(polyLine).toInt()
            }

            val avgSpeed = round((distanceInMeter / 1000f) / (timeInMillies/1000f / 60 /60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis

            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()
            val run = Run(bitMap,dateTimeStamp,avgSpeed,caloriesBurned,distanceInMeter,timeInMillies)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            sendCommandtoService(ACTION_STOP)
            nav_host_fragment.findNavController().navigate(R.id.action_tracking_fragment_to_run_fragment)
        }

    }

    //this function is overridden after setting setHasOptionsMenu(true) to actually inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tracking_menu,menu)
        //after inflating the menu we need to save it in our menu variable to make changes to it
        this.menu = menu
    }


    //this function is overridden when you need to dynamically modify menu content
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (timeInMillies > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }

    }

    private fun toggleRun(isTracking: Boolean){
        if (isTracking){
            sendCommandtoService(ACTION_PAUSE)
        } else {
            menu?.getItem(0)?.isVisible = true
            sendCommandtoService(ACTION_START_OR_RESUME)
        }
    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Discard Run?")
            .setMessage("Are you sure you want to cancel this Run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                tvTimer.text = "00:00:00:00"
                sendCommandtoService(ACTION_STOP)
                nav_host_fragment.findNavController().navigate(R.id.action_tracking_fragment_to_run_fragment)
            }
            .setNegativeButton("No") {dialogInterface ,_ ->
                dialogInterface.cancel()

            }
            .create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == menu?.getItem(0)?.itemId){
            showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun handleCameraZoom(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    15f
                )
            )
        }
    }

    private fun handlingButtons(isTracking: Boolean) {
        if (!isTracking && timeInMillies > 0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
            menu?.getItem(0)?.isVisible =true
        }
    }

    private fun drawAllPoints(){
        if (pathPoints.isNotEmpty()) {
            for (polyLine in pathPoints){
                val polyOption = PolylineOptions()
                    .color(COLOR_RED)
                    .width(8f)
                    .addAll(polyLine)

                map?.addPolyline(polyOption)
            }
        }
    }

    private fun drawLatestTwoPoints(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastpoint = pathPoints.last()[pathPoints.last().size - 2]
            val lastPoint = pathPoints.last().last()

            val polyOption = PolylineOptions()
                .color(COLOR_RED)
                .width(8f)
                .add(preLastpoint,lastPoint)

            map?.addPolyline(polyOption)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    fun sendCommandtoService(action: String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
}