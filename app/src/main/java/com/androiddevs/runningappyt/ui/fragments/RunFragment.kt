package com.androiddevs.runningappyt.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.TrackingUtils
import com.androiddevs.runningappyt.adapters.RunAdapter
import com.androiddevs.runningappyt.others.SortType
import com.androiddevs.runningappyt.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    //Initializing viewModel by dagger
    private val viewModel: MainViewModel by viewModels()

    lateinit var runAdapter: RunAdapter

    @set: Inject
    var name: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        fab.setOnClickListener {
            nav_host_fragment.findNavController().navigate(R.id.action_run_fragment_to_tracking_fragment)
        }
        setupRecyclerView()

        val toolbarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText


        when(viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }


        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        //we need to observe the mediator livedata object, so when its value changes, we submit the new list to our adapter
        viewModel.run.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
    }

    //We use this function to request the permission from the user
    fun requestPermissions(){
        if (TrackingUtils.hasLocationPermission(requireContext()))
            return
        else {
            //We need to check if the sdk version is below Q or not
                // cause below Q we don't need background location permission
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this, "This app Needs to use your locations", 0,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                EasyPermissions.requestPermissions(
                    this, "This app Needs to use your locations", 0,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    //Those functions need to be implemented to specify the action we want to make
    // depending of the permission result (Denied or Granted)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            // here we will direct the user to the app settings screen if the permission is denied permanently
                //This behavior is implemented in the EasyPermissions Library and that's why we use it
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    //This the default function that gets invoked after the user denies or grant the permission and it stores the
    //result of all the permission (Every set of permissions have their own code that we should specify)
    override fun onRequestPermissionsResult(
        //This the code that we specified when asked for user permission
        requestCode: Int,
        //We might have asked the user for one or more permission, all of the permission are stored in this array
        permissions: Array<out String>,
        //These are the results of the permissions
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(0,permissions,grantResults,this)
    }

    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }


}