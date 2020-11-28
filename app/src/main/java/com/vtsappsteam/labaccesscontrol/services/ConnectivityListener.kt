package com.vtsappsteam.labaccesscontrol.services

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.utils.Constants

class ConnectivityListener : Service() {
    private var hasLabAccess : Boolean = false
    private var isOnNetwork : Boolean = false
    private lateinit var samsungAppsLabRouterMAC : String
    private lateinit var wifiManager : WifiManager

    private val networkCallbacks: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            isOnNetwork = false
            stopService(Intent(this@ConnectivityListener, LabControlService::class.java))
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val wifiInfo : WifiInfo = wifiManager.connectionInfo

            if(wifiInfo.bssid.equals(samsungAppsLabRouterMAC, ignoreCase = true)) {
                isOnNetwork = true
                if(hasLabAccess) {
                    startService(Intent(this@ConnectivityListener, LabControlService::class.java))
                }
            }
        }
    }

    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newPermission = intent?.getStringExtra("doorPermission")
            if(newPermission == "true") {
                hasLabAccess = true
                if(isOnNetwork)
                    startService(Intent(this@ConnectivityListener, LabControlService::class.java))
            } else if(newPermission == "false") {
                hasLabAccess = false
                stopService(Intent(this@ConnectivityListener, LabControlService::class.java))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        hasLabAccess = "true" == applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("doorPermission", "false")
        samsungAppsLabRouterMAC = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("routerMAC", "") as String

        connectivityManager.registerNetworkCallback(networkRequest, networkCallbacks)

        if(ConnectivityReceiver.isConnectedOnSamsungAppsLab(this@ConnectivityListener)) {
            isOnNetwork = true
            if(hasLabAccess) {
                startService(Intent(this@ConnectivityListener, LabControlService::class.java))
            }
        }

        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(123456, NotificationCompat.Builder(this@ConnectivityListener, Notifications.NotificationChannels.CHANNEL_HIDDEN.channelId)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this@ConnectivityListener.resources, R.mipmap.ic_launcher_foreground))
                .setAutoCancel(false)
                .setProgress(0, 0, true)
                .setContentTitle(applicationContext.getString(R.string.notification_network_monitoring_title))
                .setContentText(applicationContext.getString(R.string.notification_network_monitoring_desc) )
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .build())
            Service.START_STICKY
        } else {
            Service.START_REDELIVER_INTENT
        }
    }

    override fun onCreate() {
        if(::wifiManager.isInitialized.not()) {
            wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
        super.onCreate()
        registerReceiver(broadcastReceiver, IntentFilter("ACTION_DOOR_PERMISSION_CHANGE"))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
    }
}