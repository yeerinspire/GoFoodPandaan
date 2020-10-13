package com.example.gofoodpandaan.ui.FoodDelivery

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.gofoodpandaan.*
import com.example.gofoodpandaan.Model.ModelOrder
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.Model.RequestNotification
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cart.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONException
import org.json.JSONObject
import java.sql.DriverManager.getDriver
import java.util.*
import kotlin.collections.HashMap

@Suppress("NAME_SHADOWING")
class CartActivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback,
    GoogleMap.OnMapClickListener {
    lateinit var refinfo: DatabaseReference
    var hargatotal = 0
    var userID: String? = null
    var tanggal: String? = null
    var latAwal: Double? = null
    var lonAwal: Double? = null
    var latAkhir: Double? = null
    var lonAkhir: Double? = null
    var jarak: String? = null
    var waktufix: String? = null
    var dialog: Dialog? = null
    var lattoko: String? = null
    var lontoko: String? = null
    var uang: Int? = null
    var keyy: String? = null
    var gambar: String? = null
    var namapenjual: String? = null
    var namaCostumer: String? = null
    private var auth: FirebaseAuth? = null
    var lokasifix: String? = null
    private var requestBol = false
    private lateinit var lastLocation: Location
    private lateinit var geoQuery: GeoQuery
    private var radius: Double = 1.0
    private var driverFound = false
    var driverID: String? = null
    private lateinit var driverLocationRef: DatabaseReference
    private lateinit var driverLocationRefListener: ValueEventListener
    var namalokasi: String? = null
    var hargaongkir = 0
    var id: String? = null

    companion object {
        private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private val markerIconSize = 90
    }

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAA1B1Ef7o:APA91bFzLzKZ3jrez02VjjiQ4sGDoUwDc6TyAph-8pfzfc-ifchkr8iYnFkpjXCK08LJhros4EbSvsvDdvzhy1fX5gNNGuqDIhQR8_vend8V6UEWh4LkMeNwYZy2-EM8izC2xLbrF0v2"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private var map: GoogleMap? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        maps.onCreate(savedInstanceState)
        maps.getMapAsync(this)
        showPermission()
        if (sum!=null){
            rv_2.visibility = View.INVISIBLE
        }
        else{
            rv_2.visibility = View.VISIBLE

        }
        rv_2.visibility = View.INVISIBLE
        maps.setOnClickListener {
            toast("halo")
        }
        keyy?.let { bookingHistoryUser(it) }

        val bundle: Bundle? = intent.extras
        lattoko = bundle!!.getString("Firebase_latakhir")
        lontoko = bundle.getString("Firebase_lonakhir")
        namapenjual = bundle.getString("firebase_penjual")
        namaCostumer = bundle.getString("firebase_namaCostumer")
        gambar = bundle.getString("Firebase_gambarMakanan")
        id = bundle.getString("firebase_id")
        latAkhir = lattoko.toString().toDouble()
        lonAkhir = lontoko.toString().toDouble()
        route()
        setVisible(false)
        val auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        homeAwal.onClick {
            rv1.visibility = View.INVISIBLE
            rv_2.visibility = View.VISIBLE
        }

        txt_lokasitoko.setOnClickListener {
            takeLocation(1)
        }

        btn_proses.setOnClickListener {
            if (latAwal != null && lonAwal != null && namalokasi != null) {
                when (requestBol) {
                    false -> {
                        requestBol = true
                        insertServer()


                    }
                }

            } else {
                toast("masukan lokasi terlebih dahulu")
            }

        }

        btn_konfirmlokasi.setOnClickListener {
            if (namalokasi != null && namalokasi != null) {
                rv1.visibility = View.VISIBLE
                rv_2.visibility = View.INVISIBLE
                alamat.text = namalokasi
            } else {
                toast("masukkan maps")
            }

        }

        drivercancel(userID.toString())


        foodpopular()
        if (driverID != null) {
            toast("hallo")
        }
    }


    private fun bookingHistoryUser(key: String) {

        showDialog(true)


        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("Booking")

        myRef.child(key).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val booking = p0.getValue(ModelOrder::class.java)
                if (booking?.driver != "") {
                    startActivity<TrackingOrderActivity>("key" to key)
                    showDialog(false)


                }


            }

        })


    }

    private fun showDialog(status: Boolean) {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.tunggudriver)

        if (status) {


            dialog?.show()

        } else dialog?.dismiss()


    }

