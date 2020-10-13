package com.example.gofoodpandaan

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gofoodpandaan.Model.DriverWorking
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.ui.FoodDelivery.chatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_tracking_order_ojek.*
import kotlinx.android.synthetic.main.sheet_orderan.*
import org.jetbrains.anko.startActivity
import java.util.*

class TrackingOrderOjekActivity : AppCompatActivity() {
     var peta: GoogleMap? = null
    private lateinit var database: DatabaseReference

    var latawal: String? = null
    var lngawal: String? = null
    var lattujuan: String? = null
    var lngtujuan: String? = null
    var kunci: String? = null
    var namadriver: String? = null
    var statusperjalanan: String? = null
    var uiddriver :String? = null
    var driverMarker: Marker? = null
    var gambar: String? = null
    lateinit var progressdialog: ProgressDialog
    lateinit var auth: FirebaseAuth
    var UserID :String? = null

    private fun ambildata(){
        val ref = FirebaseDatabase.getInstance().getReference("BookingOjek")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                uiddriver = p0.child(UserID.toString()).child("status").value.toString()

            }

        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order_ojek)
        progressdialog = ProgressDialog(this)
        val bundle: Bundle? = intent.extras
        kunci = bundle!!.getString("kunci")
        latawal = bundle.getString("latitudeawal")
        lngawal = bundle.getString("longitudeawal")
        lattujuan = bundle.getString("latitudetujuan")
        lngtujuan = bundle.getString("longitudetujuan")
        auth = FirebaseAuth.getInstance()
        UserID = auth.currentUser!!.uid
        maporder.onCreate(savedInstanceState)
        maporder.getMapAsync { googleMap ->
            peta = googleMap
            showMainMarker(latawal.toString().toDouble(), lngawal.toString().toDouble(), "posisiku")
            showTujuanMarker(
                lattujuan.toString().toDouble(),
                lngtujuan.toString().toDouble(),
                "Posisi Tujuan"
            )
            showbetweenmarker(latawal.toString().toDouble(), lngawal.toString().toDouble(),lattujuan.toString().toDouble(),lngtujuan.toString().toDouble(),"posisiku","posoi")
            route(latawal!!,lngawal!!,lattujuan!!,lngtujuan!!)
            ambildatadriver()

        }

        btn_chatojek.setOnClickListener {
            progressdialog.setTitle("sedang menghubungkan")
            progressdialog.show()
            ambildata()
            if (uiddriver!=null){
                progressdialog.dismiss()
                startActivity<chatActivity>("uid_driver" to uiddriver)
            }
            else{
                ambildata()
                if (uiddriver!=null){
                    progressdialog.dismiss()
                    startActivity<chatActivity>("uid_driver" to uiddriver)
                }

            }


        }


    }

    private fun ambildatadriver() {
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        val database2 = FirebaseDatabase.getInstance().getReference("BookingOjek").child(userID.toString())
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

                            val posisiDriver = LatLng(locationLat, locationLang)
                            driverMarker = if (driverMarker == null) {
                                peta?.addMarker(
                                    MarkerOptions().position(posisiDriver).title("lokasi Driver").icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderOjekActivity,
                                            R.mipmap.ic_car
                                        )
                                    )
                                )
                            } else {
                                driverMarker?.remove()
                                peta?.addMarker(
                                    MarkerOptions().position(posisiDriver).title("lokasi Driver").icon(
                                        bitmapDescriptorFromVector(
                                            this@TrackingOrderOjekActivity,
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
        database2.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var ambildata = snapshot.getValue(DriverWorking::class.java)
                val hargatotal = ambildata?.harga.toString()

                if (ambildata?.status.toString() == "selesai"){
                    val ref = FirebaseDatabase.getInstance().getReference("chat")
                    ref.child("user_messages").child(UserID.toString()).removeValue()
/*
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
*/
                    startActivity<HomeActivity>()
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
    fun showbetweenmarker(lat : Double, lng : Double, lattujuan : Double, lngtujuan : Double, namaawal : String, namatujuan : String){
        val marker1 =
            LatLng(java.lang.Double.valueOf(lat), java.lang.Double.valueOf(lng))
        val marker2 = LatLng(lattujuan, lngtujuan)

        val markersList: MutableList<Marker> = ArrayList()
        val youMarker: Marker =
            peta!!.addMarker(MarkerOptions().position(marker1).title(namaawal))
        val playerMarker: Marker =
            peta!!.addMarker(MarkerOptions().position(marker2).title(namatujuan))

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
    @SuppressLint("CheckResult")
    private fun route(latitude: String, longitude:String , latitudeTujuan : String, longitudeTujuan : String) {
        val origin = latitude.toString() + "," + longitude.toString()
        val dest = latitudeTujuan.toString() + "," + longitudeTujuan.toString()
        NetworkModule.getService()
            .actionRoute(origin, dest, "AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ t: ResultRoute? ->
                showData(t?.routes)
            },
                {})
    }

    private fun showData(routes: List<RoutesItem?>?) {


        if (routes != null) {

            val point = routes.get(0)?.overviewPolyline?.points
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val waktu = routes[0]?.legs?.get(0)?.duration?.text
            val pricex = jarakValue!!.toDouble().let { Math.round(it) }
            val price = pricex.div(1000.0).times(2000.0)
            peta?.let { point?.let { it1 -> DirectionMapsV2.gambarRoute(it, it1) } }
        }
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
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maporder?.onLowMemory()
    }


}