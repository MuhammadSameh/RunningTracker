package com.androiddevs.runningappyt.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunningDao {

    @Insert ( onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (run: Run)

    @Delete
    suspend fun delete(run: Run)

    @Query ("SELECT * FROM running_table ORDER BY  timestamp DESC")
    fun getRunsSortedByDate(): LiveData<List<Run>>

    @Query ("SELECT * FROM running_table ORDER BY  avgSpeedInKmh DESC")
    fun getRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query ("SELECT * FROM running_table ORDER BY  caloriesBurned DESC")
    fun getRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query ("SELECT * FROM running_table ORDER BY  distanceInMeters DESC")
    fun getRunsSortedByDistance(): LiveData<List<Run>>

    @Query ("SELECT * FROM running_table ORDER BY  timeInMillis DESC")
    fun getRunsSortedByTimesInMillis(): LiveData<List<Run>>


    @Query ("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>


    @Query ("SELECT SUM(avgSpeedInKmh) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>


    @Query ("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>


    @Query ("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTime(): LiveData<Long>



}