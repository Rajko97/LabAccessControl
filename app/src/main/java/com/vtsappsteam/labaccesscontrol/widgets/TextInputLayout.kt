package com.vtsappsteam.labaccesscontrol.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

class TextInputLayout : TextInputLayout{
    private lateinit var mDrawable : Drawable

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs : AttributeSet) : super (context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    fun setDrawable(drawable : Drawable) {
        mDrawable =drawable
    }

    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)
        replaceBackground()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        replaceBackground()
    }

    private fun replaceBackground() {
        val ed = editText
        if(ed != null) {
            ed.background = mDrawable/*if (isErrorEnabled) drawable else drawable2*/
            ed.background?.clearColorFilter()
        }
    }
}