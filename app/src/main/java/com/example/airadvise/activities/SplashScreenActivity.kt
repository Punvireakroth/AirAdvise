package com.example.airadvise.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.MainActivity
import com.example.airadvise.R
import com.example.airadvise.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screeen)
        lifecycleScope.launch {
            delay(1500)
            if (SessionManager.isLoggedIn(this@SplashScreenActivity)) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
                return@launch
            }
            startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            finish()
        }
    }
}