package com.vtsappsteam.labaccesscontrol.activities

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.widget.Toast
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.TypefaceSpan
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setContentViewDesign()
        btnSingUp.setOnClickListener {
            if(validateUsername() and validatePassword()) {
                Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                textInputLayout1.error = null
                textInputLayout2.error = null
            }
        }
    }
    private fun validateUsername():Boolean {
        if(inputUsername.text.toString().trim().isEmpty())
        {
            val s = SpannableString("* Polje za korisničko ime ne sme biti prazno!")
            s.setSpan(TypefaceSpan(Typeface.createFromAsset(assets, "exo.ttf"), Color.parseColor("#b20000")), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textInputLayout1.error = s
            inputUsername.requestFocus()
             return false
        }
        return true
    }
    private fun validatePassword():Boolean {
        if(inputPassword.text.toString().trim().isEmpty())
        {
            val s = SpannableString("* Polje za lozinku ne sme biti prazno!")
            s.setSpan(TypefaceSpan(Typeface.createFromAsset(assets, "exo.ttf"), Color.parseColor("#b20000")), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textInputLayout2.error = s
            return false
        }
        return true
    }
    private fun setContentViewDesign() {
        val typeface = Typeface.createFromAsset(assets, "exo.ttf")

        tvSignUp.typeface = typeface
        inputUsername.typeface = typeface
        inputPassword.typeface = typeface
        btnSingUp.typeface = typeface
        textInputLayout1.typeface = typeface
        textInputLayout2.typeface = typeface
        textInputLayout1.setDrawable(resources.getDrawable(R.drawable.input_style_username, applicationContext.theme))
        textInputLayout2.setDrawable(resources.getDrawable(R.drawable.input_style_password, applicationContext.theme))
        mainLayout.requestFocus()
    }
}
