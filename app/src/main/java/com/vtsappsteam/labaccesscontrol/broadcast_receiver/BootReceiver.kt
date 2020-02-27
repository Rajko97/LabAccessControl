package com.vtsappsteam.labaccesscontrol.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vtsappsteam.labaccesscontrol.services.utils.UnkillableServiceHelper
import com.vtsappsteam.labaccesscontrol.utils.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
           val sp = context?.applicationContext?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            if (sp?.getString("doorPermission", "login") == "true") {
                UnkillableServiceHelper.startMyService(context)
            }
        }
    }
}