/*
    private fun pushNotif(booking: Booking) {



        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Driver")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                for (issue in p0.children) {

                    val token = issue.child("token").getValue(String::class.java)!!
                    val request = RequestNotificaton()
                    request.token = token
                    request.sendNotificationModel = booking

                    NetworkModule.getServiceFcm().sendChatNotification(request)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                                Log.d("network failed : ", t.message)
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                response.body()
                                Log.d("response server:", response.message())
                            }
                        })
                }


            }
        })


    }
*/

    private fun drivercancel(userid : String){
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Booking")
        ref.addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val status = p0.child(userid).child("status")
                if (status.equals("cancel")){
                    insertServer()
                }
                  }

        })

    }
    var sum : Int? = 0
    var alfan = 0
    private fun foodpopular() {
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_cart.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("keranjang")
            .child(userID.toString())
        val option =
            FirebaseRecyclerOptions.Builder<ModelUsers>().setQuery(refinfo, ModelUsers::class.java)
                .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val itemView = LayoutInflater.from(this@CartActivity)
                        .inflate(R.layout.card_view, parent, false)
                    return MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    alfan = ((Integer.valueOf(model.harga))) * ((Integer.valueOf(model.jumlah)))
                    sum = sum!! + alfan
                    hargamakanan.text = sum.toString()
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var tipeproduk =
                                model.harga.toString().toInt() * model.jumlah.toString().toInt()
//                            hargatotal += tipeproduk + uang!!
                            holder.mtitle.setText(model.nama)
                            holder.mprice.setText(model.harga)
                            holder.mOwner.setText(model.jumlah)

                            txt_hargatotal.text = hargatotal.toString()
                            /*        holder.mprice.setText(model.harga.toString())
                                    holder.mOwner.setText(model.jumlah.toString())
                           */         holder.itemView.setOnClickListener {
/*
                                startActivity<DetailFoodActivity>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "firebase_id" to model.id,
                                    "firebase_harga" to model.harga,
                                    "firebase_penjual" to model.penjual
                                )
*/

                            }
                        }

                    })
                }
            }


        rv_cart.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showPermission() {
        showGps()
        if (this.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {

            if (this.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }) {


                showGps()
            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1
                )


            }
        }

    }

    var namefix: String? = null
    private fun showGps() {

        val gps = GPSTracker(this)
        if (gps.canGetLocation()) {
            latAwal = gps.latitude
            lonAwal = gps.longitude

            showMarker(latAwal!!, lonAwal!!, "My locations")

            val name = showName(latAwal ?: 0.0, lonAwal ?: 0.0)
            namalokasi = name
            namefix = name.toString()
            alamat.text = namalokasi

        } else gps.showSettingGps()


    }

    //untuk menkonfert dari lat long menjadi nama lokasi
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
                        strAddress.append(fetchedAddress.getAddressLine(i)).append(" ").toString()

                }

            }

        } catch (e: Exception) {

        }
        return name
    }

    //menampilkan marker pada maps
    fun showMainMarker(lat: Double, lon: Double, msg: String) {

        val res = this.resources
        val marker1 = BitmapFactory.decodeResource(res, R.mipmap.market)
        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)

        val coordinate = LatLng(lat, lon)
        map?.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.fromBitmap(smallmarker))
        )
        val cameraPosition =
            CameraPosition.Builder().target(LatLng(lat, lon)).zoom(15f).build()
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun showMarker(lat: Double, lon: Double, msg: String) {

        val coordinate = LatLng(lat, lon)

        map?.addMarker(MarkerOptions().position(coordinate).title(msg))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 5f))
        map?.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
    }

    fun takeLocation(status: Int) {

        try {
            this.let { Places.initialize(it, "AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE") }
            val fields = arrayListOf(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS
            )
            val intent = this.let {
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(it)
            }
            startActivityForResult(intent, status)
        } catch (e: GooglePlayServicesRepairableException) {
            // TODO: Handle the error.
        } catch (e: GooglePlayServicesNotAvailableException) {
            // TODO: Handle the error.
        }

    }

    @SuppressLint("CheckResult")
    private fun route() {

        val origin = latAwal.toString() + "," + lonAwal.toString()
        val dest = latAkhir.toString() + "," + lonAkhir.toString()
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

            jarak = routes[0]?.legs?.get(0)?.distance?.text
            var harga = 0
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val waktu = routes[0]?.legs?.get(0)?.duration?.text
            var jaraksebenarnya = jarakValue.toString().toFloat() / 1000
            var pendekatan = Math.round(jaraksebenarnya)
            val pricex = jarakValue?.toDouble()?.let { Math.round(it) }
            val price = pricex?.div(1000.0)?.times(2000.0)
            val price2 = ChangeFormat.toRupiahFormat2(price.toString())
            if (pendekatan <= 1) {
                harga = 2000
                ongkir.text = "Rp. $harga"
                estimasi.text = "Rp. $harga"
                hargaongkir = harga
                var alfan = sum!! + hargaongkir
                txt_hasil.text = alfan.toString()
                toast(sum!!)


            } else if (pendekatan >= 1) {
                harga = 2000
                ongkir.text = "Rp. ${pendekatan * harga}  ($jarak)"
                estimasi.text = "Rp. ${pendekatan * harga}"
                hargaongkir = pendekatan * harga
                var alfan = sum!! + hargaongkir
                txt_hasil.text = alfan.toString()
                toast(sum!!)

            }


        } else {
            alert {
                message = "Data Route Null"
            }.show()
        }
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.product_name)
        var mprice: TextView = itemView.findViewById(R.id.product_price)
        var mOwner: TextView = itemView.findViewById(R.id.product_owner)

    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.uiSettings?.isMyLocationButtonEnabled = false
        map?.setOnMapClickListener(this)

        val pasuruan = LatLng(-7.664335, 112.699083)
        map!!.moveCamera(CameraUpdateFactory.newLatLng(pasuruan))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                latAwal = place?.latLng?.latitude
                lonAwal = place?.latLng?.longitude
                route()
                namalokasi = place?.address.toString()
                alamat.text = place?.address.toString()
                namefix = place?.address.toString()
                showMainMarker(latAwal ?: 0.0, lonAwal ?: 0.0, place?.address.toString())
                var alfan = sum!! + hargaongkir
