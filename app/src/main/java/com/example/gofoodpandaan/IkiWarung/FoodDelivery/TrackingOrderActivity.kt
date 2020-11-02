package com.example.gofoodpandaan.IkiWarung.FoodDelivery

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.IkiWarung.IkiFoodActivity
import com.example.gofoodpandaan.Model.DataDriver
import com.example.gofoodpandaan.Model.DriverWorking
import com.example.gofoodpandaan.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tracking_order.*
import kotlinx.android.synthetic.main.sheet_orderanwarung.*
import kotlinx.android.synthetic.main.sheet_orderanwarung.btn_cancel
import kotlinx.android.synthetic.main.sheet_orderanwarung.btn_chatojek
import kotlinx.android.synthetic.main.sheet_orderanwarung.txt_harga
import kotlinx.android.synthetic.main.sheet_orderanwarung.txt_jarak
import kotlinx.android.synthetic.main.sheet_orderanwarung.txt_namadriver
import kotlinx.android.synthetic.main.sheet_orderanwarung.txt_namamotor
import kotlinx.android.synthetic.main.sheet_orderanwarung.txt_noplat
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class TrackingOrderActivity : AppCompatActivity(), AnkoLogger {
    private lateinit var database: DatabaseReference
    private var peta: GoogleMap? = null
    var kunci: String? = null
    var latawal: String? = null
    var lngawal: String? = null
    var lattoko: String? = null
    var lngtoko: String? = null
    var gambar: String? = null
    var driverMarker: Marker? = null
    var namalokasiuser: String? = null
    var namapenjual: String? = null
    var jarak: String? = null
    var namadriver: String? = null
    var statusperjalanan: String? = null
    var harga: String? = null
    var status: String? = null
    var telfondriver: String? = null
    var keyy: String? = null
    var id: String? = null
    var latdriver :Double? = null
    var londriver :Double? = null
    var uiddriver :String? = null
    lateinit var progressdialog: ProgressDialog

    lateinit var auth: FirebaseAuth
    var UserID : String? = null
    lateinit var refdatanyadriver : DatabaseReference
    lateinit var listenerdatanyadriver : ValueEventListener
    lateinit var refdatadriver : DatabaseReference
    lateinit var listenerdatadriver : ValueEventListener
    lateinit var refNotif : DatabaseReference
    lateinit var listenerNotif : ValueEventListener
    lateinit var refSelesai : DatabaseReference
    lateinit var listenerSelesai : ValueEventListener

    lateinit var refdatabookingOjek : DatabaseReference
    lateinit var listenerrefdatabookingOjek : ValueEventListener

    var nama: String? = null
    var foto: String? = null
    var motor: String? = null
    var platnomor: String? = null
    lateinit var imageview: ImageView
    val REQUEST_PHONE_CALL = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)
        imageview = findViewById(R.id.img_fotodriver)
        progressdialog = ProgressDialog(this)
        val bundle: Bundle? = intent.extras
        kunci = bundle!!.getString("kunci")
        latawal = bundle.getString("latitudeawal")
        lngawal = bundle.getString("longitudeawal")
        lattoko = bundle.getString("latitudetoko")
        lngtoko = bundle.getString("longitudetoko")
        namalokasiuser = bundle.getString("alamat")
        namapenjual = bundle.getString("namapenjual")
        jarak = bundle.getString("jarak")
        harga = bundle.getString("harga")
        gambar = bundle.getString("gambar")
        id = bundle.getString("id")

        auth = FirebaseAuth.getInstance()
        UserID = auth.currentUser!!.uid




        maporder.onCreate(savedInstanceState)
        maporder.getMapAsync { googleMap ->
            peta = googleMap
            peta!!.isMyLocationEnabled = true
            peta!!.uiSettings.isMyLocationButtonEnabled = true
            peta!!.uiSettings.isCompassEnabled = true

            showTokoMarker(
                lattoko.toString().toDouble(),
                lngtoko.toString().toDouble(),
                "Posisi Toko"
            )

            ambildatadriver()
            ambildatanyadriver()
        }


        gambarnotif()
