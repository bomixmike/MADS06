package com.nssf.mads06weatherapp.models

class WeatherModel {
    private var day:String? = null
    private var weather:String? = null
    private var temperature: Double = 0.0

    fun getDay():String{
        return day.toString()
    }

    fun setDay(day: String){
        this.day = day
    }

    fun getWeather(): String{
        return weather.toString()
    }

    fun setWeather(weather: String){
        this.weather = weather
    }

    fun getTemperature(): Double {
        return temperature
    }

    fun setTemperature(temperature: Double){
        this.temperature = temperature
    }
}