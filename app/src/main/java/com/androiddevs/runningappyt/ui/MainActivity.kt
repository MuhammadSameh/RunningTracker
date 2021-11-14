package com.androiddevs.runningappyt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.Constants
import com.androiddevs.runningappyt.others.Constants.ACTION_TO_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //We need to check when the activity is created if we directed to the activity
        // by the pending intent of the Notification

        navigateTrackingFragmentByIntent(intent)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(nav_host_fragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /*NO-OP*/ }


        nav_host_fragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.settings_fragment, R.id.run_fragment, R.id.statistics_fragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }

    //We might be directed to the activity again by the notification but the activity is not destroyed
    //In this case onNewIntent function will be invoked
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateTrackingFragmentByIntent(intent)
    }

    //This function checks if the intent is the Pending intent of the notification and if that's true
    //then we will navigate to the tracking activity
    //And if the intent is null that means we weren't directed to the activity by the notification
    fun navigateTrackingFragmentByIntent(intent: Intent?) {
        if (intent?.action == ACTION_TO_TRACKING_FRAGMENT ){
            nav_host_fragment.findNavController().navigate(R.id.global_tracking_fragment)
        }
    }
}
