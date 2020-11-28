package com.vtsappsteam.labaccesscontrol.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vtsappsteam.labaccesscontrol.services.ConnectivityListener
import com.vtsappsteam.labaccesscontrol.utils.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
           val sp = context?.applicationContext?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            if (sp?.getString("doorPermission", "login") == "true") {
                val launchIntent = Intent(context, ConnectivityListener::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(launchIntent)
                } else {
                    context.startService(launchIntent)
                }
            }
        }
    }
}