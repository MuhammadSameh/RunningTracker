package com.androiddevs.runningappyt.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity (tableName = "running_table")
data class Run(
    var image: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var caloriesBurned: Int = 0,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L
){
    @PrimaryKey (autoGenerate = true)
    var id: Int? = null
}
