package com.vtsappsteam.labaccesscontrol

import android.content.Context
import com.vtsappsteam.labaccesscontrol.utils.Constants

object SessionManager {

    data class User(
        var rank : String,
        var username : String,
        var firstName : String,
        var lastName : String) {

        fun getFullName() : String {return "$firstName $lastName"}
    }

    var currentUser : User? = null
    var authToken : String? = null

    fun userLoggedIn(user : User, token : String) {
        currentUser = user
        authToken = token
    }

    //returns succ or failed
    fun retreiveSession(context: Context) : Boolean {
        val data = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        if(data.getString("doorPermission", null) == null)
            return false
        val userdata = data.getString("user", null)
        return true
    }

    fun saveSession(context: Context) {
        val data = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit()
        data.apply()
    }
}