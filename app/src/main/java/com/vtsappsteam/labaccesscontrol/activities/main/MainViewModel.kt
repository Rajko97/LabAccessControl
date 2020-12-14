package com.vtsappsteam.labaccesscontrol.activities.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vtsappsteam.labaccesscontrol.activities.main.fragments.presentmembers.MemberData
import org.threeten.bp.LocalDateTime
import kotlin.collections.ArrayList

class MainViewModel : ViewModel() {
    private var lastUpdateTime : LocalDateTime? = null
    private var mutablePresentMembersList = MutableLiveData<ArrayList<MemberData>>()

    private val mutablePresentMembersCount = MutableLiveData<Int?>()
    val presentMembersCount: LiveData<Int?> get() = mutablePresentMembersCount

    val presentMemberList : LiveData<ArrayList<MemberData>> get() = mutablePresentMembersList
    fun setPresentMembersList(list : ArrayList<MemberData>) {
        if (mutablePresentMembersList.value == null) {
            mutablePresentMembersList = MutableLiveData(ArrayList<MemberData>())
        }
        mutablePresentMembersList.value!!.clear()
        mutablePresentMembersList.value!!.addAll(list)
        mutablePresentMembersCount.value = mutablePresentMembersList.value!!.size
        lastUpdateTime = LocalDateTime.now()
    }

    fun getLastUpdateTime() : LocalDateTime? {
        return lastUpdateTime
    }
}