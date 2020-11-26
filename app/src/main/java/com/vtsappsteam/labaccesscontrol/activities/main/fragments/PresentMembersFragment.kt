package com.vtsappsteam.labaccesscontrol.activities.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.http.SocketIO
import kotlinx.android.synthetic.main.fragment_present_members.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PresentMembersFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_present_members, container, false)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SocketIO.getSocket().emit("getActiveMembers", "")
        SocketIO.getSocket().on("getActiveMembers-res") {
            val users = it[0] as JSONArray
            Log.d("Testiranje", users.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shimmer_view_container.startShimmer()
        CoroutineScope(Dispatchers.Main).launch {
            delay(TimeUnit.SECONDS.toMillis(4))
            shimmer_view_container.stopShimmer()
            shimmer_view_container.visibility = View.GONE
            tvErrorEmptyLab.visibility = View.VISIBLE
        }
    }


}