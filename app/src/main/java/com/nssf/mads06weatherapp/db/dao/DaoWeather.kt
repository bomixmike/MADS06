package com.nssf.mads06weatherapp.db.dao

import androidx.room.*
import com.nssf.mads06weatherapp.db.entity.EntityWeather

@Dao
interface DaoWeather {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDbCurrentWeather(weather: EntityWeather)

    @Query("select * from current_weather_table")
    fun getDbCurrentWeather(): List<EntityWeather>

    @Update
    suspend fun updateDbCurrentWeather(weather: EntityWeather)

    @Delete
    suspend fun deleteDbCurrentWeather(weather: EntityWeather)

}