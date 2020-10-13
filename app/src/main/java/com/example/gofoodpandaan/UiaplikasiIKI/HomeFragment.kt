package com.example.gofoodpandaan.UiaplikasiIKI

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.example.gofoodpandaan.IkiFoodActivity
import com.example.gofoodpandaan.IkiOjekActivity
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity

class HomeFragment : Fragment(),AnkoLogger {
    lateinit var buttonwarung : ImageView
    lateinit var buttonikiojek : ImageView
    lateinit var auth: FirebaseAuth
    lateinit var ref:DatabaseReference
    var userID : String? = null
    var nama : String? = null
    lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_home, container, false)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Tunggu Sebentar")
        progressDialog.show()
        buttonwarung = root.find(R.id.btn_ikiwarung)
        buttonikiojek = root.find(R.id.btn_ikiojek)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        ref = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val data = p0.child(userID.toString()).getValue(ModelUsers::class.java)
                nama = data!!.name.toString()
                info { "yeri $nama" }
                if (nama!=null){
                    progressDialog.dismiss()
                    buttonikiojek.setOnClickListener {
                        startActivity<IkiOjekActivity>("namacostumer" to nama)
                    }

                }


            }

        })



        buttonwarung.setOnClickListener {
            startActivity<IkiFoodActivity>()
        }



        return  root

    }
}