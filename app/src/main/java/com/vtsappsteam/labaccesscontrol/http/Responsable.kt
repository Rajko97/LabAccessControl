package com.vtsappsteam.labaccesscontrol.http

import org.json.JSONObject

interface Responsable {
    fun successResponse(res: JSONObject)
    fun errorResponse(statusCode : Int, message : String)
}