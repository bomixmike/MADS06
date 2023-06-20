package com.nssf.mads06weatherapp.db.dao

import androidx.room.*
import com.nssf.mads06weatherapp.db.entity.EntityWeather

@Dao
interface DaoForecast {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDbForecastWeather(weather: EntityWeather)

    @Query("select * from current_weather_table")
    fun getDbForecastWeather(): List<EntityWeather>

    @Update
    suspend fun updateDbForecastWeather(weather: EntityWeather)

    @Delete
    suspend fun deleteDbForecastWeather(weather: EntityWeather)

}