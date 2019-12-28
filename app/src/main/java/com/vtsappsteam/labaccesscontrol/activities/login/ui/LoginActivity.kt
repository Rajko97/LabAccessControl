package com.vtsappsteam.labaccesscontrol.activities.login.ui

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.MainActivity
import com.vtsappsteam.labaccesscontrol.activities.WaitActiveActivity
import com.vtsappsteam.labaccesscontrol.databinding.ActivityLoginBinding
import com.vtsappsteam.labaccesscontrol.http.Responsable
import com.vtsappsteam.labaccesscontrol.http.VolleyService
import com.vtsappsteam.labaccesscontrol.activities.login.data.LoginViewModel
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver.Companion.alertDialogNet
import com.vtsappsteam.labaccesscontrol.dialogs.WaitingDialog
import com.vtsappsteam.labaccesscontrol.utils.Constants
import com.vtsappsteam.labaccesscontrol.utils.Constants.Companion.API_ROUTE_LOGIN
import kotlinx.android.synthetic.main.activity_login.*

import org.json.JSONObject

class LoginActivity : AppCompatActivity(), Responsable, ConnectivityReceiver.ConnectivityReceiverListener {
    private lateinit var adNoInternetConnection: Dialog
    private lateinit var adMacProblem: Dialog
    private lateinit var progressBar: WaitingDialog
    private var connectivityReceiver: BroadcastReceiver = ConnectivityReceiver(this)

    private val viewModel by lazy { ViewModelProviders.of(this).get(LoginViewModel::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        val mBinding : ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        mBinding.lifecycleOwner = this
        mBinding.activity = this
        mBinding.viewModel = viewModel
        mBinding.mainLayout.requestFocus()

        adMacProblem = alertDialogNet(this, resources.getString(R.string.dialog_title_mac_notfound), resources.getString(R.string.dialog_text_no_mac))
        adNoInternetConnection = alertDialogNet(this, null, null)
        progressBar = WaitingDialog(this)
    }

    fun onButtonClicked() {
        if (viewModel.validateUsername() and viewModel.validatePassword()) {
            btnSingUp.isEnabled = false //todo using viewModel
            progressBar.show()
            val http = VolleyService(this, applicationContext)
            http.post(API_ROUTE_LOGIN, viewModel.getRequestBody())
        }
    }

    override fun successResponse(res: JSONObject) {
        progressBar.hide()
        val sp = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("username", res.getString("username"))
        edit.putString("firstName", res.getString("name"))
        edit.putString("lastName", res.getString("lastName"))
        edit.putString("rank", res.getString("rank"))
        edit.putString("routerMAC", res.getString("samsungAppsLabRouterMacAddress"))
        edit.putString("uniqueToken", res.getString("uniqueToken"))
        edit.putString("doorPermission", res.getBoolean("doorPermission").toString())
        edit.apply()

        startActivity(
            Intent(
                this,
                if (res.getBoolean("doorPermission")) MainActivity::class.java else WaitActiveActivity::class.java
            )
        )
        finish()
    }

    override fun errorResponse(statusCode : Int, message: String) {
        val runnable = Runnable {
            btnSingUp.isEnabled = true
            val errorMessage : String = when (statusCode) {
                401 -> {
                    viewModel.setUsernameError(R.string.error_username_notfound)
                    getString(R.string.error_error)
                }
                402 -> {
                    viewModel.setPasswordError(R.string.error_wrong_password)
                    getString(R.string.error_error)
                }
                503 -> getString(R.string.error_server_unavailable)
                else -> message
            }
            progressBar.hide()
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }

        val delayTime: Long? = progressBar.delayTime()
        if (delayTime != null) {
            val handler = Handler()
            handler.postDelayed(runnable, delayTime)
        } else {
            runnable.run()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        progressBar.dismiss()
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