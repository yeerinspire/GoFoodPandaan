package com.example.gofoodpandaan.Auth

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var mAuth: FirebaseAuth? = null
    private var imageUri: Uri? = null
    private var myUrl = ""
    lateinit var databaseReference: DatabaseReference
    private var mStorage: StorageReference? = null
    private val PICK_IMAGE = 1
    lateinit var progressDialog: ProgressDialog
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        sessionManager = SessionManager(this)
        imageUri = null
        progressDialog = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        auth = FirebaseAuth.getInstance()
        btn_lanjut.setOnClickListener {
            daftar()
        }
        imageView11.setOnClickListener {
            startActivity<AuthActivity>()
        }
    }
    var notelp : String? = null
    private fun daftar() {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Sedang Login .....")
            progressDialog.show()
            val email = edt_email.text.toString().trim()
            val username = edt_namalengkap.text.toString().trim()
            val password = edt_password.text.toString().trim()
            val confirmPassword = edt_confirmpassword.text.toString().trim()
             notelp = edt_notelp.text.toString().trim()
            if (confirmPassword.trim() != password.trim()){
                toast("Password harus sama")
                progressDialog.dismiss()
            }
            else{
                if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    mAuth!!.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user_id = mAuth!!.currentUser!!.uid
                                        val downloadUrl = task.result
                                        myUrl = downloadUrl.toString()
                                        val usermap: MutableMap<String, Any?> = HashMap()
                                        usermap["email"] = email
                                        usermap["name"] = username
                                        usermap["password"] = password
                                        usermap["foto"] = myUrl
                                        usermap["telefon"] = notelp
                                        databaseReference =
                                            FirebaseDatabase.getInstance().getReference("Pandaan")
                                                .child("Costumers")
                                        databaseReference.child(user_id).setValue(usermap)
                                        progressDialog.dismiss()
                                        sendToMain()
                                }

                            else {
                                toast("gagal")
                                progressDialog.dismiss()

                            }

                        }


                }
                else {
                    toast("isikan form terlebih dahulu")
                    progressDialog.dismiss()

                }
            }


    }

    private fun sendToMain() {
        startActivity<HomeActivity>()
        sessionManager.setLogin(true)
        finish()
    }

}