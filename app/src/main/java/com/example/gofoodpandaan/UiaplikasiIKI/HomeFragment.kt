package com.example.gofoodpandaan.UiaplikasiIKI

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.example.gofoodpandaan.IkiOjek.IkiOjekActivity
import com.example.gofoodpandaan.IkiOjek.TrackingOrderOjekActivity
import com.example.gofoodpandaan.IkiTaksi.IkiTaksiActivity
import com.example.gofoodpandaan.IkiWarung.IkiFoodActivity
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.rating_sheet_bottom.*
import kotlinx.coroutines.delay
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class HomeFragment : Fragment(),AnkoLogger {
    lateinit var buttonwarung : ImageView
    lateinit var buttonikiojek : ImageView
    lateinit var buttonikitaksi : ImageView
    lateinit var auth: FirebaseAuth
    lateinit var ref:DatabaseReference
    lateinit var ref2 : DatabaseReference
    lateinit var refratingbar : DatabaseReference
    lateinit var ratingbarlistener : ValueEventListener
    lateinit var reflistener : ValueEventListener
    lateinit var search : MaterialSearchBar
    var userID : String? = null
    var nama : String? = null
    var telefon : String? = null
    var statusrating : Boolean? = null
    lateinit var sessionManager: SessionManager
    companion object{
        var sheet = BottomRatingbar()
        var logicsheet = 0
        lateinit var uidriver:String
        lateinit var harga:String
    }

    lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_home, container, false)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Tunggu Sebentar")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        buttonwarung = root.find(R.id.btn_ikiwarung)
        buttonikiojek = root.find(R.id.btn_ikiojek)
        buttonikitaksi = root.find(R.id.btn_ikitaksi)
        search = root.find(R.id.materialSearchBar)
        sessionManager = SessionManager(context!!.applicationContext)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        ref2 = FirebaseDatabase.getInstance().reference.child("DaftarBooking").child(userID.toString())
        ref = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers")
        reflistener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val data = p0.getValue(ModelUsers::class.java)
                    nama = data!!.name.toString()
                    telefon = data.telefon.toString()
                    statusrating = data.statusrating
                    sessionManager.settelefon(telefon.toString())
                    if (nama!=null){
                        progressDialog.dismiss()
                        buttonikiojek.setOnClickListener {
                            ambildataorder()
                        }
                        buttonikitaksi.setOnClickListener {
                            startActivity<IkiTaksiActivity>("namacostumer" to nama)
                        }
                    }
                }
            }
        }
        ref.child(userID.toString()).addValueEventListener(reflistener)

        buttonwarung.setOnClickListener {
            startActivity<IkiFoodActivity>()
        }

        search.setOnClickListener {
            startActivity<SearchActivity>()
        }

        return  root
    }

    override fun onResume() {
        super.onResume()

        ratingbar()

    }


    fun ratingbar(){
         refratingbar = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers").child(userID.toString())
        ratingbarlistener = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child("ratingdriver").exists()){
                    uidriver = p0.child("ratingdriver").value.toString()
                    harga = p0.child("harga").value.toString()
                    sheet.show(activity!!.supportFragmentManager, "BottomRatingbar")

                }
            }

        }
        refratingbar.addListenerForSingleValueEvent(ratingbarlistener)
        return
    }
    val listener : ValueEventListener = object :ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
            TODO("Not yet implemented")
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()){
                toast("Silahkan ke menu Pesanan untuk melihat order Ojek anda")
            }
            else{
                startActivity<IkiOjekActivity>("namacostumer" to nama)
            }
        }
    }

    private fun ambildataorder(){
        val query3 =
            FirebaseDatabase.getInstance().reference.child("DaftarBooking").child(userID.toString())
                .orderByChild("status")
                .equalTo("ojek")
      query3.addListenerForSingleValueEvent(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ref2.removeEventListener(listener)
        ref.removeEventListener(reflistener)
    }




    class BottomRatingbar : SuperBottomSheetFragment() {

        lateinit var ratingBar: RatingBar
        lateinit var auth: FirebaseAuth
        lateinit var ref :DatabaseReference
        lateinit var komentar : EditText
        lateinit var ref2 :DatabaseReference
        lateinit var text_harga : TextView
        lateinit var butonkomentar : Button
        lateinit var sessionManager: SessionManager
        var UserID : String? = null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            val root =  inflater.inflate(R.layout.rating_sheet_bottom, container, false)
            sessionManager = SessionManager(context!!.applicationContext)
            ratingBar = root.find(R.id.ratingBar)
            text_harga = root.find(R.id.hargarating)
            komentar = root.find(R.id.edt_komentar)
            butonkomentar = root.find(R.id.btn_inputkomen)
            val calendar = SimpleDateFormat("dd/M/yyyy")
            val currentDate = calendar.format(Date())
            val key = FirebaseDatabase.getInstance().getReference("Driver").child("Rating_Driver").child(
                uidriver.toString()).push().key
            auth = FirebaseAuth.getInstance()
            UserID = auth.currentUser!!.uid
            ref = FirebaseDatabase.getInstance().getReference("Pandaan")
            ref2 = FirebaseDatabase.getInstance().getReference("Driver")
            val hashMap : HashMap<String,Any?> = HashMap()
            hashMap["rating"] = 0.0
            hashMap["komentar"] = ""
            hashMap["calendar"] = currentDate
            ref2.child("Rating_Driver").child(uidriver).child(key.toString()).setValue(hashMap)
            var datakomentar = komentar.text.toString()
            text_harga.text = "Rp. $harga"
            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating.toInt()==1){
                    ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("rating").setValue(rating)
                    ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                    ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                } else if (rating.toInt()==2){
                    ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("rating").setValue(rating)
                    ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                    ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                }
                else if (rating.toInt()==3){
                    ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("rating").setValue(rating)
                    ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                    ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                }
                else if (rating.toInt()==4){
                    ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("rating").setValue(rating)
                    ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                    ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                }
                else if (rating.toInt()==5){
                    ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("rating").setValue(rating)
                    ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                    ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                }
            }

            butonkomentar.setOnClickListener {
                ref2.child("Rating_Driver").child(uidriver).child(key.toString()).child("komentar").setValue(komentar.text.toString())
                sheet.dismiss()
                toast("Terimakasih!!")
            }

            return root
        }

        override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)

        override fun getStatusBarColor() = Color.RED
        override fun isSheetAlwaysExpanded(): Boolean = true
    }

}