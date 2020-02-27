package com.vtsappsteam.labaccesscontrol.services

import android.app.IntentService
import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.services.utils.UnkillableServiceHelper
import com.vtsappsteam.labaccesscontrol.utils.Constants
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

// Used to name the worker thread, important only for debugging
class ConnectivityListener : IntentService("com.vtsappsteam.labaccesscontrol:serviceNonStoppable") {
    private var hasLabAccess : Boolean = false
    private var isOnNetwork : Boolean = false
    private lateinit var samsungAppsLabRouterMAC : String
    private lateinit var wifiManager : WifiManager
    // Needed to keep up notifying without show the icon
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
    private var notifier: ScheduledExecutorService? = null

    override fun onHandleIntent(@Nullable intent: Intent?) {}
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        hasLabAccess = "true" == applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("doorPermission", "false")
        samsungAppsLabRouterMAC = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("routerMAC", "") as String

        connectivityManager.registerNetworkCallback(networkRequest, networkCallbacks)
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        registerReceiver(broadcastReceiver, IntentFilter(UnkillableServiceHelper.MESSAGE))
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationDefault: Notification =
                NotificationCompat.Builder(applicationContext, Notifications.NotificationChannels.CHANNEL_HIDDEN.channelId)
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle("Connectivity  Listener")
                    .setContentText("The service allows to control lab")
                    .build()

            // This is an efficient workaround to lie the system if we don't wont to show notification icon on top of the phone but a little aggressive
            notifier = Executors.newSingleThreadScheduledExecutor()
            notifier?.scheduleAtFixedRate({
                // Here start the notification witch system need to permit this service to run and take this on.
                startForeground(200, notificationDefault)
                //immediately after the system know about our service and permit this to run
                //at this point we remove that notification (note that is never shown before)
                stopForeground(true)
            }, 1000, 15000, TimeUnit.MILLISECONDS)  // And we repeat that task every 15 seconds
        }
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notifier != null) {
                notifier!!.shutdownNow()
                notifier = null
            }
            val context = baseContext
            object : Thread() {
                override fun run() {
                // When the system detects inactivity by our service decides to put them in cache or kill it
                    UnkillableServiceHelper.returnUpMyService(context)
                }
            }.start()
        }
    }
}