package com.vtsappsteam.labaccesscontrol.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.main.MainActivity
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.NotificationReceiver
import com.vtsappsteam.labaccesscontrol.http.SocketIO
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject

class LabControlService : Service() {
    private lateinit var job : CompletableJob
    private lateinit var wifiManager : WifiManager
    private var isUserInLab : Boolean = false
    private lateinit var userToken : String

    override fun onCreate() {
        super.onCreate()
        job = Job()

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        userToken = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("uniqueToken", "unsigned") as String

        SocketIO.getSocket().on("unlock-res") {
            if (it[0] is JSONObject) {
                val message = (it[0] as JSONObject).getString("message")
                startForeground(531997, createNotification(message))
            }
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        startForeground(531997, createNotification(getString(R.string.button_unlock)))
        return START_STICKY
    }

    private fun startMonitoring() {
        if(job.isActive) {
            job.cancel()
        }
        job = Job()
        CoroutineScope(Default + job).launch {
            while (true) {
                val wifiInfo : WifiInfo = wifiManager.connectionInfo
                val lastStatus = isUserInLab
                if(wifiInfo.rssi >= Constants.CHECK_IN_WIFI_WIFI_STRENGTH)
                    isUserInLab = true
                else if (wifiInfo.rssi <= Constants.CHECK_OUT_WIFI_STRENGTH)
                    isUserInLab = false

                if(lastStatus != isUserInLab) {
                    if (isUserInLab) {
                        SocketIO.checkIn(userToken)
                        showToast(R.string.welcome_message)
                    } else {
                        SocketIO.checkOut(userToken)
                        showToast(R.string.goodbye_message)
                    }
                }
                delay(2000)
            }
        }
    }

    private fun createNotification(message : String) : Notification {
        val remoteViews = RemoteViews(packageName, R.layout.lab_access_control_notification)

        //OpenActivity
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        //OnUnlockButtonClick
        val clickIntent = Intent(this, NotificationReceiver::class.java)
        val clickPendingIntent = PendingIntent.getBroadcast(this, 0, clickIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.ivNotificationImage, clickPendingIntent)

        remoteViews.setImageViewBitmap(R.id.tvNotificationTitle, textAsBitmap("Lab Access Control", 40f, getColor(R.color.themeTextBlue), R.font.exo_medium))
        remoteViews.setImageViewBitmap(R.id.imgTextButton, textAsBitmap(message, 35f, getColor(R.color.themeTextGray), R.font.exo_medium))

        return NotificationCompat.Builder(this@LabControlService, Notifications.NotificationChannels.CHANNEL_CONTROL.channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(this@LabControlService.resources, R.mipmap.ic_launcher_foreground))
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setContent(remoteViews)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun showToast(stringId : Int) {
        GlobalScope.launch(Main) {
            Toast.makeText(this@LabControlService, getString(stringId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun textAsBitmap(messageText: String, textSize: Float, textColor: Int, textFont : Int): Bitmap? {
        val paint = Paint().apply {
            this.textSize = textSize
            this.typeface = ResourcesCompat.getFont(this@LabControlService, textFont)
            this.color = textColor
            this.textAlign = Paint.Align.LEFT
        }
        val baseline: Float = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(messageText) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()

        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(messageText, 0.0f, baseline, paint)
        return image
    }
}