package com.example.airadvise.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.activities.RegisterActivity
import com.example.airadvise.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screeen)
        lifecycleScope.launch {
            delay(1500)
            startActivity(Intent(this@SplashScreenActivity, RegisterActivity::class.java))
            finish()
        }
    }
}