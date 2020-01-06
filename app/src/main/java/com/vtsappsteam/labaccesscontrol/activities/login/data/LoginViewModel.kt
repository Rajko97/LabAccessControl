package com.vtsappsteam.labaccesscontrol.activities.login.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.broadcast_receiver.ConnectivityReceiver
import org.json.JSONObject


class LoginViewModel : ViewModel() {
    val editUsernameContent = MutableLiveData<String>("")
    val editPasswordContent = MutableLiveData<String>("")

    private val _textErrorUsername = MutableLiveData<Int?>()
    private val _textErrorPassword = MutableLiveData<Int?>()

    val textErrorUsername : LiveData<Int?> = _textErrorUsername
    val textErrorPassword : LiveData<Int?> = _textErrorPassword

    private val _usernameFocus = MutableLiveData<Boolean>()
    private val _passwordFocus = MutableLiveData<Boolean>()
    val editUsernameFocus : LiveData<Boolean> = _usernameFocus
    val editPasswordFocus : LiveData<Boolean> = _passwordFocus

    private val usernameObserver : Observer<String> = Observer { validateUsername() }
    private val passwordObserver: Observer<String> = Observer { validatePassword() }

    override fun onCleared() {
        super.onCleared()
        editUsernameContent.removeObserver(usernameObserver)
        editPasswordContent.removeObserver(passwordObserver)
    }

    //Additional function which sets observer if data is invalid
    //Observer check for validation
    //Result: When user make mistake for first time, it will always show how to set input correctly
    fun validateUsername() : Boolean {
        val valid = doUsernameValidation()
        if (valid.not()) {
            editUsernameContent.observeForever(usernameObserver)
        }
        return valid
    }

    private fun doUsernameValidation(): Boolean {
        _textErrorUsername.postValue(null)
        editUsernameContent.value.let { username ->
            if (username == null || username.isEmpty()) {
                _textErrorUsername.postValue(R.string.error_username_empty)
                return false
            }
            if(username.contains(" ")) {
                _textErrorUsername.postValue(R.string.error_username_blank)
                return false
            }
            if(username.indexOf(".") == 0 || username.lastIndexOf(".") == username.length-1 || username.count { ".".contains(it) } != 1) {
                _textErrorUsername.postValue(R.string.error_username_dot_format)
                return false
            }
        }
        _textErrorUsername.postValue(null)
        return true
    }

    fun setUsernameError(errorTextId : Int?) {
            _textErrorUsername.value = errorTextId
            _usernameFocus.value = _usernameFocus.value?.not()
    }

    fun validatePassword() : Boolean {
        val valid = doPasswordValidation()
        if(valid.not()) {
            editPasswordContent.observeForever(passwordObserver)
        }
        return valid
    }

    private fun doPasswordValidation(): Boolean {
        _textErrorPassword.postValue(null)
        editPasswordContent.value.let { password ->
            if (password == null || password.isEmpty()) {
                _textErrorPassword.postValue(R.string.error_password_empty)
                return false
            }
            if(password.contains(" ")) {
                _textErrorPassword.postValue(R.string.error_password_blank)
                return false
            }
        }
        _textErrorPassword.postValue(null)
        return true
    }

    fun setPasswordError(errorTextId: Int?) {
        _textErrorPassword.value = errorTextId
        _passwordFocus.value = _passwordFocus.value?.not()
    }

    fun getRequestBody(): JSONObject {
        val requestBody = JSONObject()
        requestBody.put("username", editUsernameContent.value)
        requestBody.put("password", editPasswordContent.value)
        requestBody.put("MACAddress", ConnectivityReceiver.getMacAddress())
        return requestBody
    }
}