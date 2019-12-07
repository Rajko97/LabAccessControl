package com.vtsappsteam.labaccesscontrol.activities

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vtsappsteam.labaccesscontrol.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val typeface = Typeface.createFromAsset(assets, "exo.ttf")
        tvSignUp.typeface = typeface
        inputUsername.typeface = typeface
        inputPassword.typeface = typeface
        btnSingUp.typeface = typeface
    }
}
