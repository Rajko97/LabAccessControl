package com.vtsappsteam.labaccesscontrol.widgets

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.TypefaceSpan

class TextInputLayout : TextInputLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs : AttributeSet) : super (context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    init {
        typeface = ResourcesCompat.getFont(context, R.font.exo_medium)
    }

    override fun setError(errorText: CharSequence?) {
        if(errorText != null) {
            val coloredErrorText = SpannableString(errorText)
            coloredErrorText.setSpan(TypefaceSpan(ResourcesCompat.getFont(context, R.font.exo_medium), Color.parseColor("#b20000")),
                0,
                coloredErrorText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            super.setError(coloredErrorText)
        } else {
            super.setError(errorText)
        }
        replaceBackground()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        replaceBackground()
    }

    private fun replaceBackground() {
        val ed = editText
        if(ed != null ) {
            ed.background?.clearColorFilter()
        }
    }
}