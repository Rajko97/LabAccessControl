package com.vtsappsteam.labaccesscontrol.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.transition.Fade
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.iid.FirebaseInstanceId
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.http.Responsable
import com.vtsappsteam.labaccesscontrol.http.VolleyService
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.wait_active_activity.*
import org.json.JSONObject


class WaitActiveActivity : AppCompatActivity(), Responsable {

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra("doorPermission").let { message ->
                if(message != null && message == "true") {
                    this@WaitActiveActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString("doorPermission", message)
                        .apply()
                }
                startActivity(Intent(this@WaitActiveActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.
            getInstance(this@WaitActiveActivity)
            .registerReceiver(broadcastReceiver, IntentFilter("ACTION_WAIT_ADMIN"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this@WaitActiveActivity).unregisterReceiver(broadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wait_active_activity)

        setSupportActionBar(appToolbar)

        val decor = window.decorView
        window.enterTransition = null
        Fade().apply {
            excludeTarget(decor.findViewById<View>(R.id.action_bar_container), true)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
        }

        checkForToken()
    }

    private fun checkForToken() {
         if (FirebaseMessagingService.isLastTokenUploaded(applicationContext).not()) {
             FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                 val userToken = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getString("uniqueToken", "")
                 val fireBaseToken = it.token

                 val http = VolleyService(this@WaitActiveActivity, applicationContext)
                 http.post(Constants.API_ROUTE_UPDATE_FIREBASE_TOKEN, JSONObject().apply {
                     put("token", userToken)
                     put("newToken", fireBaseToken)
                 })
             }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.wait_active_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.buttonLogout) {
            logOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logOut() {
        getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        window.exitTransition = null
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, logoImage,
            ViewCompat.getTransitionName(logoImage)!!
        )
        FirebaseMessagingService.disableFCM()
        startActivity(Intent(this@WaitActiveActivity, LoginActivity::class.java), options.toBundle())
        finish()
    }

    override fun successResponse(res: JSONObject) {
        FirebaseMessagingService.isLastTokenUploaded(applicationContext, true)
    }

    override fun errorResponse(statusCode: Int, message: String) {
        FirebaseMessagingService.isLastTokenUploaded(applicationContext, false)
    }
}