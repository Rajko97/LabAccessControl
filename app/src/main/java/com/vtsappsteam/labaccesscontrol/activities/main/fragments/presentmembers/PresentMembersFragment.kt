package com.vtsappsteam.labaccesscontrol.activities.main.fragments.presentmembers

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.header.ClassicsHeader
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.activities.main.MainViewModel
import com.vtsappsteam.labaccesscontrol.http.SocketIO
import com.vtsappsteam.labaccesscontrol.utils.Constants
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_present_members.*
import kotlinx.android.synthetic.main.fragment_present_members.view.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.time.LocalDateTime


class PresentMembersFragment : Fragment() {
    private val presentMembersRecyclerAdapter = PresentMembersRecyclerAdapter()
    private lateinit var refreshLayoutHeader : ClassicsHeader
    private lateinit var userModel: MainViewModel
    private lateinit var job : Job

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_present_members, container, false)

        refreshLayoutHeader = v.swipeRefreshLayout.refreshHeader as ClassicsHeader
        refreshLayoutHeader.apply {
            setEnableLastTime(false)
        }

        v.recyclerPresentMembers.apply {
            setHasFixedSize(false)
            layoutManager = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                LinearLayoutManager(context)
            else {
                GridLayoutManager(context, 2)
            }
            adapter = presentMembersRecyclerAdapter
        }

        if(userModel.presentMemberList.value?.isEmpty()?.not() == true) {
            v.layoutLoading.visibility = View.GONE
            v.tvErrorEmptyLab.visibility = View.GONE
            v.recyclerPresentMembers.visibility = View.VISIBLE
            (v.swipeRefreshLayout.refreshHeader as ClassicsHeader).setEnableLastTime(true)
            presentMembersRecyclerAdapter.submitMembersList(userModel.presentMemberList.value!!)
            refreshLayoutHeader.apply {
                setEnableLastTime(true)
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val time = userModel.getLastUpdateTime()!!.format(formatter)
                setLastUpdateText("${getString(R.string.refresh_last_update)} $time")
            }
        }
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.refresh_refreshing)
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.refresh_loading)
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.refresh_release)
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.refresh_finish)
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.refresh_failed)
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.refresh_pull);
        userModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.swipeRefreshLayout.setOnRefreshListener { refLayout ->
            SocketIO.getSocket().emit("getActiveMembers").on("getActiveMembers-res") {
                if(::job.isInitialized && job.isActive)
                    job.cancel()
                job = CoroutineScope(Dispatchers.Main).launch {
                    if(layoutLoading == null) return@launch
                    layoutLoading.visibility = View.GONE
                    val users = it[0] as JSONArray
                    if (users.length() == 0) {
                        tvErrorEmptyLab.visibility = View.VISIBLE
                        recyclerPresentMembers.visibility = View.GONE
                    } else {
                        tvErrorEmptyLab.visibility = View.GONE
                        recyclerPresentMembers.visibility = View.VISIBLE
                        val serialized =
                            Array<JSONObject>(users.length()) { i -> users.getJSONObject(i) }
                        val data = serialized.map {
                            MemberData(
                                it.getString("userId"),
                                it.getString("name"),
                                it.getString("lastName"),
                                it.getString("rank")
                            )
                        }
                        presentMembersRecyclerAdapter.submitMembersList(data)
                    }
                    userModel.setPresentMembersList(presentMembersRecyclerAdapter.getMembersList())
                    SocketIO.getSocket().off("getActiveMembers-res")
                    refLayout.finishRefresh(true)
                    refreshLayoutHeader.setEnableLastTime(false)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeSockets(SocketIO.getSocket())
    }

    override fun onStop() {
        super.onStop()
        if(::job.isInitialized && job.isActive)
            job.cancel()
    }

    private fun subscribeSockets(socket : Socket) {
        val progressBarStartedAt : Long = System.currentTimeMillis()
        fun delayTime(): Long? {
            val currentTime = System.currentTimeMillis()
            val timeWhenDialogShouldDisappears = progressBarStartedAt+ Constants.DIALOG_MINIMUM_CLOSE_TIME
            return if (timeWhenDialogShouldDisappears > currentTime) {
                timeWhenDialogShouldDisappears-currentTime
            } else null
        }

        socket.emit("getActiveMembers").on("getActiveMembers-res") {
                job = CoroutineScope(Dispatchers.Main).launch {
                    delayTime()?.let { it1 -> delay(it1) }
                    if(layoutLoading == null) return@launch
                    layoutLoading.visibility = View.GONE
                    val users = it[0] as JSONArray?
                    if (users == null || users.length() == 0) {
                        tvErrorEmptyLab.visibility = View.VISIBLE
                        recyclerPresentMembers.visibility = View.GONE
                    } else {
                        tvErrorEmptyLab.visibility = View.GONE
                        recyclerPresentMembers.visibility = View.VISIBLE

                        val serialized =
                            Array<JSONObject>(users.length()) { i -> users.getJSONObject(i) }
                        val data = serialized.map {
                            MemberData(
                                it.getString("userId"),
                                it.getString("name"),
                                it.getString("lastName"),
                                it.getString("rank")
                            )
                        }
                        presentMembersRecyclerAdapter.submitMembersList(data)
                    }
                    userModel.setPresentMembersList(presentMembersRecyclerAdapter.getMembersList())
                    refreshLayoutHeader.setEnableLastTime(false)
                    SocketIO.getSocket().off("getActiveMembers-res")
                }
        }

        socket.on("checkIn-res") {
            this.activity?.runOnUiThread {
                val member = it[0] as JSONObject
                val status = member.getString("message")
                if(status != "success" || tvErrorEmptyLab == null)
                    return@runOnUiThread
                val data = member.getJSONObject("data")
                presentMembersRecyclerAdapter.addMember(MemberData(data.getString("username"), data.getString("name"), data.getString("lastName"), data.getString("rank")))
                tvErrorEmptyLab.visibility = View.GONE
                recyclerPresentMembers.visibility = View.VISIBLE
                userModel.setPresentMembersList(presentMembersRecyclerAdapter.getMembersList())
            }
        }

        socket.on("checkOut-res") {
            this.activity?.runOnUiThread {
                val member = it[0] as JSONObject
                val mac = member.getString("message")
                presentMembersRecyclerAdapter.removeMember(mac)
                userModel.setPresentMembersList(presentMembersRecyclerAdapter.getMembersList())
                if(presentMembersRecyclerAdapter.itemCount == 0) {
                    if(tvErrorEmptyLab == null)
                        return@runOnUiThread
                    tvErrorEmptyLab.visibility = View.VISIBLE
                    recyclerPresentMembers.visibility = View.GONE
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            this.activity?.runOnUiThread {
                refreshLayoutHeader.apply {
                    refreshLayoutHeader.setEnableLastTime(true)
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val time = userModel.getLastUpdateTime()!!.format(formatter)
                    setLastUpdateText("${getString(R.string.refresh_last_update)} $time")
                }
            }
        }
    }
}