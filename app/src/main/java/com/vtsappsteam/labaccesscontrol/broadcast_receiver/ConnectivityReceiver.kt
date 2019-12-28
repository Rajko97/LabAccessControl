package com.vtsappsteam.labaccesscontrol.broadcast_receiver

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.internal.VisibilityAwareImageButton
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.Constants
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.net.NetworkInterface
import java.util.*

class ConnectivityReceiver(private var connectivityReceiverListener: ConnectivityReceiverListener) : BroadcastReceiver() {
    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(connectionType: ConnectionType, onSamsungAppsLab : Boolean)
    }

    enum class ConnectionType {
        CONNECTION_NO_INTERNET,
        CONNECTION_MOBILE_DATA,
        CONNECTION_WIFI
    }

    companion object {
        private const val defaultMac : String = "02:00:00:00:00:00"
        private var isConnected : Boolean = false
        private var macAddress : String =
            defaultMac

        fun isConnected() : Boolean {
            return isConnected
        }

        fun isMacValid(context: Context) : Boolean {
            if(macAddress == defaultMac) {
                macAddress =
                    findMacAddress(
                        context
                    )
            }
            return macAddress != defaultMac
        }

        fun getMacAddress() : String {
            return macAddress
        }

        @SuppressLint("HardwareIds")
        @Suppress("SpellCheckingInspection")
        private fun findMacAddress(context: Context) : String {

            val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = manager.connectionInfo

            if(info.macAddress == defaultMac) {
                var ret : String?
                try {
                    ret =
                        getAddressMacByInterface()
                    return if (ret != null) {
                        ret
                    } else {
                        ret =
                            getAddressMacByFile(
                                manager
                            )
                        ret
                    }
                } catch (e : IOException) {
                    e.printStackTrace()
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            } else {
                return info.macAddress
            }
            return defaultMac
        }

        @Suppress("DEPRECATION")
        private fun getAddressMacByFile(wifiManager: WifiManager): String {
            val fileAddressMac = "/sys/class/net/wlan0/address"
            val ret : String
            val wifiState = wifiManager.wifiState

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = true
            }
            val file  = File(fileAddressMac)
            val fin  = FileInputStream(file)
            val builder : java.lang.StringBuilder = java.lang.StringBuilder()
            var ch : Int
            ch = fin.read()
            while(ch != -1) {
                builder.append(ch.toChar())
                ch = fin.read()
            }
            ret = builder.toString()
            fin.close()
            val enabled : Boolean = WifiManager.WIFI_STATE_ENABLED == wifiState
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = enabled
            }
            return ret
        }

        private fun getAddressMacByInterface() : String? {
            try {
                val all = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if (!nif.name.equals("wlan0", ignoreCase=true)) continue

                    val macBytes = nif.hardwareAddress ?: return ""

                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        res1.append(String.format("%02X:", b))
                    }

                    if (res1.isNotEmpty()) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }

        fun alertDialogNet(context: Context, title : String?, message : String?) : Dialog {
           val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_connection)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            title?.let { (dialog.findViewById(R.id.dialogTitle) as TextView).text = title }
            message?.let { (dialog.findViewById(R.id.dialogDescription) as TextView).text = message}

            if(title != null || message != null){
                dialog.findViewById<ImageView>(R.id.dialogImage).visibility = VisibilityAwareImageButton.GONE
            }
            dialog.setCancelable(false)
            dialog.findViewById<TextView>(R.id.buttonSettings).setOnClickListener {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                context.startActivity(intent)
            }
           dialog.findViewById<TextView>(R.id.buttonExit).setOnClickListener {
               dialog.dismiss()
               (context as Activity).finish()
           }
            return dialog
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val networkType = checkNetworkAvailable(context)
            isConnected = networkType != ConnectionType.CONNECTION_NO_INTERNET
            connectivityReceiverListener.onNetworkConnectionChanged(
                networkType,
                connectedOnSamsungAppsLab(context)
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun checkNetworkAvailable(context: Context): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return ConnectionType.CONNECTION_NO_INTERNET
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return ConnectionType.CONNECTION_NO_INTERNET
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.CONNECTION_WIFI
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CONNECTION_MOBILE_DATA
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.CONNECTION_WIFI
                else -> ConnectionType.CONNECTION_NO_INTERNET
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return ConnectionType.CONNECTION_NO_INTERNET
            return if (nwInfo.isConnected) ConnectionType.CONNECTION_MOBILE_DATA else ConnectionType.CONNECTION_NO_INTERNET
        }
    }

    private fun connectedOnSamsungAppsLab(context: Context) : Boolean {
        val sp: SharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val manager : WifiManager? = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return manager?.connectionInfo?.bssid.toString().toUpperCase(Locale.ROOT) == (sp.getString("routerMAC", "notSet") as String).toUpperCase(Locale.ROOT)
    }
}