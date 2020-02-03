package com.vtsappsteam.labaccesscontrol.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.transition.Fade
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)
        Notifications.cancelApprovedDeviceNotification(this)

        val decor = window.decorView
        window.enterTransition = null
        window.sharedElementEnterTransition.duration = 1000
        window.sharedElementReturnTransition.duration = 1000
        window.sharedElementReturnTransition.interpolator = DecelerateInterpolator()
        Fade().apply {
            excludeTarget(decor.findViewById<View>(R.id.action_bar_container), true)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
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
        startActivity(Intent(this@MainActivity, LoginActivity::class.java), options.toBundle())
        finish()
    }
}