/*        btn_telfonojek.setOnClickListener {
            if (telfondriver!=null){
                val dial = "tel:$telfondriver"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            }
            else{
                return@setOnClickListener
                toast("masukan nomor telfon")
            }
        }*/

        btn_chatojek.setOnClickListener {
            progressdialog.setTitle("sedang menghubungkan")
            progressdialog.show()
            progressdialog.dismiss()
            startActivity<chatActivity>("uid_driver" to kunci)
        }

        btn_cancel.setOnClickListener {
            progressdialog.setTitle("Tunggu sebentar")
            progressdialog.show()
            progressdialog.setCanceledOnTouchOutside(false)
            val ambiluiddriver = FirebaseDatabase.getInstance().reference.child("Booking")
                .child(UserID.toString()).child(id.toString())
            ambiluiddriver.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val ambildata = p0.child("status").value.toString()

                    val db: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Pandaan")
                    val ambiluiddriver =
                        FirebaseDatabase.getInstance().reference.child("Booking")
                            .child(UserID.toString()).removeValue()
                    progressdialog.dismiss()
                    val hapuspesanan =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(UserID.toString()).child(UserID.toString()).removeValue().addOnCompleteListener {
                                if (it.isSuccessful){
                                    val hapusstatus = FirebaseDatabase.getInstance().reference.child("Pandaan")
                                        .child("Akun_Driver").child(ambildata).child("status").removeValue()
                                    val ref4: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")
                                    ref4.child("latest-messages").child(UserID.toString())
                                        .removeValue()
                                    ref4.child("user-messages").child(UserID.toString())
                                        .removeValue()
                                    ref4.child("user-messages").child(uiddriver.toString())
                                        .removeValue()
                                    val hapusdaftarpesanan =
                                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                                            .child(UserID.toString()).child(id.toString())
                                            .removeValue()
                                    val hapuskeranjang =
                                        FirebaseDatabase.getInstance().getReference("Pandaan").child("keranjang")
                                            .child(UserID.toString()).child(id.toString())
                                            .removeValue()


                                    startActivity(intentFor<HomeActivity>().clearTask().newTask())
                                    finish()
                                }
                            }
                }

            })


        }


        ambildataBookingOjek()

        /*     posisiku.text = namalokasiuser.toString()
             namatoko.text = namapenjual.toString()
             txt_tokho.text = namapenjual.toString()
             jarakketoko.text = jarak.toString()*/
    }

    private fun startcall(telepon: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + telepon)
        startActivity(callIntent)
    }
    private fun ambildataBookingOjek(){
        refdatabookingOjek = FirebaseDatabase.getInstance().reference.child("Booking").child(UserID.toString()).child(id.toString())
        listenerrefdatabookingOjek = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var ambildata = p0.getValue(DriverWorking::class.java)
                var jarak = ambildata!!.jarak.toString()
                var harga = ambildata.harga.toString()
                var hargapelanggan = ambildata.hargapelanggan.toString()
                var ongkir = ambildata.ongkir.toString()
                var namawarung = ambildata.namatoko.toString()
                var longitudewarung = ambildata.longitudeToko.toString()
                var latitudewarung = ambildata.latitudeToko.toString()
                var latitudepenumpang = ambildata.latitudePenumpang.toString()
                var longitudepenumpang = ambildata.longitudePenumpang.toString()
                txt_jarak.text = "Jarak = $jarak"
                txt_harga.text = "Rp. $harga"
                txt_hargawarung.text ="Rp. $hargapelanggan"
                txt_ongkir.text = "Rp. $ongkir"
                txt_tagihanwarung.text ="TAGIHAN TUNAI : Rp. $harga"
                txt_namawarung.text = namawarung
                detail_alamatwarung.text = showName(
                    latitudewarung.toDouble(),
                    longitudewarung.toDouble()
                )
                namatujuanwarung.text = showName(
                    latitudepenumpang.toDouble(),
                    longitudepenumpang.toDouble()
                )

            }

        }
        refdatabookingOjek.addListenerForSingleValueEvent(listenerrefdatabookingOjek)

    }
    fun gambarnotif() {
        refNotif = FirebaseDatabase.getInstance().getReference("chat").child("statusnotif")
            .child(UserID.toString())
        listenerNotif = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child("status").exists()) {
                    btn_chatojek.setBackgroundResource(R.drawable.kotakbiruovalsolidnotif)
                } else {
                    btn_chatojek.setBackgroundResource(R.drawable.kotakbiruovalsolid)
                }
            }

        }
        refNotif.addValueEventListener(listenerNotif)
    }

    fun ambildatanyadriver() {
        refdatanyadriver = FirebaseDatabase.getInstance().getReference("Pandaan").child("Akun_Driver")
        var notelp = ""
        listenerdatanyadriver = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val data = p0.child(kunci.toString()).getValue(DataDriver::class.java)
                nama = data!!.nama.toString()
                foto = data.foto.toString()
                motor = data.motor.toString()
                platnomor = data.platnomor.toString()
                txt_namadriver.text = nama.toString()
                txt_namamotor.text = motor.toString()
                txt_noplat.text = platnomor.toString()
                notelp = data.notelp.toString()
                Picasso.get().load(foto.toString()).resize(100, 100).into(imageview)

                btn_telfonojek.setOnClickListener {
                    if (ActivityCompat.checkSelfPermission(
                            this@TrackingOrderActivity,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@TrackingOrderActivity,
                            arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL
                        )
                    } else {
                        startcall(notelp)
                    }

                }


            }
        }
        refdatanyadriver.addValueEventListener(listenerdatanyadriver)
    }
    private fun ambildatadriver() {
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        refdatadriver = FirebaseDatabase.getInstance().getReference("Driver")
        listenerdatadriver = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val ambildata = p0.getValue(DriverWorking::class.java)
                    if (ambildata != null) {
                        namadriver = ambildata.driver.toString()
                        statusperjalanan = ambildata.statusPerjalanan.toString()
                        var locationLat = 0.0
                        var locationLang = 0.0
                        uiddriver = ambildata.uid.toString()
                        if (ambildata.latitudeDriver != null) {
                            locationLat = ambildata.latitudeDriver.toString().toDouble()
                        }
                        if (ambildata.longitudeDriver != null) {
                            locationLang = ambildata.longitudeDriver.toString().toDouble()
                        }

                        val posisiDriver = LatLng(locationLat, locationLang)
                        driverMarker = if (driverMarker == null) {
                            peta?.addMarker(
                                MarkerOptions().position(posisiDriver).title("lokasi Driver")
                                    .icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderActivity,
                                            R.drawable.driverojek
                                        )
                                    )
                            )
                        } else {
                            driverMarker?.remove()
                            peta?.addMarker(
                                MarkerOptions().position(posisiDriver).title("lokasi Driver")
                                    .icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderActivity,
                                            R.drawable.driverojek
                                        )
                                    )
                            )
                        }
                    } else {
                        ambildatadriver()
                    }
                }
                selesaiorder()
            }

        }

        refdatadriver.child("DriverSibuk").child(kunci.toString())
            .addValueEventListener(listenerdatadriver)


    }
    private fun selesaiorder(){
        refSelesai =
            FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers").child(UserID.toString())
        listenerSelesai = object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var ambildata = snapshot.getValue(DriverWorking::class.java)
                if (ambildata?.status.toString() == "selesai") {
                    val ref = FirebaseDatabase.getInstance().getReference("chat")
                    ref.child("user_messages").child(UserID.toString()).removeValue()
                    val refhapusbooking = FirebaseDatabase.getInstance().getReference("Booking")
                        .child(UserID.toString()).removeValue()
                    val hapusdata =
                        FirebaseDatabase.getInstance().getReference("chat").child("statusnotif")
                            .child(UserID.toString()).removeValue()
                    val hapuspesanan =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(UserID.toString()).child(UserID.toString()).removeValue()
                    val ref4: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")
                    ref4.child("latest-messages").child(UserID.toString())
                        .removeValue()
                    ref4.child("user-messages").child(UserID.toString())
                        .removeValue()
                    ref4.child("user-messages").child(uiddriver.toString())
                        .removeValue()
                    val hapusstatuscancel = FirebaseDatabase.getInstance().getReference("Pandaan").child("Costumers")
                        .child(UserID.toString()).child("status").removeValue()
                    startActivity(intentFor<HomeActivity>().clearTask().newTask())
                    finish()
                }
            }


        }
        refSelesai.addValueEventListener(listenerSelesai)

    }




    fun showTokoMarker(lat: Double, lon: Double, msg: String) {

        val res = this.resources
//        val marker1 = BitmapFactory.decodeResource(res, R.mipmap)
//        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)

        val coordinate = LatLng(lat, lon)
        peta!!.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        val cameraPosition =
            CameraPosition.Builder().target(coordinate).zoom(15f).build()
        peta!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        peta!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15f))
    }
    fun showDriverMarker(lat: Double, lon: Double, msg: String) {

        val res = this.resources
//        val marker1 = BitmapFactory.decodeResource(res, R.mipmap)
//        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)

        val coordinate = LatLng(lat, lon)
        peta!!.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle))
        )
        val cameraPosition =
            CameraPosition.Builder().target(coordinate).zoom(15f).build()
        peta!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        peta!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15f))
    }
    fun showName(lat: Double, lon: Double): String {
        var name = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses.size > 0) {
                val fetchedAddress = addresses.get(0)
                val strAddress = StringBuilder()
                for (i in 0..fetchedAddress.maxAddressLineIndex) {
                    name =
                        "Jl. ${fetchedAddress.featureName + ",${fetchedAddress.subLocality}"}, ${fetchedAddress.locality} "

                }
            }
        } catch (e: Exception) {

        }
        return name
    }

    override fun onResume() {
        super.onResume()
        maporder?.onResume()
    }

    override fun onStart() {
        super.onStart()
        maporder?.onStart()
    }

    override fun onStop() {
        super.onStop()
        maporder?.onStop()
    }

    override fun onPause() {
        maporder?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        maporder?.onDestroy()
        refdatanyadriver.removeEventListener(listenerdatanyadriver)
        refdatadriver.removeEventListener(listenerdatadriver)
        refNotif.removeEventListener(listenerNotif)
        refSelesai.removeEventListener(listenerSelesai)

        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maporder?.onLowMemory()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(intentFor<HomeActivity>().clearTask().newTask())
        finish()
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            this.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

}