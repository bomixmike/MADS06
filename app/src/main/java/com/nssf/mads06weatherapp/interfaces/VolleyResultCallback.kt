package com.nssf.mads06weatherapp.interfaces

import com.android.volley.VolleyError
import org.json.JSONException
import org.json.JSONObject

interface VolleyResultCallback {

    @Throws(JSONException::class)
    fun onResultSuccess(jsonObject: JSONObject)

    @Throws(JSONException::class)
    fun onResultError(volleyError: VolleyError)
}