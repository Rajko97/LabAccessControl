package com.vtsappsteam.labaccesscontrol

import android.app.Application
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications.Companion.createNotificationChannels

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(applicationContext)
    }
}