package com.vtsappsteam.labaccesscontrol.activities.login.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.iid.FirebaseInstanceId

import com.vtsappsteam.labaccesscontrol.activities.MainActivity
import com.vtsappsteam.labaccesscontrol.activities.login.data.WaitApprovalViewModel
import com.vtsappsteam.labaccesscontrol.activities.login.ui.LoginActivity
import com.vtsappsteam.labaccesscontrol.databinding.WaitApprovalFragmentBinding
import com.vtsappsteam.labaccesscontrol.http.Responsable
import com.vtsappsteam.labaccesscontrol.http.VolleyService
import com.vtsappsteam.labaccesscontrol.services.FirebaseMessagingService
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class WaitApprovalFragment : Fragment(), Responsable {
    companion object {
        fun newInstance() =
            WaitApprovalFragment()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra("doorPermission").let { message ->
                if(message != null && message == "true") {
                    activity!!.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString("doorPermission", message)
                        .apply()
                    viewModel.setProgressFinished()
                }
            }
            intent.getStringExtra("tokenUploaded").let { message ->
                if(message != null && message == "true") {
                    viewModel.setProgressWaiting()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(broadcastReceiver, IntentFilter("ACTION_WAIT_ADMIN"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(broadcastReceiver)
    }

    private lateinit var viewModel: WaitApprovalViewModel
    private var setProgressWaiting : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(WaitApprovalViewModel::class.java)
        return WaitApprovalFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@WaitApprovalFragment.viewLifecycleOwner
            fragment = this@WaitApprovalFragment
            viewModel = this@WaitApprovalFragment.viewModel.apply { if(setProgressWaiting) setProgressWaiting() }
        }.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginActivity)
            checkForToken(context)
    }

    fun onNextButtonClicked() {
        activity?.let {
            it.window.exitTransition = null
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(it,
                it.logoHeader,
                ViewCompat.getTransitionName(it.logoHeader)!!
            )
            it.startActivity(Intent(activity, MainActivity::class.java), options.toBundle())
            it.finish()
        }
    }

    fun onCancelButtonClicked() {
        activity!!.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        activity!!.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        FirebaseMessagingService.disableFCM()
        (activity as LoginActivity).backFragment()
    }

    private fun checkForToken(activity: LoginActivity) {
        if (FirebaseMessagingService.isLastTokenUploaded(activity.applicationContext).not()) {
            if (FirebaseMessagingService.isLastTokenUploading().not()) {
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    val userToken =
                        activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                            .getString("uniqueToken", "")
                    val fireBaseToken = it.token

                    val http = VolleyService(this@WaitApprovalFragment, activity.applicationContext)
                    http.post(Constants.API_ROUTE_UPDATE_FIREBASE_TOKEN, JSONObject().apply {
                        put("token", userToken)
                        put("newToken", fireBaseToken)
                    })
                }
            }
        } else {
            if(FirebaseMessagingService.isLastTokenUploading().not()) {
                setProgressWaiting = true
            }
        }
    }

    override fun successResponse(applicationContext: Context, res: JSONObject) {
        viewModel.setProgressWaiting()

        FirebaseMessagingService.isLastTokenUploaded(applicationContext, true)
    }

    override fun errorResponse(applicationContext: Context, statusCode: Int, message: String) {
        FirebaseMessagingService.isLastTokenUploaded(applicationContext, false)
    }
}
