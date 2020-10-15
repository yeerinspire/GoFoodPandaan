package com.example.gofoodpandaan.Auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.gofoodpandaan.R
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler = Handler()
        handler.postDelayed({
            startActivity<AuthActivity>()
            finish()
        }, 3000)
    }
    }