//                hasil.text = alfan.toString()

                Log.i("locations", "Place: " + place?.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }
                // TODO: Handle the error.
                Log.i("locatios", status?.statusMessage.toString())

            } else if (resultCode == RESULT_CANCELED) {
            }
        }

    }

    private fun insertServer() {
        val currentTime = Calendar.getInstance().time
        tanggal = currentTime.toString()
        insertRequest(
            currentTime.toString(),
            userID.toString(),
            alamat.text.toString(),
            latAwal,
            lonAwal,
            alamat.text.toString(),
            latAkhir,
            lonAkhir,
            txt_hargatotal.text.toString(),
            jarak.toString()
        )
    }

    fun insertRequest(
        tanggal: String,
        uid: String,
        lokasiAwal: String,
        latAwal: Double?,
        lonAwal: Double?,
        lokasiTujuan: String,
        latTujuan: Double?,
        lonTujuan: Double?,
        harga: String,
        jarak: String
    ): Boolean {

        val booking = Booking()
        booking.tanggal = tanggal
        booking.uid = uid
        booking.lokasiAwal = lokasiAwal
        booking.latAwal = latAwal
        booking.lonAwal = lonAwal
        booking.lokasiTujuan = lokasiTujuan
        booking.lonTujuan = lonTujuan
        booking.latTujuan = latTujuan
        booking.jarak = jarak
        booking.harga = harga
        booking.status = 1
        booking.driver = ""


        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_Booking)
        var nilai = alfan + hargaongkir
        var alfantotal = sum!! + hargaongkir
        keyy = database.reference.push().key
        myRef.child(userID.toString()).child("latitudeToko").setValue(latAkhir.toString())
        myRef.child(userID.toString()).child("longitudeToko").setValue(lonAkhir.toString())
        myRef.child(userID.toString()).child("namatoko").setValue(namapenjual.toString())
        myRef.child(userID.toString()).child("penumpang").setValue(namaCostumer.toString())
        myRef.child(userID.toString()).child("latitudePenumpang").setValue(latAwal.toString())
        myRef.child(userID.toString()).child("longitudePenumpang").setValue(lonAwal.toString())
        myRef.child(userID.toString()).child("harga").setValue(alfantotal.toString())
        myRef.child(userID.toString()).child("ongkir").setValue(hargaongkir.toString())
        myRef.child(userID.toString()).child("notelpon").setValue("082232469415")


        showDialog(true)
        mencaridriverterdekat()

