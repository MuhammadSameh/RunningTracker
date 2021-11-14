package com.androiddevs.runningappyt.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.androiddevs.runningappyt.repositories.RunningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: RunningRepository
    ): ViewModel(){
        val totalCaloriesBurned = repository.getTotalCaloriesBurned()
        val totalAvgSpeed = repository.getTotalAvgSpeed()
        val totalDistance = repository.getTotalDistance()
        val totalTime = repository.getTotalTime()

    val runsSortedByDate = repository.getRunsSortedByDate()

}