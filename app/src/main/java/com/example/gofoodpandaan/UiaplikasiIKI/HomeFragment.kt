package com.example.gofoodpandaan.UiaplikasiIKI

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.example.gofoodpandaan.IkiOjek.IkiOjekActivity
import com.example.gofoodpandaan.IkiTaksi.IkiTaksiActivity
import com.example.gofoodpandaan.IkiWarung.IkiFoodActivity
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class HomeFragment : Fragment(),AnkoLogger {
    lateinit var buttonwarung : ImageView
    lateinit var buttonikiojek : ImageView
    lateinit var buttonikitaksi : ImageView
    lateinit var auth: FirebaseAuth
    lateinit var ref:DatabaseReference
    var userID : String? = null
    var nama : String? = null
    var statusrating : Boolean? = null
    companion object{
        var sheet = BottomRatingbar()

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
        progressDialog.show()
        buttonwarung = root.find(R.id.btn_ikiwarung)
        buttonikiojek = root.find(R.id.btn_ikiojek)
        buttonikitaksi = root.find(R.id.btn_ikitaksi)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        ref = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val data = p0.child(userID.toString()).getValue(ModelUsers::class.java)
                    nama = data!!.name.toString()
                    statusrating = data.statusrating
                    if (statusrating==true){
                        sheet.show(activity!!.supportFragmentManager, "BottomRatingbar")
                    }



                    if (nama!=null){
                        progressDialog.dismiss()
                        buttonikiojek.setOnClickListener {
                            startActivity<IkiOjekActivity>("namacostumer" to nama)
                        }
                        buttonikitaksi.setOnClickListener {
                            startActivity<IkiTaksiActivity>("namacostumer" to nama)
                        }

                    }

                }


            }

        })




        buttonwarung.setOnClickListener {
            startActivity<IkiFoodActivity>()
        }

        return  root
    }



    class BottomRatingbar : SuperBottomSheetFragment() {

        lateinit var ratingBar: RatingBar
        lateinit var auth: FirebaseAuth
        lateinit var ref :DatabaseReference
        var UserID : String? = null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            val root =  inflater.inflate(R.layout.rating_sheet_bottom, container, false)
            ratingBar = root.find(R.id.ratingBar)
            auth = FirebaseAuth.getInstance()
            UserID = auth.currentUser!!.uid
            ref = FirebaseDatabase.getInstance().getReference("Pandaan")
            ratingBar.setOnRatingBarChangeListener(object : RatingBar.OnRatingBarChangeListener{
                override fun onRatingChanged(
                    ratingBar: RatingBar?,
                    rating: Float,
                    fromUser: Boolean
                ) {
                        if (rating.toInt()==1){
                            ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                            ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                            sheet.dismiss()
                        }
                    else if (rating.toInt()==2){
                            ref.child("Costumers").child(UserID.toString()).child("statusrating").removeValue()
                            ref.child("Costumers").child(UserID.toString()).child("ratingdriver").removeValue()
                            sheet.dismiss()
                        }
                }

            })
            return root
        }

        override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)

        override fun getStatusBarColor() = Color.RED
        override fun isSheetAlwaysExpanded(): Boolean = true
    }

}