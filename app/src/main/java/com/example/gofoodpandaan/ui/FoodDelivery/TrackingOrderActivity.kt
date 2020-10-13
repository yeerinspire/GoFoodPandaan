package com.example.gofoodpandaan.ui.FoodDelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gofoodpandaan.IkiFoodActivity
import com.example.gofoodpandaan.Model.DriverWorking
import com.example.gofoodpandaan.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tracking_order.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)
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




        map.onCreate(savedInstanceState)
        map.getMapAsync { googleMap ->
            peta = googleMap
            showMainMarker(latawal.toString().toDouble(), lngawal.toString().toDouble(), "posisiku")
            showTokoMarker(
                lattoko.toString().toDouble(),
                lngtoko.toString().toDouble(),
                "Posisi Toko"
            )

            ambildatadriver()
        }


        telfon_ojek.setOnClickListener {
            if (telfondriver!=null){
                val dial = "tel:$telfondriver"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            }
            else{
                return@setOnClickListener
                toast("masukan nomor telfon")
            }
        }

        btn_chat.setOnClickListener {
            if (uiddriver!=null){
                startActivity<chatActivity>("uid_driver" to uiddriver)
            }
            return@setOnClickListener
        }


        posisiku.text = namalokasiuser.toString()
        namatoko.text = namapenjual.toString()
        txt_tokho.text = namapenjual.toString()
        jarakketoko.text = jarak.toString()
    }


    private fun ambildatadriver() {
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        val database2 = FirebaseDatabase.getInstance().getReference("Booking").child(userID.toString())
        database = FirebaseDatabase.getInstance().getReference("Driver")
        database.child("DriverSibuk").child(kunci.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val ambildata = p0.getValue(DriverWorking::class.java)
                        if (ambildata!=null){
                            namadriver = ambildata.driver.toString()
                            statusperjalanan = ambildata.statusPerjalanan.toString()
                            var locationLat = 0.0
                            var locationLang = 0.0
                            uiddriver = ambildata.uid.toString()
                            if (ambildata.latitudeDriver!=null){
                                locationLat = ambildata.latitudeDriver.toString().toDouble()
                            }
                            if (ambildata.longitudeDriver!=null){
                                locationLang = ambildata.longitudeDriver.toString().toDouble()
                            }

                                txt_statusperjalanan.text = statusperjalanan
                            val posisiDriver = LatLng(locationLat, locationLang)
                            driverMarker = if (driverMarker == null) {
                                peta?.addMarker(
                                    MarkerOptions().position(posisiDriver).title("lokasi Driver").icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderActivity,
                                            R.mipmap.ic_car
                                        )
                                    )
                                )
                            } else {
                                driverMarker?.remove()
                                peta?.addMarker(
                                    MarkerOptions().position(posisiDriver).title("lokasi Driver").icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderActivity,
                                            R.mipmap.ic_car
                                        )
                                    )
                                )
                            }


                        }
                        else{
                            ambildatadriver()
                        }

                    }
                }

            })
        database2.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var ambildata = snapshot.getValue(DriverWorking::class.java)
                val hargatotal = ambildata?.harga.toString()
                txt_harga.text = "Harga : Rp. $hargatotal"

                if (ambildata?.status.toString() == "selesai"){
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                   val refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("HistoryCostumer").child(userID.toString())
                    val usermap: HashMap<String, Any?> = HashMap()
                    usermap["penumpang"] = ambildata?.penumpang.toString()
                    usermap["harga"] = ambildata?.harga.toString()
                    usermap["namatoko"] = ambildata?.namatoko.toString()
                    usermap["ongkir"] = ambildata?.ongkir.toString()
                    usermap["calendar"] = currentDate.toString()
                    usermap["gambar"] = gambar.toString()
                    refinfo.setValue(usermap)
                    startActivity<IkiFoodActivity>()
                    finish()
                }
            }

        })


    }


    fun showMainMarker(lat: Double, lon: Double, msg: String) {

        val res = this.resources
//        val marker1 = BitmapFactory.decodeResource(res, R.mipmap)
//        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)

        val coordinate = LatLng(lat, lon)
        peta!!.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )
        val cameraPosition =
            CameraPosition.Builder().target(coordinate).zoom(15f).build()
        peta!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        peta!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15f))
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


    override fun onResume() {
        super.onResume()
        map?.onResume()
    }

    override fun onStart() {
        super.onStart()
        map?.onStart()
    }

    override fun onStop() {
        super.onStop()
        map?.onStop()
    }

    override fun onPause() {
        map?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        map?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map?.onLowMemory()
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