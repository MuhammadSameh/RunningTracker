package com.androiddevs.runningappyt.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.androiddevs.runningappyt.database.RunningDatabase
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_FIRST_TIME_KEY
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_NAME
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_NAME_KEY
import com.androiddevs.runningappyt.others.Constants.SHARED_PREFERENCE_WEIGHT_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn (SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext app: Context) = Room.databaseBuilder(app
    , RunningDatabase::class.java,"running.db").build()

    @Provides
    @Singleton
    fun provideDao(db: RunningDatabase) = db.getDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideFirstTime(sharedPref:SharedPreferences) = sharedPref.getBoolean(
        SHARED_PREFERENCE_FIRST_TIME_KEY, true)


    @Provides
    @Singleton
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(SHARED_PREFERENCE_NAME_KEY, "") ?: ""


    @Provides
    @Singleton
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(
        SHARED_PREFERENCE_WEIGHT_KEY, 65f)

}