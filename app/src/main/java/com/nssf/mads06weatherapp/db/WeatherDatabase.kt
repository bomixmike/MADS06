package com.nssf.mads06weatherapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nssf.mads06weatherapp.db.dao.DaoForecast
import com.nssf.mads06weatherapp.db.dao.DaoWeather
import com.nssf.mads06weatherapp.db.entity.EntityWeather

@Database(entities = [EntityWeather::class], version = 1)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun daoWeather(): DaoWeather
    abstract fun daoForecast(): DaoForecast
}