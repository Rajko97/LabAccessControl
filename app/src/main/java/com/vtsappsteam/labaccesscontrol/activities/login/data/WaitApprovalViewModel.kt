package com.vtsappsteam.labaccesscontrol.activities.login.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vtsappsteam.labaccesscontrol.R

class WaitApprovalViewModel : ViewModel() {
    private var _nextButtonEnabled = MutableLiveData<Boolean>(false)
    val enabled = _nextButtonEnabled as LiveData<Boolean>

    private var _progressText = MutableLiveData<Int>(R.string.progress_desc_contacting)
    val progressText = _progressText as LiveData<Int>

    fun setProgressWaiting() {
        _progressText.postValue(R.string.progress_desc_waiting)
    }

    fun setProgressFinished() {
        _nextButtonEnabled.postValue(true)
        _progressText.postValue(R.string.progress_desc_approved)
    }
}