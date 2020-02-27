package com.vtsappsteam.labaccesscontrol.services.utils

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import com.vtsappsteam.labaccesscontrol.services.ConnectivityListener

internal object UnkillableServiceHelper {
    //for broadcast receiver intent filter
    const val MESSAGE = "ACTION_DOOR_PERMISSION_CHANGE"

    //to avoid crashes when this method is called by service (from itself) make sure the service is not already running (maybe is in cache)
    fun returnUpMyService(context: Context) {
        if (killServiceIfRun(context)) {
            startConnectivityService(context)
        }
    }

    private fun killServiceIfRun(context: Context): Boolean {
        val isRunning = isMyServiceRunning(context)
        if (!isRunning) {
            return true
        }
        try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // maybe killing process is not terminated by system in this case
            //I force to kill them by my one
            manager.killBackgroundProcesses(getServiceName(context))
            return true

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun isServiceInCache(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        if (manager != null && manager.runningAppProcesses != null) {
            if (manager.runningAppProcesses.size > 0) {
                for (process in manager.runningAppProcesses) {
                    if (process.processName != null) {
                        if (process.processName.equals(getServiceName(context), ignoreCase = true)) {
                            // Here we know that the service is running but sleep
                            if (process.importance != RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
    fun startMyService(context: Context) {
        if (isMyServiceRunning(context) && !isServiceInCache(context)) {
            context.sendBroadcast(Intent().apply {
                putExtra("doorPermission", "true")
                action = MESSAGE
            })
            return
        }
        // If service is running but is in cache is the same like killed, so we need to kill them
        if (isServiceInCache(context)) { // this method at first kill and after that start the service
            returnUpMyService(context)
        } else { //Otherwise we start own service
            startConnectivityService(context)
        }
    }

    fun stopMyService(context: Context) {
        context.sendBroadcast(Intent().apply {
            putExtra("doorPermission", "false")
            action = MESSAGE
        })
    }

    private fun startConnectivityService(context: Context) {
        // After we had been sure about that service doesn't exist
        // we make a schedule to restart them
        ScheduledThreadPoolExecutor(1).schedule({
            //Create an instance of ConnectivityListener
            val service = ConnectivityListener()
            //prepare the launch intent
            val launchIntent = Intent(context, service::class.java)
            // Now we start in background our service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(launchIntent)
            } else {
                context.startService(launchIntent)
            }
        }, 50, TimeUnit.MILLISECONDS)
    }

    private fun isMyServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        if (manager != null && manager.runningAppProcesses != null) {
            if (manager.runningAppProcesses.size > 0) {
                for (process in manager.runningAppProcesses) {
                    if (process.processName != null && process.processName.equals(getServiceName(context), ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getServiceName(context: Context): String {
        return context.packageName + ":serviceNonStoppable"//the name declared in manifest
    }
}