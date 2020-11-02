package com.example.gofoodpandaan.Auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
    lateinit var sessionManager: SessionManager
    lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var ref : DatabaseReference
    lateinit var progressdialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        sessionManager = SessionManager(this)

        if (sessionManager.getLogin()!!){
                startActivity<HomeActivity>()
            finish()
        }

        txt_register.setOnClickListener {
            startActivity<RegisterActivity>()
        }

        btn_login.setOnClickListener {
            login()
        }
    }


    fun login() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Sedang Login .....")
        progressDialog.show()
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Udang").child("Users")
        var userss = username_input.text.toString()
        var password = pass.text.toString()


        if (!TextUtils.isEmpty(userss) && !TextUtils.isEmpty(password)) {
            if (userss.equals("udin@udin.com") && password.equals("udin123"))
            {
                auth.signInWithEmailAndPassword(userss, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            sessionManager.setLoginadmin(true)
                            startActivity<HomeActivity>()
                            progressDialog.dismiss()
                        }
                        else
                        {
                            toast("gagal login")

                        }
                    }

            }
            else if (!userss.equals("udin@udin.com") && !password.equals("udin123"))
            {
                auth.signInWithEmailAndPassword(userss, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            sessionManager.setLogin(true)
                            startActivity<HomeActivity>()
                            progressDialog.dismiss()
                            finish()
                        }
                        else
                        {
                            toast("gagal login")

                        }
                    }

            }



        }
        else {
            toast("masukkan username dan password")

        }

    }
}