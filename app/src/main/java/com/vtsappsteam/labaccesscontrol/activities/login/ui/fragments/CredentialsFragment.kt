package com.vtsappsteam.labaccesscontrol.activities.login.ui.fragments

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat

import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.MainActivity
import com.vtsappsteam.labaccesscontrol.activities.login.data.CredentialsViewModel
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.databinding.CredentialsFragmentBinding
import com.vtsappsteam.labaccesscontrol.dialogs.WaitingDialog
import com.vtsappsteam.labaccesscontrol.http.Responsable
import com.vtsappsteam.labaccesscontrol.http.VolleyService
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class CredentialsFragment : Fragment(), Responsable {
    companion object {
        fun newInstance() =
            CredentialsFragment()
    }

    private lateinit var viewModel: CredentialsViewModel
    private lateinit var progressBar: WaitingDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(CredentialsViewModel::class.java)
        progressBar = WaitingDialog(activity!!)
        return CredentialsFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@CredentialsFragment.viewLifecycleOwner
            fragment = this@CredentialsFragment
            viewModel = this@CredentialsFragment.viewModel
       }.root
    }

    fun onButtonClicked() {
        if (viewModel.validateUsername() and viewModel.validatePassword()) {
            viewModel.setButtonEnabled(false)
            progressBar.show()
            val http = VolleyService(this, activity?.applicationContext!!)
            http.post(Constants.API_ROUTE_LOGIN, viewModel.getRequestBody())
        }
    }

    override fun successResponse(applicationContext: Context, res: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.delayTime()?.let { delay(it) }
            progressBar.hide()
            activity?.let {
                it.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().apply {
                    putString("username", res.getString("username"))
                    putString("firstName", res.getString("name"))
                    putString("lastName", res.getString("lastName"))
                    putString("rank", res.getString("rank"))
                    putString("routerMAC", res.getString("samsungAppsLabRouterMacAddress"))
                    putString("uniqueToken", res.getString("uniqueToken"))
                    apply()
                }
                applicationContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit()
                    .putString("doorPermission", res.getBoolean("doorPermission").toString())
                    .putString("uniqueToken", res.getString("uniqueToken"))
                    .apply()

                FirebaseMessagingService.enableFCM()

                when (res.getBoolean("doorPermission")) {
                    true -> {
                        it.window.exitTransition = null
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(it,
                            it.logoHeader,
                            ViewCompat.getTransitionName(it.logoHeader)!!
                        )
                        startActivity(Intent(it, MainActivity::class.java), options.toBundle())
                        it.finish()
                    }
                    false -> {
                        if(it is LoginActivity) it.nextFragment()
                    }
                }
            }
        }
    }

    override fun errorResponse(applicationContext: Context, statusCode: Int, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.delayTime()?.let { delay(it) }
            viewModel.setButtonEnabled(true)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        progressBar.dismiss()
    }
}
