package com.nssf.mads06weatherapp.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import com.android.volley.toolbox.JsonObjectRequest
import com.nssf.mads06weatherapp.interfaces.VolleyResultCallback
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class Utility {
    companion object{
        val sharedPreferenceFile = "WeatherAppPref"
        lateinit var sharedPrefs: SharedPreferences

        fun setOnboardingPreferences(activity: Activity, onBoarded: Boolean){
            sharedPrefs = activity.getSharedPreferences(sharedPreferenceFile,Context.MODE_PRIVATE)
            val sharedPrefsEditor: SharedPreferences.Editor = sharedPrefs.edit()
            sharedPrefsEditor.putBoolean("isOnBoardingCompleted",onBoarded)
            sharedPrefsEditor.apply()
        }

        fun getOnboardingComplete(activity: Activity) : Boolean{
            sharedPrefs = activity.getSharedPreferences(sharedPreferenceFile,Context.MODE_PRIVATE)
            return sharedPrefs.getBoolean("isOnBoardingCompleted",false)
        }

        fun volleyRequest(url: String, requestMode: Int, activity: Activity, volleyResultCallback: VolleyResultCallback){
            val requestQueue = InstanceSingleton.getInstance(activity.applicationContext).requestQueue
            val jsonObjectReq = JsonObjectRequest(requestMode,url,null, {
                Log.e("VolleySuccess", it.toString())
                 volleyResultCallback.onResultSuccess(it)
            }, {
                Log.e("VolleyError",it.toString())
                volleyResultCallback.onResultError(it)
            })
            requestQueue.add(jsonObjectReq)
        }

        fun rotateFab(v: View, rotate: Boolean): Boolean{
            v.animate().setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                        if (animation != null) {
                            super.onAnimationEnd(animation)
                        }
                    }
                })
                .rotation(if (rotate) 135f else 0f)
            return rotate
        }

        fun showIn(v: View) {
            v.visibility = View.VISIBLE
            v.alpha = 0f
            v.translationY = v.height.toFloat()
            v.animate()
                .setDuration(200)
                .translationY(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
                .alpha(1f)
                .start()
        }

        fun showOut(v: View) {
            v.visibility = View.VISIBLE
            v.alpha = 1f
            v.translationY = 0f
            v.animate()
                .setDuration(200)
                .translationY(v.height.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        v.visibility = View.GONE
                        super.onAnimationEnd(animation)
                    }
                }).alpha(0f)
                .start()
        }

        fun init(v: View) {
            v.visibility = View.GONE
            v.translationY = v.height.toFloat()
            v.alpha = 0f
        }

        fun isConnectingToInternet(context: Context): Boolean {
            return if (networkConnectivity(context)) {
                try {
                    val urlc: HttpURLConnection = URL(
                        "http://www.google.com"
                    ).openConnection() as HttpURLConnection
                    urlc.setRequestProperty("User-Agent", "Test")
                    urlc.setRequestProperty("Connection", "close")
                    urlc.setConnectTimeout(3000)
                    urlc.setReadTimeout(4000)
                    urlc.connect()
                    urlc.getResponseCode() === 200
                } catch (e: IOException) {
                    false
                }
            } else false
        }

        private fun networkConnectivity(context: Context): Boolean {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    }
}