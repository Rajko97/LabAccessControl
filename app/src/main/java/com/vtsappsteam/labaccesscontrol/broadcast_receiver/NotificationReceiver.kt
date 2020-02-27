package com.vtsappsteam.labaccesscontrol.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vtsappsteam.labaccesscontrol.http.SocketIO
import com.vtsappsteam.labaccesscontrol.utils.Constants

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val token = context?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)?.getString("uniqueToken", "") as String
        SocketIO.unlock(token)
    }
}