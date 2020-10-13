package com.example.gofoodpandaan.Auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.android.synthetic.main.activity_o_t_p.*
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {
    var notelp: String? = null


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_o_t_p)

        val bundle: Bundle? = intent.extras
        notelp = bundle!!.getString("notelp")
        sendVerificationCodeToUser(notelp.toString())

            verify_btn.setOnClickListener(View.OnClickListener {
                val code: String = verification_code_entered_by_user.getText().toString()
                if (code.isEmpty() || code.length < 6) {
                    verification_code_entered_by_user.setError("Wrong OTP...")
                    verification_code_entered_by_user.requestFocus()
                    return@OnClickListener
                }
                progress_bar.setVisibility(View.VISIBLE)
                verifyCode(code)
            })
    }

    private fun sendVerificationCodeToUser(phoneNo: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+62" +phoneNo, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            TaskExecutors.MAIN_THREAD, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks


    }

    var verifikasikode : String? = null

    private var callbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String?, forceResendingToken: ForceResendingToken?) {
                super.onCodeSent(s, forceResendingToken)
                verifikasikode = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code!=null){
                    progress_bar.visibility = View.VISIBLE
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@OTPActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    private fun verifyCode(codebyUser: String) {
        val credential = PhoneAuthProvider.getCredential(verifikasikode!!,codebyUser)
        masukdengancredential(credential)
    }

    private fun masukdengancredential(credential: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this@OTPActivity,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@OTPActivity,
                            "Your Account has been created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        //Perform Your required action here to either let the user sign In or do something required
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@OTPActivity,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }


}