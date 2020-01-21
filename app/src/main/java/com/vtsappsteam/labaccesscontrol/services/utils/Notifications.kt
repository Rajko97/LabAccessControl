package com.vtsappsteam.labaccesscontrol.services.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.StartActivity

class Notifications {
    companion object {
        private const val CHANNEL_ID = "channelRTS1"
        private const val NOTIFICATION_ID = 1

        fun displayNotification(applicationContext : Context) {
            createNotificationChannel(applicationContext)

            val resultIntent = Intent(applicationContext, StartActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(applicationContext, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setContentTitle(applicationContext.getString(R.string.notification_title))
                setContentText(applicationContext.getString(R.string.notification_device_approved))
                priority = NotificationCompat.PRIORITY_HIGH
                setAutoCancel(true)
                setContentIntent(pendingIntent)

            }

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }

        private fun createNotificationChannel(applicationContext: Context) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "MAC Change requests"
                val descriptionText ="Obavesti me kada administrator odobri moj uredjaj"
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}