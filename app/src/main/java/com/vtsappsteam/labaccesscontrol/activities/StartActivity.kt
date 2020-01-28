package com.vtsappsteam.labaccesscontrol.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
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
        startActivity(Intent(this@StartActivity, activity).clearStack(isNotificationIntent()))
        finish()
    }

    private fun isNotificationIntent() : Boolean {
        return intent.getBooleanExtra("isNotificationIntent", false)
    }

    private fun Intent.clearStack(shouldClear : Boolean) : Intent {
        if (shouldClear)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return this
    }
}