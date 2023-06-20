package com.nssf.mads06weatherapp.db.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

class EntityForecast(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var temperature: Double,
    var location: String,
    var weather: String,
    var weather_timestamp: Long,
    var min_temp: Double,
    var current_temp: Double,
    var max_temp: Double,
    @ColumnInfo(name = "latitude_location")
    var lat_loc: Double,
    @ColumnInfo(name = "longitude_location")
    var lon_loc: Double,
    var time_at: Long,
    var favorite: Int
)