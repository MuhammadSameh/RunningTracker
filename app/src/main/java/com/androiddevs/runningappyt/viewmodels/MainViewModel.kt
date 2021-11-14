package com.androiddevs.runningappyt.viewmodels

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.database.Run
import com.androiddevs.runningappyt.others.SortType
import com.androiddevs.runningappyt.repositories.RunningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Normally we would want to make a factory class so that we can pass parameters to our viewModel
 * But by using Hilt we can use @HiltViewModel annotation so hilt makes the factory class for us
 * and also we need to use inject annotation before the constructor to inject any parameter
 * only if that parameter Has a provider in the modules made by us
 *  **/
@HiltViewModel
class MainViewModel @Inject constructor(
    val repository: RunningRepository
    ): ViewModel(){

    private val runSortedByDate = repository.getRunsSortedByDate()
    private val runSortedBySpeed = repository.getRunsSortedByAvgSpeed()
    private val runSortedByCaloriesBurned = repository.getRunsSortedByCaloriesBurned()
    private val runSortedByDistance = repository.getRunsSortedByDistance()
    private val runSortedByTime = repository.getRunsSortedByTimesInMillis()

    //Instead of observing all of our livedata variables in our Run Fragment we can use MediatorLiveData
    //this Special Kind of liveData will observe all of them for us and we will provide a lambda function for it, so it knows how
    //to behave when any of these livedata change
    val run = MediatorLiveData<List<Run>>()

    //this variable is for monitoring when the user changes the sorting type, and we will set it initially to date
    var sortType = SortType.DATE

    //in this init block we will use addSource function on our mediator variable so it knows how to behave when
    //any of our livedata changes, and also to tell it, what livedata variables we want to observe
    init {
        run.addSource(runSortedByDate){
            if (sortType == SortType.DATE)
            it?.let {
                run.value = it
            }
        }
        run.addSource(runSortedBySpeed){
            if (sortType == SortType.AVG_SPEED)
            it?.let {
                run.value = it
            }
        }
        run.addSource(runSortedByCaloriesBurned){
            if (sortType == SortType.CALORIES_BURNED)
            it?.let {
                run.value = it
            }
        }
        run.addSource(runSortedByDistance){
            if (sortType == SortType.DISTANCE)
            it?.let {
                run.value = it
            }
        }
        run.addSource(runSortedByTime){
            if (sortType == SortType.RUNNING_TIME)
            it?.let {
                run.value = it
            }
        }

    }

    //this function is responsible for changing the value of the mediator liveData whenever the user changes
    //the sort type, so it appears in the corresponding order
    fun sortRuns (sortType: SortType){
        when(sortType){
            SortType.DATE -> {
                runSortedByDate.value?.let {
                    run.value = it
                }
            }
            SortType.RUNNING_TIME -> {
                runSortedByTime.value?.let {
                    run.value = it
                }
            }
            SortType.DISTANCE -> {
                runSortedByDistance.value?.let {
                    run.value = it
                }
            }
            SortType.CALORIES_BURNED -> {
                runSortedByCaloriesBurned.value?.let {
                    run.value = it
                }
            }
            SortType.AVG_SPEED -> {
                runSortedBySpeed.value?.let {
                    run.value = it
                }
            }

        }.also {
            this.sortType = sortType
        }
    }

        fun insertRun (run: Run) = viewModelScope.launch {
            repository.insertRun(run)
        }


}