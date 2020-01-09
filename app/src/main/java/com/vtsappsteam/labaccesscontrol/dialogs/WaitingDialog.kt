package com.vtsappsteam.labaccesscontrol.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.Constants

class WaitingDialog(context: Context) : Dialog(context){
    init {
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /*
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
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

    fun delayAndHide(runnable: Runnable) {

        val delayTime: Long? = delayTime()
        if (delayTime != null) {
            val handler = Handler()
            handler.postDelayed(runnable, delayTime)
            handler.postDelayed({hide()}, delayTime)
        } else {
            runnable.run()
            hide()
        }
    }

    private fun delayTime(): Long? {
        val currentTime = System.currentTimeMillis()
        val timeWhenDialogShouldDisappears = dialogShowedAt+Constants.DIALOG_MINIMUM_CLOSE_TIME
        return if (timeWhenDialogShouldDisappears > currentTime) {
            timeWhenDialogShouldDisappears-currentTime
        } else null
    }
}