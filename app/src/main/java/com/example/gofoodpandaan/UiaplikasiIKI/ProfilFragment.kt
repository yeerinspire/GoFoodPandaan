package com.example.gofoodpandaan.UiaplikasiIKI

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.Auth.LoginActivity
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profil.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.startActivity

class ProfilFragment : Fragment() {
    lateinit var ref : DatabaseReference
    lateinit var auth: FirebaseAuth
    var UserID : String? = null
    lateinit var buttonkeluar : Button
    lateinit var sessionManager: SessionManager
    lateinit var progressDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_profil, container, false)
        buttonkeluar = root.find(R.id.btn_keluar)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Tunggu sebentar")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        sessionManager = SessionManager(context!!.applicationContext)
        auth = FirebaseAuth.getInstance()
        UserID = auth.currentUser!!.uid

        ref = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers").child(UserID.toString())
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                progressDialog.dismiss()
                val ambildata = p0.getValue(ModelUsers::class.java)
                txt_namaprofil.text = ambildata!!.name.toString()
                txt_notelp.text = ambildata.telefon.toString()
                txt_email.text = ambildata.email.toString()
                  }

        })

        buttonkeluar.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sessionManager.setLogin(false)
            startActivity<LoginActivity>()
            activity!!.finish()
        }



        return  root
    }


}