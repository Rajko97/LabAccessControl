package com.vtsappsteam.labaccesscontrol.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.utils.Constants

class StartActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAccess()
    }

    private fun checkAccess() {
        sharedPreferences = applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        when (sharedPreferences.getString("doorPermission", "login")) {
            "true" -> {
                FirebaseMessagingService.enableFCM()
                lunchActivity(MainActivity::class.java)
            }
            "false" -> {
                FirebaseMessagingService.enableFCM()
                lunchActivity(WaitActiveActivity::class.java)
            }
            else -> lunchActivity(LoginActivity::class.java)
        }
    }

    private fun lunchActivity(activity: Class<out AppCompatActivity>) {
        startActivity(Intent(this@StartActivity, activity).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}