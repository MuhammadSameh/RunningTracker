package com.androiddevs.runningappyt.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.TrackingUtils
import com.androiddevs.runningappyt.others.Constants.ACTION_PAUSE
import com.androiddevs.runningappyt.others.Constants.ACTION_START_OR_RESUME
import com.androiddevs.runningappyt.others.Constants.ACTION_STOP
import com.androiddevs.runningappyt.others.Constants.NOTIFICATION_CHANNEL_ID
import com.androiddevs.runningappyt.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.androiddevs.runningappyt.others.Constants.NOTIFICATION_ID
import com.androiddevs.runningappyt.others.Constants.TIMER_UPDATE_INTERVAL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    //This service class will only be instantiated one time when we first start it by the intent we created
    //in the tracking fragment, but every time we try to start it again only the onStartcommand function of the
    //same instance will be invoked , so we need to know if it's the first time to start the service or it's
    //already running so, we will use this flag for this matter
    var isFirstRun = true

    //fusedLocation client gives us variable location functionalities and one of them is
    //requesting location updated this makes us able to live track user's location
    //we declare it then initialize it once the service is created (onCreate)
    // and remember that onCreate gets called only once
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //we need to create a notification builder and specify every detail about our notification in it (In the Service Module)

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    var isServiceKilled = false

    @Inject
    lateinit var currentNotificationBuildr: NotificationCompat.Builder


    var timeRunInSeconds = MutableLiveData<Long>()

    //This companion object is called once the service class is instantiated
    //we need those two livedata variables to observe when we need to track the user location
    // and add every point (when tracking) to the other live data object
    //and pointsPath is created as a live date not an Ordinary mutable list cause we need to draw a live track of the
    //user's route
    companion object {
        var timeRunInMillis = MutableLiveData<Long>()
        val pointsPath = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val isTracking = MutableLiveData<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()
        addInitialValues()
        //Toast.makeText(this, "Service onCreate $isFirstRun",Toast.LENGTH_SHORT).show()
        currentNotificationBuildr = baseNotificationBuilder
        //once the service is created (started for the first time) we need to attach this observer to the tracking
        //live data so, when the user starts the run we track every location update and save it, and
        //when he pauses the run or finish it we don't need to track it, and this functionality is implemented
        //in track location function that we created
        isTracking.observe(this, Observer {
            trackLocation(it)
            updateNotificationAction(it)
        })
    }

    //this function gets invoked automatically once the service started using an intent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //we need to check the action embedded in the  intent that started the service
        //to know which action we need to take

        intent?.let {
            when(intent.action){
                ACTION_START_OR_RESUME ->{
                    if (isFirstRun){
                        startForgroundService()
                        isTracking.postValue(true)
                        isFirstRun = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE -> {
                    isTracking.postValue(false)
                    isTimerEnabled = false
                }
                ACTION_STOP -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)


    }

    //this function only responsible for giving our livedata objects initial values so it cannot be null
    fun addInitialValues(){
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        pointsPath.postValue(mutableListOf())
        isTracking.postValue(false)
    }

    private fun killService(){
        //Toast.makeText(this, "In kill Service",Toast.LENGTH_SHORT).show()
          isServiceKilled = true
//        isFirstRun = true
        //we need to set it as false, cause when it's true the our service will continue working and it will be hard to interrupt it
        //from the first time we stop it, so need to stop updating everything by posting the value false
        isTracking.postValue(false)
        isTimerEnabled = false
        //we need to add initial value again before killing our service so the timer text returns to 0
        addInitialValues()
        stopForeground(true)
        stopSelf()
    }

    //every time we want to draw a path of the user's location we need to create an empty list of locations first
    //so we can add to it, this function is responsible for that matter
    fun addEmptyPolyLine(){
        pointsPath.value?.apply {
            add(mutableListOf())
            pointsPath.postValue(this)
        } ?: pointsPath.postValue(mutableListOf(mutableListOf()))
    }

    //We need to add every location update to the list of routes, and that's exactly what this function does
    fun addPoint(location: Location?){
        location?.let {
            val point = LatLng(location.latitude, location.longitude)
            pointsPath.value?.apply {
                last().add(point)
                pointsPath.postValue(this)
            }
        }
    }

    //this function is responsible for the live tracking of the user, and its functionality depends on the current state
    //of the isTracking livedate object so it only works when we need to track user's location
    @SuppressLint("MissingPermission")
    fun trackLocation(isTracking: Boolean){
        if (isTracking){
            if (TrackingUtils.hasLocationPermission(this)){
                val request = LocationRequest().apply {
                    interval = 5000L
                    fastestInterval = 2000L
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(request
                    ,locationCallback, Looper.getMainLooper())

            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyLine()

        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }



    //fusedLocationClient needs to know what to do with each location update, and the implementation of the
    //location callBack is responsible for this (we add each location update to the list of routes)
    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result?.locations?.let {
                    for (location in result.locations){
                        addPoint(location)
                    }
                }
            }
        }
    }

    private fun updateNotificationAction(isTracking: Boolean) {
        val actionText = if (isTracking) "Pause" else "Resume"

        val actionPendingIntent = if (isTracking){
            val intent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            PendingIntent.getService(this,1,intent, FLAG_UPDATE_CURRENT)
        } else {
            val intent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME
            }
            PendingIntent.getService(this,1,intent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuildr.javaClass.getDeclaredField("mActions").apply {

            isAccessible = true
            set(currentNotificationBuildr, ArrayList<NotificationCompat.Action>())
        }

        if (!isServiceKilled){
            currentNotificationBuildr = baseNotificationBuilder.addAction(R.drawable.ic_pause_black_24dp,actionText,actionPendingIntent)
            notificationManager.notify(NOTIFICATION_ID,currentNotificationBuildr.build())

        }

    }



    //This function is responsible for creating the notification then starting the service as a forgound service
    fun startForgroundService() {

        startTimer()

        isTracking.postValue(true)
        //We need to get an instance of the notification manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //then we need to check the sdk version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        //this function starts the service as a forground service and it takes the notification itself as a parameter
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this, Observer {
            if (!isServiceKilled){
                val notification = currentNotificationBuildr.setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }

        })
    }

    //If you want your notification to direct the user to a specific fragment or activity then
    //you need to create a Pending intent



    //Phones that run on android above Oreo needs to create something called notification Channel first
    //before actually creating the notification itself and this function is responsible for that matter
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager){

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW) //We need to make the importance level to low so that not every notification we send makes sound

        notificationManager.createNotificationChannel(channel)
    }
}