/*

        val hei = keyy

        hei?.let { bookingHistoryUser(it) }
        myRef.child(keyy ?: "").setValue(booking)
*/


        return true
    }

    private fun mencaridriverterdekat() {
        val posisiDriver =
            FirebaseDatabase.getInstance().reference.child("Driver").child("DriverTersedia")
        val geoFire = GeoFire(posisiDriver)
        geoQuery = geoFire.queryAtLocation(GeoLocation(latAkhir!!, lonAkhir!!), radius)
        geoQuery.removeAllListeners()
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
                if (!driverFound) {
                    radius++
                    Log.d("here", "here")
                    mencaridriverterdekat()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!driverFound && requestBol) {
                    driverFound = true
                    driverID = key!!

                    val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Pandaan")
                    db.child("Akun_Driver").child(driverID.toString()).child("status").setValue(userID)
                    db.child("Akun_Driver").child(driverID.toString()).child("uidtoko").setValue(id.toString())


                    val booking: DatabaseReference = FirebaseDatabase.getInstance().getReference("Booking")
                    booking.addValueEventListener(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val data = p0.child(userID.toString()).child("status").value.toString()
                            if (data.equals(driverID.toString())){
                                var alfan = sum!! + hargaongkir
                                    startActivity<TrackingOrderActivity>(   "kunci" to driverID,
                                        "latitudeawal" to latAwal.toString(),
                                        "longitudeawal" to lonAwal.toString(),
                                        "latitudetoko" to latAkhir.toString(),
                                        "longitudetoko" to lonAkhir.toString(),
                                        "alamat" to namalokasi,
                                        "namapenjual" to namapenjual,
                                        "jarak" to jarak,
                                        "gambar" to gambar,
                                        "id" to id,
                                        "harga" to alfan.toString())
                                finish()
                            }
                        }

                    })


                    /*val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Pandaan")
                    db.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val customerID = FirebaseAuth.getInstance().currentUser?.uid
                            val databaseRef = FirebaseDatabase.getInstance().getReference("Pandaan")
                            databaseRef.child("Akun_Driver")
                                .child(driverID!!).child("status").setValue(userID.toString())

                            info { "wiwik$userID" }
                            if () {
                                startActivity<TrackingOrderActivity>(
                                    "kunci" to driverID,
                                    "latitudeawal" to latAwal.toString(),
                                    "longitudeawal" to lonAwal.toString(),
                                    "latitudetoko" to latAkhir.toString(),
                                    "longitudetoko" to lonAkhir.toString(),
                                    "alamat" to namalokasi,
                                    "namapenjual" to namapenjual,
                                    "jarak" to jarak,
                                    "harga" to alfan
                                )
                            }


                        }

                    })*/


//                    ambildriver()

                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onGeoQueryError(error: DatabaseError?) {
            }

        })


    }


    private fun ambildriver() {
        toast("driverfound")
        driverLocationRef = FirebaseDatabase.getInstance().reference
            .child("DriversWorking")
            .child(driverID!!)
            .child("l")
        driverLocationRefListener =
            driverLocationRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists() && requestBol) {
                        val map = p0.value as (List<*>)
                        var locationLat = 0.0
                        var locationLang = 0.0

                        if (map[0] != null) {
                            locationLat = map[0].toString().toDouble()
                        }

                        if (map[1] != null) {
                            locationLang = map[1].toString().toDouble()
                        }
                        val posisiDriver = LatLng(locationLat, locationLang)

                    }
                }

            })

    }

    @Suppress("NAME_SHADOWING")
    private fun sendNotification(notification: JSONObject) {
        val notification = JSONObject()
        val notifcationBody = JSONObject()

        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                toast("Request error")
                sendNotification(notification)
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    override fun onResume() {
        keyy?.let { bookingHistoryUser(it) }
        maps?.onResume()

        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        maps?.onStart()
    }

    override fun onStop() {
        super.onStop()
        maps?.onStop()
    }

    override fun onPause() {
        maps?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        maps?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            showGps()
        }
    }

    override fun onMapClick(p0: LatLng?) {
        var latitude = p0!!.latitude
        var longitude = p0.longitude
        latAwal = latitude
        lonAwal = longitude

        namalokasi = showName(latitude, longitude)
        txt_lokasitoko.text = namalokasi
        map?.clear()
        showMainMarker(latitude, longitude, "alfan")
        route()
    }


}