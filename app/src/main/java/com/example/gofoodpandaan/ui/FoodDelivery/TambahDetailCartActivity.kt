package com.example.gofoodpandaan.ui.FoodDelivery

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.gofoodpandaan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tambah_detail_cart.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.HashMap

class TambahDetailCartActivity : AppCompatActivity() {
    var gambar: String? = null
    var nama: String? = null
    var harga: String? = null
    var nilai: Int? = null
    var kode:String? = null
    var jarak:String? = null
    var lattoko: String? = null
    var lontoko: String? = null
    var namapenjual: String? = null

    lateinit var userid: String
    lateinit var auth : FirebaseAuth
    lateinit var refinfo: DatabaseReference
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_detail_cart)
        val bundle: Bundle? = intent.extras
        gambar = bundle!!.getString("Firebase_Image")
        nama = bundle.getString("Firebase_title")
        harga = bundle.getString("firebase_harga")
        kode = bundle.getString("firebase_kode")
        lattoko = bundle.getString("Firebase_latakhir")
        lontoko = bundle.getString("Firebase_lonakhir")
        namapenjual = bundle.getString("firebase_penjual")

        auth = FirebaseAuth.getInstance()
        userid = auth.uid.toString()
        val progressDialog = ProgressDialog(this)


        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("keranjang").child(userid)
        cart()
        img_cart.setOnClickListener {
            startActivity<CartActivity>(  "Firebase_latakhir" to lattoko,
                "Firebase_lonakhir" to lontoko,
                "firebase_penjual" to namapenjual)
        }
        var counter = 0
        btn_down.setOnClickListener {
            if (counter <= 0) {
                counter == 0
            } else {
                counter = counter - 1
            }
            nilai = harga.toString().toInt() * counter
            txt_counter.text = counter.toString()
           hargatotal.text = "Rp. ${nilai.toString()}"

        }

        btn_up.setOnClickListener {
            counter += 1
            nilai = harga.toString().toInt() * counter
            txt_counter.text = counter.toString()
            hargatotal.text = "Rp. ${nilai.toString()}"
        }

        tambah.setOnClickListener {
            progressDialog.setTitle("Tunggu Sebentar....")
            progressDialog.show()
            val usermap: MutableMap<String, Any?> = HashMap()
            usermap["nama"] = nama
            usermap["harga"] = nilai.toString()
            usermap["jumlah"] = counter.toString()
            refinfo.child(nama.toString()).setValue(usermap).addOnCompleteListener {
                if (it.isComplete){
                    toast("berhasil")
                }
                else{
                    toast("Pastikan koneksi anda stabil")
                }
            }

            progressDialog.dismiss()

        }


        Picasso.get().load(gambar).into(fotocart)
        name.text = nama.toString()
        hargacart.text = "Rp. ${harga.toString()}"


    }


    private fun cart(){
        img_cart.visibility = View.INVISIBLE
        refinfo.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()){
                    img_cart.visibility = View.INVISIBLE
                }
                else{
                    img_cart.visibility = View.VISIBLE
                }
            }

        })
    }

}