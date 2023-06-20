package com.nssf.mads06weatherapp.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class InstanceSingleton constructor(context: Context) {
    companion object{
        private var INSTANCE: InstanceSingleton? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this){
                INSTANCE ?: InstanceSingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(requestQ: Request<T>){
        requestQueue.add(requestQ)
    }
}