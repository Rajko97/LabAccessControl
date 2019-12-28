package com.vtsappsteam.labaccesscontrol.activities.login.util

import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.vtsappsteam.labaccesscontrol.widgets.TextInputLayout

@BindingAdapter("app:setErrorMessage")
fun setErrorMessage(textInputLayout: TextInputLayout?, errorMsgId: Int?) {
    if(errorMsgId == null)
        textInputLayout?.error = null
    else {
        textInputLayout?.error = textInputLayout?.resources?.getString(errorMsgId)
    }
}

@BindingAdapter("app:requireFocus")
fun requestFocus(editText: EditText?, focus : Boolean?) {
    if (focus != null)
        editText?.requestFocus()
}