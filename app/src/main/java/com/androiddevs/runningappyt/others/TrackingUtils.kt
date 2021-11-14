package com.androiddevs.runningappyt.others

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK
import com.google.android.gms.maps.model.LatLng
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

//We will use Tracking Utils in our Run Fragment to check if the permission is granted or denied
object TrackingUtils {

    // We need this function just to know if our permission is granted or not and it returns a boolean
    // And we are using EasyPermissions library cause it makes it easier to direct the user to app settings
    //if he denied the location permission
    fun hasLocationPermission(context: Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        } else {
            EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }


    //We use this function to format the timer in our stopWatch, it takes two parameters the time In milliseconds and
    //a boolean, if we pass false this means that we don't want to include milliseconds in the returned Format
    //(We use this in our notification cause we don't want to include too much information in our notification)
    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }

    //This function is used to calculate the distance using polyLines drawn on the map
    fun calculatePolyDistance(polyLine: MutableList<LatLng>): Float {
        var distance = 0f
        for (i in 0..polyLine.size-2){
            val pos1 = polyLine[i]
            val pos2 = polyLine[i+1]
            val resulArray = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,pos1.longitude,
                pos2.latitude,pos2.longitude,
                resulArray
            )
            distance += resulArray[0]
        }
        return distance
    }

}