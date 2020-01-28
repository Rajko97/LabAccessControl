package com.vtsappsteam.labaccesscontrol.services

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vtsappsteam.labaccesscontrol.http.Responsable
import com.vtsappsteam.labaccesscontrol.http.VolleyService
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.utils.Constants
import org.json.JSONObject
import java.io.IOException

class FirebaseMessagingService : FirebaseMessagingService(), Responsable {

    override fun onMessageReceived(remoteMessage : RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data["access"]?.let {
                Notifications.displayNotification(applicationContext, Notifications.NotificationID.NOTIF_MAC_APPROVED)
                applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit().putString("doorPermission", it).apply()
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent().apply {
                    putExtra("doorPermission", it)
                    action = "ACTION_WAIT_ADMIN"
                })
            }
            remoteMessage.data["deviceRequest"]?.let {
                Notifications.displayNotification(applicationContext, Notifications.NotificationID.NOTIF_MAC_REJECTED)
            }
        }
        /*
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            // Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
        }*/
    }

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val userToken = applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("uniqueToken", "")
        val http = VolleyService(this, applicationContext)
        http.post(Constants.API_ROUTE_UPDATE_FIREBASE_TOKEN, JSONObject().apply {
            put("token", userToken)
            put("newToken", token)
        })
    }

    override fun successResponse(res: JSONObject) {
        isLastTokenUploaded(applicationContext, true)
    }

    override fun errorResponse(statusCode: Int, message: String) {
       isLastTokenUploaded(applicationContext, false)
    }

    companion object {
        fun isLastTokenUploaded(applicationContext: Context, isIt : Boolean) {
            applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean("isLastTokenUploaded", isIt).apply()
        }
        fun isLastTokenUploaded(applicationContext: Context) : Boolean {
            return applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getBoolean("isLastTokenUploaded", true)
        }

        fun enableFCM() {
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
        }

        fun disableFCM() {
            FirebaseMessaging.getInstance().isAutoInitEnabled = false
            Thread {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}