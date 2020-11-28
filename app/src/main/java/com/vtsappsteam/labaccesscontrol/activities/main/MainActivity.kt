package com.vtsappsteam.labaccesscontrol.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.Fade
import com.google.android.material.navigation.NavigationView
import com.rupins.drawercardbehaviour.CardDrawerLayout
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.activities.main.fragments.PresentMembersFragment
import com.vtsappsteam.labaccesscontrol.activities.main.fragments.LabControlFragment
import com.vtsappsteam.labaccesscontrol.activities.main.fragments.SettingsFragment
import com.vtsappsteam.labaccesscontrol.activities.main.fragments.TimeInLabFragment
import com.vtsappsteam.labaccesscontrol.services.ConnectivityListener
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.services.utils.Notifications
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var drawer: CardDrawerLayout? = null

    private lateinit var fragmentHome : LabControlFragment
    private lateinit var fragmentCurrentInLab : PresentMembersFragment
    private lateinit var fragmentTimeInLab : TimeInLabFragment
    private lateinit var fragmentSettings : SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)
        setNavigationDrawers()

        fragmentHome = LabControlFragment()
        fragmentCurrentInLab = PresentMembersFragment()
        fragmentTimeInLab = TimeInLabFragment()
        fragmentSettings = SettingsFragment()

        if(savedInstanceState == null)
            setFragmentByChosenItem(R.id.nav_home_page)

        Notifications.cancelApprovedDeviceNotification(this)
        val decor = window.decorView
        window.enterTransition = null
        window.sharedElementEnterTransition.duration = 1000
        window.sharedElementReturnTransition.duration = 1000
        window.sharedElementReturnTransition.interpolator = AccelerateInterpolator(10f)
        Fade().apply {
            excludeTarget(decor.findViewById<View>(R.id.action_bar_container), true)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
        }

        val launchIntent = Intent(this@MainActivity, ConnectivityListener::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this@MainActivity.startForegroundService(launchIntent)
        } else {
            this@MainActivity.startService(launchIntent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setNavigationDrawers() {
        drawer = findViewById<View>(R.id.drawer_layout) as CardDrawerLayout

        //ToDo: Retrieve information from session
        val headerView = nav_view.getHeaderView(0)
        headerView.userName.text = "Milan Rajković"
        headerView.userRank.text = "Senior member"
        headerView.imageView.setImageResource(R.drawable.avatar)

        //ToDo: Check if is user admin
        if(false) {
            nav_view.inflateMenu(R.menu.main_activity_drawer_admin)
        }

        val toggle = ActionBarDrawerToggle(this, drawer, appToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(0).isChecked = true
        (nav_view.menu.findItem(R.id.nav_current_in_lab).actionView as TextView).text = "10"

        drawer!!.setViewScale(Gravity.START, 0.9f)
        //drawer!!.setRadius(Gravity.START, 35f) //this creates graphic glitch on dark background
        drawer!!.setViewElevation(Gravity.START, 20f)
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { // Handle navigation view item clicks here.
        drawer!!.closeDrawer(GravityCompat.START)
        setFragmentByChosenItem(item.itemId)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_right_drawer -> {
                drawer!!.openDrawer(GravityCompat.END)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Returns whether fragment is set or not
    private fun setFragmentByChosenItem(itemId : Int) : Boolean {
        var fragmentReference : Fragment? = null
        when(itemId) {
            R.id.nav_home_page -> {
                fragmentReference = fragmentHome
            }
            R.id.nav_current_in_lab -> {
                fragmentReference = fragmentCurrentInLab
            }
            R.id.nav_timespent -> {
                fragmentReference = fragmentTimeInLab
            }
            R.id.nav_settings -> {
                logOut()
                //setFragment(fragmentSettings)
            }
        }

        if(fragmentReference != null) {
            val fragmentManager: FragmentManager = supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentReference)
                .commit()
            return true
        }
        return false
    }

    private fun logOut() {
        getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        window.exitTransition = null
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, logoImage,
            ViewCompat.getTransitionName(logoImage)!!
        )
        this@MainActivity.sendBroadcast(Intent().apply {
            putExtra("doorPermission", "false")
            action = "ACTION_DOOR_PERMISSION_CHANGE"
        })
        FirebaseMessagingService.disableFCM()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java), options.toBundle())
        finish()
    }
}