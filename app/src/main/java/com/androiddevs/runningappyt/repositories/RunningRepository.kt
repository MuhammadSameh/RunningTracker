package com.androiddevs.runningappyt.repositories

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.androiddevs.runningappyt.database.Run
import com.androiddevs.runningappyt.database.RunningDatabase
import javax.inject.Inject

class RunningRepository @Inject constructor(
 val db: RunningDatabase
) {

    suspend fun insertRun(run: Run) = db.getDao().insert(run)

    suspend fun deleteRun(run: Run) = db.getDao().delete(run)


    fun getRunsSortedByDate() = db.getDao().getRunsSortedByDate()

    fun getRunsSortedByAvgSpeed() = db.getDao().getRunsSortedByAvgSpeed()

    fun getRunsSortedByCaloriesBurned() = db.getDao().getRunsSortedByCaloriesBurned()

    fun getRunsSortedByDistance() = db.getDao().getRunsSortedByDistance()

    fun getRunsSortedByTimesInMillis() = db.getDao().getRunsSortedByTimesInMillis()

    fun getTotalCaloriesBurned() = db.getDao().getTotalCaloriesBurned()

    fun getTotalAvgSpeed() = db.getDao().getTotalAvgSpeed()

    fun getTotalDistance() = db.getDao().getTotalDistance()

    fun getTotalTime() = db.getDao().getTotalTime()
}