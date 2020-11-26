package com.vtsappsteam.labaccesscontrol.activities.login.ui

import android.Manifest
import android.accounts.AccountManager
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.login.ui.fragments.CredentialsFragment
import com.vtsappsteam.labaccesscontrol.activities.login.ui.fragments.WaitApprovalFragment
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver.Companion.alertDialogNet
import com.vtsappsteam.labaccesscontrol.dialogs.WaitingDialog
import kotlinx.android.synthetic.main.activity_login.*

private const val PERMISSION_FINE_LOCATION = 747

class LoginActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private lateinit var adNoInternetConnection: Dialog
    private lateinit var adMacProblem: Dialog
    private lateinit var progressBar: WaitingDialog
    private var connectivityReceiver: BroadcastReceiver = ConnectivityReceiver(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if(savedInstanceState == null)
            setFragment(if(intent.getBooleanExtra("isLoggedIn", false)) WaitApprovalFragment.newInstance() else CredentialsFragment.newInstance())
        clearStartAnimations()
        clearKeyboardOnLayoutClick()
        adMacProblem = alertDialogNet(this, resources.getString(R.string.dialog_title_mac_notfound), resources.getString(R.string.dialog_text_no_mac))
        adNoInternetConnection = alertDialogNet(this, null, null)
        progressBar = WaitingDialog(this)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_FINE_LOCATION)
        }
     }

    private fun setFragment(fragment : Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun clearStartAnimations() {
        val decor = window.decorView
        window.enterTransition = null
        window.sharedElementEnterTransition.duration = 700
        window.sharedElementReturnTransition.duration = 700
        window.sharedElementReturnTransition.interpolator = DecelerateInterpolator()
        Fade().run {
            excludeTarget(decor.findViewById<View>(R.id.action_bar_container), true)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
        }
    }

    private fun clearKeyboardOnLayoutClick() {
        mainLayout.run {
            requestFocus()
            setOnFocusChangeListener { v, hasFocus ->
                if(v.id == this.id && hasFocus) {
                    val imm : InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
    }

    fun nextFragment() {
        val fragmentManager = supportFragmentManager

        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.fragmentContainer, WaitApprovalFragment.newInstance())
        transaction.commit()
    }

    fun backFragment() {
        val fragmentManager = supportFragmentManager

        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_left,
            R.anim.exit_to_right,
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        transaction.replace(R.id.fragmentContainer, CredentialsFragment.newInstance())
        transaction.commit()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityReceiver)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(connectivityReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        checkNet()
    }

    private fun checkNet() {
        if(ConnectivityReceiver.isConnected()) {
            if(ConnectivityReceiver.isMacValid(this)) {
                adNoInternetConnection.dismiss()
                adMacProblem.dismiss()
            }
            else {
                adMacProblem.show()
            }
        }
        else {
            adNoInternetConnection.show()
        }
    }

    override fun onNetworkConnectionChanged(connectionType: ConnectivityReceiver.ConnectionType, onSamsungAppsLab: Boolean) {
        checkNet()
    }
}