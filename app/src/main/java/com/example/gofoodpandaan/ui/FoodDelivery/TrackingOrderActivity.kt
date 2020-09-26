package com.example.gofoodpandaan.ui.FoodDelivery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.gofoodpandaan.Booking
import com.example.gofoodpandaan.Model.Driver
import com.example.gofoodpandaan.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_tracking_order.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

class TrackingOrderActivity : AppCompatActivity(),AnkoLogger {
        private lateinit var database : DatabaseReference
    private var peta: GoogleMap? = null
     var kunci : String? = null
    var latawal : String? = null
    var lngawal : String? = null
    var lattoko : String? = null
    var lngtoko : String? = null
    var driverMarker: Marker? = null
    var namalokasiuser: String? = null
    var namapenjual: String? = null
    var jarak: String? = null

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



        map.onCreate(savedInstanceState)
        map.getMapAsync{
            googleMap ->
            peta = googleMap
            showMainMarker(latawal.toString().toDouble(),lngawal.toString().toDouble(),"posisiku")
            showTokoMarker(lattoko.toString().toDouble(),lngtoko.toString().toDouble(),"Posisi Toko")

            ambildatadriver()
        }
        posisiku.text = namalokasiuser.toString()
        namatoko.text = namapenjual.toString()
        jarakketoko.text = jarak.toString()
    }

    private fun ambildatadriver() {
        database = FirebaseDatabase.getInstance().reference.child("DriversWorking").child(kunci!!).child("l")

        database.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val map = p0.value as(List<*>)
                    var locationLat = 0.0
                    var locationLang = 0.0
                    if (map[0] != null){
                        locationLat = map[0].toString().toDouble()
                    }

                    if (map[1] != null){
                        locationLang = map[1].toString().toDouble()
                    }
                    val posisiDriver = LatLng(locationLat,locationLang)
                    driverMarker = if (driverMarker == null){
                        peta?.addMarker(MarkerOptions().position(posisiDriver).title("lokasi Driver").icon(bitmapDescriptorFromVector(this@TrackingOrderActivity, R.mipmap.ic_car)))
                    } else {
                        driverMarker?.remove()
                        peta?.addMarker(MarkerOptions().position(posisiDriver).title("Driver Location").icon(bitmapDescriptorFromVector(this@TrackingOrderActivity, R.mipmap.ic_car)))
                    }


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
        peta!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate,15f))
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
        peta!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate,15f))
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
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

}