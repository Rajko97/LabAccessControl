package com.vtsappsteam.labaccesscontrol.http

import android.content.Context
import org.json.JSONObject

interface Responsable {
    fun successResponse(applicationContext: Context, res: JSONObject)
    fun errorResponse(applicationContext: Context, statusCode : Int, message : String)
}