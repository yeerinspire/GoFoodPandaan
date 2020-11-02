package com.example.gofoodpandaan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_riwayat_ojek.*
import org.jetbrains.anko.info

class DetailRiwayatOjekActivity : AppCompatActivity() {

    var kode : String? = null
    var namawarung : String? = null
    var statuswarung : String? = null
    var alamatwarung : String? = null
    var alamatacostumer : String? = null
    var uiddriver : String? = null
    var harga : String? = null
    lateinit var ref : DatabaseReference
    lateinit var eventref : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_riwayat_ojek)
        val bundle: Bundle? = intent.extras
        kode = bundle!!.getString("kode")
        namawarung = bundle.getString("namawarung")
        statuswarung = bundle.getString("status")
        alamatwarung = bundle.getString("alamattujuan")
        alamatacostumer = bundle.getString("alamatcostumer")
        uiddriver = bundle.getString("uiddriver")
        harga = bundle.getString("harga")

        kodeorder.text = "Kode Order : $kode"
        status.text = "Order IKI-$statuswarung"
        txt_namawarung.text = namawarung.toString()
        detail_alamatwarung.text = alamatwarung.toString()
        namatujuanwarung.text = alamatacostumer.toString()
        txt_harga.text = harga.toString()


        ref = FirebaseDatabase.getInstance().getReference("Pandaan")
        eventref = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var foto =p0.child("Akun_Driver").child(uiddriver.toString()).child("foto").value.toString()
                var nama = p0.child("Akun_Driver").child(uiddriver.toString()).child("nama").value.toString()
                var platnomor = p0.child("Akun_Driver").child(uiddriver.toString()).child("platnomor").value.toString()
                Picasso.get().load(foto).fit().into(fotodriver)
                txt_namadriver.text = nama
                txt_platnomor.text = platnomor
            }

        }
        ref.addListenerForSingleValueEvent(eventref)





    }
}