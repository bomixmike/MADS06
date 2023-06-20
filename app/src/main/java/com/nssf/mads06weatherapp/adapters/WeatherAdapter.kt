package com.nssf.mads06weatherapp.adapters

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nssf.mads06weatherapp.R
import com.nssf.mads06weatherapp.models.WeatherModel

class WeatherAdapter(private val mWeatherList: List<WeatherModel>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tv_todays_date = itemView.findViewById<TextView>(R.id.tv_todays_date)
        val tv_temperature = itemView.findViewById<TextView>(R.id.tv_temperature)
        val iv_weather_type = itemView.findViewById<ImageView>(R.id.iv_weather_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val weatherView = inflater.inflate(R.layout.forecast_item,parent,false)
        return ViewHolder(weatherView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weatherModel: WeatherModel = mWeatherList[position]
        Log.e("day",weatherModel.getDay() + mWeatherList[0].getDay())

        holder.tv_todays_date.text = weatherModel.getDay()

        holder.tv_temperature.text = Html.fromHtml(weatherModel.getTemperature().toString() + "&#176;")

        var type_of_weather = weatherModel.getWeather()
        if (type_of_weather.contains("Clouds")){
            holder.iv_weather_type.setImageResource(R.drawable.partlysunny_3x)
        }else
        if (type_of_weather.contains("Rain") || type_of_weather.contains("Drizzle")
            ||type_of_weather.contains("Thunderstorm")){
            holder.iv_weather_type.setImageResource(R.drawable.rain_3x)

        }else
        {
            holder.iv_weather_type.setImageResource(R.drawable.clear_3x)
        }

    }

    override fun getItemCount(): Int {
        return mWeatherList.size
    }

}