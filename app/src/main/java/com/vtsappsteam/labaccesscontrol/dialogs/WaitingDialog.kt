package com.vtsappsteam.labaccesscontrol.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.Constants


class WaitingDialog : Dialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super (context, cancelable, cancelListener)

    init {
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /*val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.CENTER// or Gravity.LEFT
        //layoutParams.x = 30
        //layoutParams.y = -120
        window!!.attributes = layoutParams*/
    }

    private var dialogShowedAt : Long = 0

    override fun show() {
        super.show()
        dialogShowedAt = System.currentTimeMillis()
    }

    fun delayTime(): Long? {
        val currentTime = System.currentTimeMillis()
        val timeWhenDialogShouldDisappears = dialogShowedAt+Constants.DIALOG_MINIMUM_CLOSE_TIME
        return if (timeWhenDialogShouldDisappears > currentTime) {
            timeWhenDialogShouldDisappears-currentTime
        } else null
    }
}