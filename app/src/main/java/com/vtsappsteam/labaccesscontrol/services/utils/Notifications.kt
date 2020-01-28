package com.vtsappsteam.labaccesscontrol.services.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.StartActivity

class Notifications {
    enum class NotificationChannels(val channelId: String, val channelName : Int, val descriptionText : Int, val importance : Int) {
        CHANNEL_MAC(
            "channelMac",
            R.string.notification_name_mac_change,
            R.string.notification_desc_mac_change,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    }

    enum class NotificationID(val notificationId: Int, val channelId: NotificationChannels) {
        NOTIF_MAC_APPROVED(0, NotificationChannels.CHANNEL_MAC),
        NOTIF_MAC_REJECTED(0, NotificationChannels.CHANNEL_MAC)
    }

    companion object {
        fun displayNotification(applicationContext : Context, id : NotificationID) {
            when(id) {
                NotificationID.NOTIF_MAC_APPROVED -> {
                    NotificationManagerCompat.from(applicationContext).notify(id.notificationId, (NotificationCompat.Builder(applicationContext,
                        id.channelId.channelId
                    ).apply {
                        setSmallIcon(R.mipmap.ic_launcher)
                        setContentTitle(applicationContext.getString(R.string.notification_title))
                        setContentText(applicationContext.getString(R.string.notification_device_approved))
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                        setContentIntent(PendingIntent.getActivity(applicationContext, 1, Intent(applicationContext, StartActivity::class.java).putExtra("isNotificationIntent", true), PendingIntent.FLAG_UPDATE_CURRENT))
                    }).build())
                }
                NotificationID.NOTIF_MAC_REJECTED -> TODO()
            }
        }

        fun cancelApprovedDeviceNotification(context: Context) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NotificationID.NOTIF_MAC_APPROVED.notificationId)
        }

        fun createNotificationChannels(applicationContext: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                enumValues< NotificationChannels>().forEach {
                    notificationManager.createNotificationChannel(NotificationChannel(it.channelId, applicationContext.getString(it.channelName), it.importance).apply {
                        description = applicationContext.getString(it.descriptionText)
                    })
                }
            }
        }
    }
}