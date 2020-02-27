package com.vtsappsteam.labaccesscontrol.services.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.StartActivity

class Notifications {
    enum class NotificationChannels(val channelId: String, val channelName : Int, val descriptionText : Int, val importance : Int) {
        CHANNEL_MAC(
            "channelMac",
            R.string.notification_name_mac_change,
            R.string.notification_desc_mac_change,
            NotificationManager.IMPORTANCE_DEFAULT
        ),
        CHANNEL_CONTROL(
            "channelControl",
            R.string.notification_name_lab_control,
            R.string.notification_desc_lab_control,
            NotificationManager.IMPORTANCE_MAX
        ),
        CHANNEL_HIDDEN(
            "channelHidden",
            R.string.notification_name_hidden,
            R.string.notification_desc_hidden,
            NotificationManager.IMPORTANCE_MIN
        )

    }

    enum class NotificationID(val notificationId: Int, val channelId: NotificationChannels) {
        NOTIF_MAC_APPROVED(0, NotificationChannels.CHANNEL_MAC),
        NOTIF_MAC_REJECTED(0, NotificationChannels.CHANNEL_MAC),
    }

    companion object {
        fun displayNotification(applicationContext : Context, id : NotificationID) {
            when(id) {
                NotificationID.NOTIF_MAC_APPROVED -> {
                    NotificationManagerCompat.from(applicationContext).notify(id.notificationId, (NotificationCompat.Builder(applicationContext,
                        id.channelId.channelId
                    ).apply {
                        setSmallIcon(R.drawable.ic_notification_icon)
                        setContentTitle(applicationContext.getString(R.string.notification_title))
                        setContentText(applicationContext.getString(R.string.notification_device_approved))
                        color = ResourcesCompat.getColor(mContext.resources, R.color.themeTextBlue, null)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                        setContentIntent(PendingIntent.getActivity(applicationContext, 1, Intent(applicationContext, StartActivity::class.java).putExtra("isNotificationIntent", true), PendingIntent.FLAG_UPDATE_CURRENT))
                    }).build())
                }
                NotificationID.NOTIF_MAC_REJECTED -> {}
            }
        }

        fun cancelApprovedDeviceNotification(context: Context) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NotificationID.NOTIF_MAC_APPROVED.notificationId)
        }

        fun createNotificationChannels(applicationContext: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                enumValues<NotificationChannels>().forEach {
                    notificationManager.createNotificationChannel(NotificationChannel(it.channelId, applicationContext.getString(it.channelName), it.importance).apply {
                        description = applicationContext.getString(it.descriptionText)
                        if(it == NotificationChannels.CHANNEL_CONTROL) {
                            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        }
                    })
                }
            }
        }
    }
}