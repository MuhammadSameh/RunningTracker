package com.androiddevs.runningappyt.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.readPendingIntentOrNullFromParcel
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.Constants
import com.androiddevs.runningappyt.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn (ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun provideFusedLocationClientProvider(@ApplicationContext app: Context) = FusedLocationProviderClient(app)

    @Provides
    @ServiceScoped
    fun providePendingIntent(@ApplicationContext app: Context) =
    //Pending Intent is just like the normal intent but it needs some extra information cause it will be used
    //from outside the application, and we use pending intent with our notification so that when the user clicks on the
        // notification we can direct him to a specific activity
    PendingIntent.getActivity(app,0
    , Intent(app, MainActivity::class.java).also {
        it.action = Constants.ACTION_TO_TRACKING_FRAGMENT
    },
    FLAG_UPDATE_CURRENT)

    @Provides
    @ServiceScoped
    fun provideNotificationBuilder(@ApplicationContext app: Context, intent: PendingIntent) = NotificationCompat.Builder(app,
        Constants.NOTIFICATION_CHANNEL_ID
    )
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setAutoCancel(false) //So the notification doesn't cancel after pressing on it
        .setOngoing(true) // So the notification doesn't cancel by swiping
        .setContentTitle("Running App")
        .setContentText("00:00:00")
        .setContentIntent(intent)
}