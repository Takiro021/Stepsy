package com.example.stepsy
import android.content.Intent
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper


@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed(Runnable { //This method will be executed once the timer is over
            // Start your app main activity
            val i = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(i)
            // close this activity
            finish()
        }, 3000)
    }
}