package com.vtsappsteam.labaccesscontrol.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vtsappsteam.labaccesscontrol.utils.Constants

class StartActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAccess()
    }

    private fun checkAccess() {
        sharedPreferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        when (sharedPreferences.getString("doorPermission", "login")) {
            "true" -> lunchActivity(MainActivity::class.java)
            "false" -> lunchActivity(WaitActiveActivity::class.java)
            else -> lunchActivity(LoginActivity::class.java)
        }
    }

    private fun lunchActivity(activity: Class<out AppCompatActivity>) {
        startActivity(Intent(this@StartActivity, activity))
        finish()
    }
}