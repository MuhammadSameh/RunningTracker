package com.androiddevs.runningappyt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.others.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.etName
import kotlinx.android.synthetic.main.fragment_settings.etWeight

import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @set:Inject
    var name = ""

    @set:Inject
    var weight = 65f

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSavedDetails()

        btnApplyChanges.setOnClickListener {
            val success = applyChanges()
            if (success)
                Snackbar.make(requireView(),"Changes Saved!", Snackbar.LENGTH_SHORT).show()
            else
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun showSavedDetails(){
        etName.setText(name)
        etWeight.setText(weight.toString())
        setToolbar(name)
    }

    private fun setToolbar(name:String) {
        val toolbarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText
    }

    fun applyChanges(): Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(Constants.SHARED_PREFERENCE_NAME_KEY,name)
            .putFloat(Constants.SHARED_PREFERENCE_WEIGHT_KEY,weight.toFloat())
            .apply()

        setToolbar(name)
        return true

    }

}