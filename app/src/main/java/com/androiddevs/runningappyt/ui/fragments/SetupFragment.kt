package com.androiddevs.runningappyt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.Constants
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_FIRST_TIME_KEY
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_NAME
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_NAME_KEY
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_WEIGHT_KEY
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreference: SharedPreferences

    @set:Inject
    var isFirstTime:Boolean = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstTime){

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setup_fragment, true).build()

            nav_host_fragment.findNavController().navigate(R.id.action_setup_fragment_to_run_fragment
                ,savedInstanceState
            ,navOptions)

        }

        tvContinue.setOnClickListener {
            if (addDetailsToSharedPreference()){

            nav_host_fragment.findNavController().navigate(R.id.action_setup_fragment_to_run_fragment)
            } else {

                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    fun addDetailsToSharedPreference():Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreference.edit()
            .putString(SHARED_PREFERENCE_NAME_KEY,name)
            .putFloat(SHARED_PREFERENCE_WEIGHT_KEY,weight.toFloat())
            .putBoolean(SHARED_PREFERENCE_FIRST_TIME_KEY, false)
            .apply()

        val toolbarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}