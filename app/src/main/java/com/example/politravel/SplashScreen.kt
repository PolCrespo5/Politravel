package com.example.politravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val backgroundImage = findViewById<ImageView>(R.id.splashLogo)
        val sideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        backgroundImage.startAnimation(sideAnimation)

        Handler().postDelayed({
            startActivity(Intent(this, PantallaLlistat::class.java))
            finish()
        }, 3000
        )
    }
}