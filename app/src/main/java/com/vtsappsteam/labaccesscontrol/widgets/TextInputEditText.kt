package com.vtsappsteam.labaccesscontrol.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText

class TextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle)
    : TextInputEditText(context, attrs, defStyleAttr)
{
    init {
        if (context is Activity) {
            Log.d("Testiranje", "Jeste aktivnost")
            if (context.currentFocus is com.vtsappsteam.labaccesscontrol.widgets.TextInputEditText)
            {
                Log.d("Testiranje", "Focus JESTE na TextInputEditText")
            } else {
                Log.d("Testiranje", "Focus nije na testwjiadw")
            }
        } else {
            Log.d("Testiranje", "Cont nije aktivnsot")
        }

        setOnFocusChangeListener { v, hasFocus ->
            //if(v is com.vtsappsteam.labaccesscontrol.widgets.TextInputEditText)
            if(v.id == this@TextInputEditText.id && !hasFocus) {
                val imm : InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    private val parentRect = Rect()

    override fun getFocusedRect(rect: Rect?) {
        super.getFocusedRect(rect)
        rect?.let {
            getTextInputLayout()?.getFocusedRect(parentRect)
            rect.bottom = parentRect.bottom
        }
    }

    override fun getGlobalVisibleRect(rect: Rect?, globalOffset: Point?): Boolean {
        val result = super.getGlobalVisibleRect(rect, globalOffset)
        rect?.let {
            getTextInputLayout()?.getGlobalVisibleRect(parentRect, globalOffset)
            rect.bottom = parentRect.bottom
        }
        return result
    }

    override fun requestRectangleOnScreen(rect: Rect?): Boolean {
        val result = super.requestRectangleOnScreen(rect)
        val parent = getTextInputLayout()
        // 10 is a random magic number to define a rectangle height.
        parentRect.set(0, parent?.height ?: 10 - 24, parent?.right ?: 0, parent?.height?: 0)
        parent?.requestRectangleOnScreen(parentRect, true /*immediate*/)
        return result
    }

    private fun getTextInputLayout(): TextInputLayout? {
        var parent = parent
        while (parent is View) {
            if (parent is TextInputLayout) {
                return parent
            }
            parent = parent.getParent()
        }
        return null
    }
}