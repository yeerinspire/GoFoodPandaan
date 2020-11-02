package com.example.gofoodpandaan.IkiTaksi

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.directions.route.*
import com.example.gofoodpandaan.DirectionMapsV2
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.Model.DataDriver
import com.example.gofoodpandaan.Model.DriverWorking
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.chatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_tracking_order_taksi.maporder
import kotlinx.android.synthetic.main.sheet_orderan.*
import org.jetbrains.anko.*
import java.util.*

class TrackingOrderTaksiActivity : AppCompatActivity(), RoutingListener, AnkoLogger {
    var peta: GoogleMap? = null
    private lateinit var database: DatabaseReference

    var latawal: String? = null
    var lngawal: String? = null
    var lattujuan: String? = null
    var lngtujuan: String? = null
    var kunci: String? = null
    var namadriver: String? = null
    var statusperjalanan: String? = null
    var uiddriver: String? = null
    var driverMarker: Marker? = null
    var gambar: String? = null
    var kode: String? = null
    var harga: String? = null

    var nama: String? = null
    var foto: String? = null
    var motor: String? = null
    var platnomor: String? = null
    lateinit var imageview: ImageView

    lateinit var progressdialog: ProgressDialog
    lateinit var auth: FirebaseAuth
    var UserID: String? = null

    lateinit var sessionManager: SessionManager

