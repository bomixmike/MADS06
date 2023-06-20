package com.nssf.mads06weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.nssf.mads06weatherapp.adapters.WeatherAdapter
import com.nssf.mads06weatherapp.api.Api
import com.nssf.mads06weatherapp.databinding.ActivityMainBinding
import com.nssf.mads06weatherapp.db.WeatherDatabase
import com.nssf.mads06weatherapp.db.dao.DaoWeather
import com.nssf.mads06weatherapp.db.entity.EntityWeather
import com.nssf.mads06weatherapp.interfaces.VolleyResultCallback
import com.nssf.mads06weatherapp.models.WeatherModel
import com.nssf.mads06weatherapp.utils.Network
import com.nssf.mads06weatherapp.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private lateinit var binding: ActivityMainBinding
    internal lateinit var dataWeatherList: ArrayList<WeatherModel>
    private var weatherAdapter: WeatherAdapter? = null
    private var weather: String? = null
    private var temp: Double? = null
    private var temp_min: Double? = null
    private var temp_max: Double? = null
    private var weather_location: String? = null
    private var progressDialog: ProgressDialog? = null
    private var isRotate: Boolean = false
    private lateinit var daoWeather: DaoWeather
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val dB = Room.databaseBuilder(this, WeatherDatabase::class.java,"weather_database")
            .build()
        daoWeather = dB.daoWeather()

        progressDialog = ProgressDialog(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentWeatherLocation()

        Utility.init(binding.fabAddFavorite)
        Utility.init(binding.fabViewFavorite)

        binding.fbWeatherData.setOnClickListener {
            isRotate = Utility.rotateFab(it,!isRotate)
            if(isRotate){
                Utility.showIn(binding.fabAddFavorite)
                Utility.showIn(binding.fabViewFavorite)
            }else{
                Utility.showOut(binding.fabViewFavorite)
                Utility.showOut(binding.fabAddFavorite)
            }
        }

        binding.fabAddFavorite.setOnClickListener{
            Toast.makeText(this,"Add Favorites Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.fabViewFavorite.setOnClickListener{
            Toast.makeText(this,"View Favorites Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertCurrentWeatherToDb(ent_main_temp: Double,ent_location: String,ent_weather: String,ent_weather_millis: Long,
                                         ent_min_temp: Double,ent_current_temp: Double,ent_max_temp: Double, ent_lat: Double,ent_lon: Double){
        lifecycleScope.launch(Dispatchers.IO){
            daoWeather.insertDbCurrentWeather(
                EntityWeather(0,ent_main_temp,ent_location,
                ent_weather,ent_weather_millis,ent_min_temp,ent_current_temp,ent_max_temp,ent_lat,
                ent_lon,System.currentTimeMillis())
            )
        }
    }

    private fun fetchCurrentWeatherToDb(){
        lifecycleScope.launch(Dispatchers.IO){
            if (daoWeather.getDbCurrentWeather().size != 0){
                daoWeather.getDbCurrentWeather()
                Log.e("Fetched data", daoWeather.getDbCurrentWeather()[0].weather)
                weather = daoWeather.getDbCurrentWeather()[0].weather
                temp = daoWeather.getDbCurrentWeather()[0].temperature
                temp_min = daoWeather.getDbCurrentWeather()[0].min_temp
                temp_max = daoWeather.getDbCurrentWeather()[0].max_temp
                weather_location = daoWeather.getDbCurrentWeather()[0].location
                var last_updated_on = daoWeather.getDbCurrentWeather()[0].weather_timestamp
                if(temp != null){
                    runOnUiThread {
                        setWeatherAssets()
                        binding.tvLastUpdatedOn.visibility = View.VISIBLE
                        binding.tvLastUpdatedOn.text =  "Last updated on " + humanReadableDate(last_updated_on)
                    }
                }else{
                    binding.mainContent.visibility = View.GONE
                    binding.incNoLocation.root.visibility = View.VISIBLE
                    binding.incNoLocation.tryAgain.setOnClickListener{
                        recreate()
                    }
                }

            }
        }
    }

    private fun countDown(){
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.incNoLocation.tryAgain.text = "Restarting in ${(millisUntilFinished / 1000)}.."
            }
            override fun onFinish() {
                restartAppCompletely()
            }
        }.start()
    }

    private fun restartAppCompletely(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
        this.finish()
        Runtime.getRuntime().exit(0)
    }

    private fun getCurrentWeatherLocation(){
        doProgressDialog(1)
        if(checkLocPermission()){
            if(locIsEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocPermission()
                    return
                }

                locationRequest = LocationRequest.create().apply {
                    interval = TimeUnit.SECONDS.toMillis(60)
                    fastestInterval = TimeUnit.SECONDS.toMillis(60)
                    maxWaitTime = TimeUnit.MINUTES.toMillis(2)
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                locationCallback = object : LocationCallback(){
                    override fun onLocationResult(p0: LocationResult) {
                        super.onLocationResult(p0)
                        currentLocation = p0.lastLocation
                    }
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {

//                    Toast.makeText(this,"$currentLocation",Toast.LENGTH_SHORT).show()
                    Log.e("LocationResult","$currentLocation")
                    if (it == null){
                        Toast.makeText(this,"Something peculiar happened, retrying...",Toast.LENGTH_SHORT).show()
                        binding.mainContent.visibility = View.GONE
                        binding.incNoLocation.root.visibility = View.VISIBLE
                        doProgressDialog(0)
                        binding.incNoLocation.tryAgain.setOnClickListener{
                            countDown()
                        }
                    }else{
                        binding.mainContent.visibility = View.VISIBLE
                        binding.incNoLocation.root.visibility = View.GONE
//                        Toast.makeText(this,"Latitude= ${it?.latitude}  Longitude= ${it?.longitude}",Toast.LENGTH_LONG).show()

                        if(!Network.checkConnectivity(this)){
                            doProgressDialog(0)
                            fetchCurrentWeatherToDb()

                        }else{
                            getForecastWeather(it.latitude,it.longitude)
                            getCurrentWeather(it.latitude,it.longitude)
                        }
                    }
                }

            }else{
                //user didnt allow so open setting manually
                Toast.makeText(this,"Please Turn on Your Location, to use App",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            //request weather loc permission
            requestLocPermission()
        }
    }

    private fun locIsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestLocPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_LOC)
    }

    companion object{
        const val PERMISSION_REQUEST_ACCESS_LOC = 92
    }

    private fun getCurrentWeather(lat:Double,lon:Double){
        Utility.volleyRequest("${Api.current_weather}lat=$lat&lon=$lon&appid=b03ac709c411a1f27fa05ff3f3f0743e&units=metric",
            Request.Method.GET,this, object : VolleyResultCallback {
            override fun onResultSuccess(jsonObject: JSONObject) {
                val weatherObject = jsonObject.getJSONArray("weather")
                for (i in 0 until  weatherObject.length()){
                    val innerWeatherObject = weatherObject.getJSONObject(i)
                    weather = innerWeatherObject.getString("main")
                }

                weather_location = jsonObject.getString("name")
                val mainTemp = jsonObject.getJSONObject("main")
                temp = mainTemp.getDouble("temp")
                temp_min = mainTemp.getDouble("temp_min")
                temp_max = mainTemp.getDouble("temp_max")
                val weather_millis = jsonObject.getLong("dt")
                if (weather != null && temp != null){
                    setWeatherAssets()
                    insertCurrentWeatherToDb(
                        temp!!,weather_location!!,weather!!,weather_millis, temp_min!!,
                        temp!!, temp_max!!,lat,lon)
                }

            }
            override fun onResultError(volleyError: VolleyError) {
                binding.mainContent.visibility = View.GONE
                binding.incNoLocation.root.visibility = View.VISIBLE
                doProgressDialog(0)
                binding.incNoLocation.tryAgain.setOnClickListener{
                    recreate()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setWeatherAssets(){
        binding.tvCurrentTemp.text = temp.toString()+ Html.fromHtml("&#176;")
        binding.tvMainTemp.text = temp.toString()+ Html.fromHtml("&#176;")
        binding.tvMainWeather.text = weather+ Html.fromHtml("&#176;")
        binding.tvMaxTemp.text = temp_max.toString()+ Html.fromHtml("&#176;")
        binding.tvMinTemp.text = temp_min.toString() + Html.fromHtml("&#176;")
        binding.tvLocation.text = weather_location
        if (weather?.contains("Clouds") == true){
            binding.root.setBackgroundColor(this.resources.getColor(R.color.cloudy))
            binding.ivWeather.setImageResource(R.drawable.forest_cloudy)
        }else
            if (weather?.contains("Thunderstorm") == true||
                weather?.contains("Rain") == true ||
                weather?.contains("Drizzle") == true){
                binding.root.setBackgroundColor(this.resources.getColor(R.color.rainy))
                binding.ivWeather.setImageResource(R.drawable.forest_cloudy)

            }else{
                binding.root.setBackgroundColor(this.resources.getColor(R.color.sunny))
                binding.ivWeather.setImageResource(R.drawable.forest_sunny)
            }

    }

    private fun getForecastWeather(lat:Double,lon:Double){
        Utility.volleyRequest("${Api.forecast_weather}lat=$lat&lon=$lon&appid=b03ac709c411a1f27fa05ff3f3f0743e&units=metric",
            Request.Method.GET,this, object : VolleyResultCallback{
            override fun onResultSuccess(jsonObject: JSONObject) {
                dataWeatherList = arrayListOf()
                val jsonArray = jsonObject.getJSONArray("list")
                Log.e("Weather List", "${jsonArray.length()}>> onResultSuccess: $jsonArray" )

                for (i in 0 until jsonArray.length()){
                    Log.e("Weather Array Items","${jsonArray[i]}")
                    val weatherDataObjects = jsonArray.getJSONObject(i)
                    val weatherModel = WeatherModel()

                    weatherModel.setDay(humanReadableDate(weatherDataObjects.getLong("dt")))

                    val mainWeatherDataObject = weatherDataObjects.getJSONObject("main")
                    weatherModel.setTemperature(mainWeatherDataObject.getDouble("temp"))

                    val weatherTypeArray = weatherDataObjects.getJSONArray("weather")
                    for (j in 0 until weatherTypeArray.length()){
                        val weatherTypeObjects = weatherTypeArray.getJSONObject(j)
                        weatherModel.setWeather(weatherTypeObjects.getString("main"))
                    }

                    dataWeatherList.add(weatherModel)
                }

                populateRecyclerView()

            }
            override fun onResultError(volleyError: VolleyError) {
                binding.mainContent.visibility = View.GONE
                binding.incNoLocation.root.visibility = View.VISIBLE
                doProgressDialog(0)
                binding.incNoLocation.tryAgain.setOnClickListener{
                    recreate()
                }
            }
        })
    }

    private fun doProgressDialog(int: Int){
        progressDialog?.setTitle("Loading Weather data")
        progressDialog?.setMessage("just a moment... as we are prepare your data.")
        progressDialog?.setCancelable(false)
        if (int == 1) {
            progressDialog?.show()
        }else{
            if(progressDialog?.isShowing == true){
                progressDialog?.dismiss()
            }

        }
    }

    private fun clearListFromDuplicateDays(list1: List<WeatherModel>): List<WeatherModel?>? {
        val noDuplicateDays: MutableMap<String, WeatherModel> =
            LinkedHashMap<String, WeatherModel>()
        for (i in list1.indices) {
            noDuplicateDays[list1[i].getDay()] = list1[i]
        }
        return ArrayList<WeatherModel>(noDuplicateDays.values)
    }

    private fun humanReadableDate(date_in: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd")
        val netDate = Date((date_in) * 1000)
        return sdf.format(netDate)
    }

    private fun populateRecyclerView(){
        doProgressDialog(0)
        val no_duplicatesList = clearListFromDuplicateDays(dataWeatherList) as List<WeatherModel>
        val weather_sublist = no_duplicatesList.subList(1,no_duplicatesList.size)
        weatherAdapter = WeatherAdapter(weather_sublist)
        binding.rvForecastDays.adapter = weatherAdapter
        binding.rvForecastDays.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOC){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                getCurrentWeatherLocation()//get location once more
            }else{
                Toast.makeText(this,"Permission Denied, However DVT Weather needs this to work Properly",Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentWeatherLocation()
    }
}