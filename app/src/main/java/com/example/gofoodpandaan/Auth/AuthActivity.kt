package com.example.gofoodpandaan.Auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.R
import kotlinx.android.synthetic.main.activity_auth.*
import org.jetbrains.anko.startActivity

class AuthActivity : AppCompatActivity() {
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        sessionManager = SessionManager(this)

        if (sessionManager.getLogin()!!){
            startActivity<HomeActivity>()
            finish()
        }

        btn_masuk.setOnClickListener {
            startActivity<LoginActivity>()
        }

        btn_daftar.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }
}