    lateinit var refdatanyadriver : DatabaseReference
    lateinit var listenerdatanyadriver : ValueEventListener
    lateinit var refdatadriver : DatabaseReference
    lateinit var listenerdatadriver : ValueEventListener
    lateinit var refNotif : DatabaseReference
    lateinit var listenerNotif : ValueEventListener
    lateinit var refSelesai : DatabaseReference
    lateinit var listenerSelesai : ValueEventListener
    val REQUEST_PHONE_CALL = 1
    lateinit var refdatabookingOjek : DatabaseReference
    lateinit var listenerrefdatabookingOjek : ValueEventListener



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order_taksi)
        imageview = findViewById(R.id.img_fotodriver)
        progressdialog = ProgressDialog(this)
        sessionManager = SessionManager(this)
        val bundle: Bundle? = intent.extras
        kunci = bundle!!.getString("kunci")
        latawal = bundle.getString("latitudeawal")
        lngawal = bundle.getString("longitudeawal")
        lattujuan = bundle.getString("latitudetujuan")
        lngtujuan = bundle.getString("longitudetujuan")
        kode = bundle.getString("kode")
        harga = bundle.getString("harga")
        auth = FirebaseAuth.getInstance()
        UserID = auth.currentUser!!.uid
        maporder.onCreate(savedInstanceState)
        maporder.getMapAsync { googleMap ->
            peta = googleMap
            peta!!.isMyLocationEnabled = true
            peta!!.uiSettings.isMyLocationButtonEnabled = true
            peta!!.uiSettings.isCompassEnabled = true
            showTujuanMarker(
                lattujuan.toString().toDouble(),
                lngtujuan.toString().toDouble(),
                "Posisi Tujuan"
            )
            showbetweenmarker(
                latawal.toString().toDouble(),
                lngawal.toString().toDouble(),
                lattujuan.toString().toDouble(),
                lngtujuan.toString().toDouble(),
                "posisiku",
                "posoi"
            )
            Findroutes(
                LatLng(
                    latawal!!.toDouble(),
                    lngawal!!.toDouble()
                ), LatLng(
                    lattujuan!!.toDouble(),
                    lngtujuan!!.toDouble()
                )
            )
            ambildatanyadriver()
            ambildatadriver()
            ambildataBookingOjek()
        }

        txt_tunai.text = harga.toString()
        gambarnotif()
        btn_chatojek.setOnClickListener {
            progressdialog.setTitle("sedang menghubungkan")
            progressdialog.show()
            progressdialog.dismiss()
            startActivity<chatActivity>("uid_driver" to kunci,"nama_driver" to nama,"platdriver" to platnomor)
        }

        btn_cancel.setOnClickListener {
            progressdialog.setTitle("Tunggu sebentar")
            progressdialog.show()
            progressdialog.setCanceledOnTouchOutside(false)
            val ambiluiddriver = FirebaseDatabase.getInstance().reference.child("BookingOjek")
                .child(UserID.toString())
            ambiluiddriver.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val ambildata = p0.child("status").value.toString()

                    val db: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Pandaan")
                    val ambiluiddriver =
                        FirebaseDatabase.getInstance().reference.child("BookingOjek")
                            .child(UserID.toString()).removeValue()
                    progressdialog.dismiss()
                    val hapuspesanan =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(UserID.toString()).child(kode!!).removeValue().addOnCompleteListener {
                                if (it.isSuccessful){
                                    val hapusstatus = FirebaseDatabase.getInstance().reference.child("Pandaan")
                                        .child("Akun_Driver").child(ambildata).child("statusOjek").removeValue()
                                    val ref4: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")
                                    ref4.child("latest-messages").child(UserID.toString())
                                        .removeValue()
                                    ref4.child("user-messages").child(UserID.toString())
                                        .removeValue()
                                    ref4.child("user-messages").child(uiddriver.toString())
                                        .removeValue()

                                    startActivity(intentFor<HomeActivity>().clearTask().newTask())
                                    finish()
                                }
                            }
                }

            })


        }


    }
    fun ambildatanyadriver() {
        refdatanyadriver = FirebaseDatabase.getInstance().getReference("Pandaan").child("Akun_Driver")
        listenerdatanyadriver = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val data = p0.child(kunci.toString()).getValue(DataDriver::class.java)
                var notelp = ""
                nama = data!!.nama.toString()
                foto = data.foto.toString()
                motor = data.motor.toString()
                platnomor = data.platnomor.toString()
                txt_namadriver.text = nama.toString()
                txt_namamotor.text = motor.toString()
                txt_noplat.text = platnomor.toString()
                notelp = data.notelp.toString()
                btn_telfonojek.setOnClickListener {
                    if (ActivityCompat.checkSelfPermission(
                            this@TrackingOrderTaksiActivity,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@TrackingOrderTaksiActivity,
                            arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL
                        )
                    } else {
                        startcall(notelp.toString())
                    }

                }

                Picasso.get().load(foto.toString()).resize(100, 100).into(imageview)

            }
        }
        refdatanyadriver.addValueEventListener(listenerdatanyadriver)
    }

    private fun ambildataBookingOjek(){
        refdatabookingOjek = FirebaseDatabase.getInstance().reference.child("BookingOjek").child(UserID.toString())
        listenerrefdatabookingOjek = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var ambildata = p0.getValue(DriverWorking::class.java)
                var jarak = ambildata!!.jarak.toString()
                var harga = ambildata.harga.toString()
                txt_jarak.text = "Jarak = $jarak"
                txt_tunai.text = "Rp. $harga"
                txt_harga.text = "Rp. $harga"

            }

        }
        refdatabookingOjek.addListenerForSingleValueEvent(listenerrefdatabookingOjek)

    }
    private fun startcall(telepon: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + telepon)
        startActivity(callIntent)
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
                                            this@TrackingOrderTaksiActivity,
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
                                            this@TrackingOrderTaksiActivity,
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
                    val refhapusbooking = FirebaseDatabase.getInstance().getReference("BookingOjek")
                        .child(UserID.toString()).removeValue()
                    val hapusdata =
                        FirebaseDatabase.getInstance().getReference("chat").child("statusnotif")
                            .child(UserID.toString()).removeValue()
                    val hapuspesanan =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(UserID.toString()).child(kode!!).removeValue()
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
        refSelesai.addListenerForSingleValueEvent(listenerSelesai)

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

    fun showTujuanMarker(lat: Double, lon: Double, msg: String) {

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

    fun showbetweenmarker(
        lat: Double,
        lng: Double,
        lattujuan: Double,
        lngtujuan: Double,
        namaawal: String,
        namatujuan: String
    ) {
        val marker1 =
            LatLng(java.lang.Double.valueOf(lat), java.lang.Double.valueOf(lng))
        val marker2 = LatLng(lattujuan, lngtujuan)

        val markersList: MutableList<Marker> = ArrayList()
        val youMarker: Marker =
            peta!!.addMarker(MarkerOptions().position(marker1).title(namaawal).visible(false))
        val playerMarker: Marker =
            peta!!.addMarker(MarkerOptions().position(marker2).title(namatujuan).visible(false))

        markersList.add(youMarker)
        markersList.add(playerMarker)

        val builder = LatLngBounds.Builder()
        for (m in markersList) {
            builder.include(m.position)
        }
        val padding = 50
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        peta!!.setOnMapLoadedCallback(OnMapLoadedCallback { //animate camera here
            peta!!.animateCamera(cu)
        })

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
        finish()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        maporder?.onLowMemory()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        refdatanyadriver.removeEventListener(listenerdatanyadriver)
        refdatadriver.removeEventListener(listenerdatadriver)
        refNotif.removeEventListener(listenerNotif)
        refSelesai.removeEventListener(listenerSelesai)
        startActivity(intentFor<HomeActivity>().clearTask().newTask())
        finish()
    }
    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            toast("tidak dapat mendapatkan aplikasi")
        } else {
            val routing: Routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE") //also define your api key here.
                .build()
            routing.execute()

        }
    }

    override fun onRoutingCancelled() {
        Findroutes(start, end)
    }

    override fun onRoutingStart() {
        toast("mencari route")
    }

    override fun onRoutingFailure(p0: RouteException?) {
        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, p0.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
//    Findroutes(start,end);
    }

    protected var start: LatLng? = null
    protected var end: LatLng? = null

    //polyline object
    private var polylines: List<Polyline>? = null

    override fun onRoutingSuccess(routes: ArrayList<Route>?, shortestRouteIndex: Int) {
        val center = CameraUpdateFactory.newLatLng(start)
        val zoom = CameraUpdateFactory.zoomTo(16F)

        val polyOptions = PolylineOptions()
        var polylineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        polylines = ArrayList()
        for (i in 0 until routes!!.size) {
            if (i == shortestRouteIndex) {
                polyOptions.color(Color.RED)
                polyOptions.width(7F)
                polyOptions.addAll(routes.get(shortestRouteIndex).points)
                val polyline = peta!!.addPolyline(polyOptions)
                polylineStartLatLng = polyline.points.get(0)
                val k = polyline.points.size
                polylineEndLatLng = polyline.points.get(k - 1)
                (polylines as ArrayList<Polyline>).add(polyline)
            } else {
            }
        }

    }


}