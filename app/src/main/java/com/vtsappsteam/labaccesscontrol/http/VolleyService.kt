package com.vtsappsteam.labaccesscontrol.http

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.Constants
import org.json.JSONObject
import java.lang.Exception

class VolleyService(var responseCallback: Responsable, var context: Context) {
    fun post(url: String, params: JSONObject) {
        try {
            val que = Volley.newRequestQueue(context)
            val request = object : JsonObjectRequest(Method.POST, url, params,
                Response.Listener {
                    response -> responseCallback.successResponse(context, response)
                },
                Response.ErrorListener {
                    error -> run {
                        val message = when (error) {
                            is TimeoutError, is NetworkResponse, is NoConnectionError -> context.resources.getString(R.string.error_server_unavailable)
                            is OutOfMemoryError -> "Error: Out of Memory"
                            else -> "An unknown error occurred."
                        }
                        responseCallback.errorResponse(context, if (error.networkResponse != null) error.networkResponse.statusCode else 0, message)
                    }
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    val sp = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    val token = sp.getString("token", "")
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }
            que.add(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}