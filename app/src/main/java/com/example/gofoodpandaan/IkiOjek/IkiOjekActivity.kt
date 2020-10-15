package com.example.gofoodpandaan.IkiOjek

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.example.gofoodpandaan.*
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.skyfishjy.library.RippleBackground
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.accept_order.*
import kotlinx.android.synthetic.main.activity_iki_ojek.*
import kotlinx.android.synthetic.main.activity_pilih_maps.*
import kotlinx.android.synthetic.main.content_map.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import org.jetbrains.anko.*
import java.util.*

class IkiOjekActivity : AppCompatActivity(),GoogleMap.OnMarkerDragListener,AnkoLogger,OnMapReadyCallback {
     var harga: Int? = null
    var pendekatan = 0
     var jaraksebenarnya = 0f
    private val sum: Int? = null
    var jarak: String? = null
    lateinit var sessionManager: SessionManager
    //punya pilihmaps
    lateinit var materialSearchBar: MaterialSearchBar
    private var rippleBg: RippleBackground? = null
    private var mapView: View? = null
    private var placesClient: PlacesClient? = null
    lateinit var predictionList : List<AutocompletePrediction>
    lateinit var mMap: GoogleMap
    var keyy: String? = null
    var nama: String? = null
    var dialog: Dialog? = null
    private lateinit var geoQuery: GeoQuery
    private var radius: Double = 1.0
    private var driverFound = false
    var driverID: String? = null


    private var requestBol = false

    private var mLastKnownLocation: Location? = null
var logic = 0

    lateinit var auth: FirebaseAuth
    var userID : String? = null


    //lokasi system
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    companion object{


        lateinit var peta: GoogleMap
        var namalokasi: String? = null
        var namalokasitujuan : String? = null
        val sheet = DemoBottomSheetFragment()
        var latitude : String? = null
        var longitude : String? = null
        var latitudetujuan : String? = null
        var longitudetujuan : String? = null
        var logic : Int? = null
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        var hargaongkir : Int? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iki_ojek)
        init()

        val bundle: Bundle? = intent.extras
        nama = bundle!!.getString("namacostumer")

        mapview.onCreate(savedInstanceState)
        pilihmaps.visibility = View.INVISIBLE
        accept_order.visibility = View.INVISIBLE
        sessionManager = SessionManager(this)
        materialSearchBar = findViewById(R.id.searchBar)
        rippleBg = findViewById(R.id.ripple_bg)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mapView = mapFragment.view
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@IkiOjekActivity)
        Places.initialize(this@IkiOjekActivity, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
        val token = AutocompleteSessionToken.newInstance()

        materialSearchBar.setOnSearchActionListener(object :
            MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {}
            override fun onSearchConfirmed(text: CharSequence) {
                startSearch(text.toString(), true, null, true)
            }

            override fun onButtonClicked(buttonCode: Int) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch()
                }
            }
        })

        materialSearchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                val predictionsRequest =
                    FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build()
                placesClient!!.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val predictionsResponse =
                                task.result
                            if (predictionsResponse != null) {
                                predictionList =
                                    predictionsResponse.autocompletePredictions
                                val suggestionsList: MutableList<String?> =
                                    ArrayList()
                                for (i in predictionList.indices) {
                                    val prediction: AutocompletePrediction =
                                        predictionList.get(i)
                                    suggestionsList.add(prediction.getFullText(null).toString())
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList)
                                if (!materialSearchBar.isSuggestionsVisible) {
                                    materialSearchBar.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful")
                        }
                    }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        materialSearchBar.setSuggstionsClickListener(object :
            SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View) {
                if (position >= predictionList.size) {
                    return
                }
                val selectedPrediction = predictionList[position]
                val suggestion =
                    materialSearchBar.lastSuggestions[position].toString()
                materialSearchBar.text = suggestion
                Handler().postDelayed({ materialSearchBar.clearSuggestions() }, 1000)
                val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(
                    materialSearchBar.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
                val placeId = selectedPrediction.placeId
                val placeFields =
                    Arrays.asList(Place.Field.LAT_LNG)
                val fetchPlaceRequest =
                    FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient!!.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        Log.i("mytag", "Place found: " + place.name)
                        val latLngOfPlace = place.latLng
                        if (latLngOfPlace != null) {
                            mMap!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            val apiException = e as ApiException
                            apiException.printStackTrace()
                            val statusCode = apiException.statusCode
                            Log.i("mytag", "place not found: " + e.message)
                            Log.i("mytag", "status code: $statusCode")
                        }
                    }
            }

            override fun OnItemDeleteListener(position: Int, v: View) {}
        })

        btn_accept.setOnClickListener(View.OnClickListener {
            val latitude = mMap!!.cameraPosition.target.latitude
            val longitude = mMap!!.cameraPosition.target.longitude
            rippleBg!!.startRippleAnimation()
            Companion.latitude = latitude.toString()
            Companion.longitude = longitude.toString()

            startActivity<IkiOjekActivity>()
            Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
        })

        mapview.getMapAsync { googleMap ->
            peta = googleMap

            googleMap.isMyLocationEnabled = true
            googleMap.setOnMarkerDragListener(this)



            edt_lokasitujuan.setOnClickListener {
                mapview.visibility = View.INVISIBLE
                layoutBottomSheet.visibility = View.INVISIBLE
                accept_order.visibility = View.INVISIBLE

                pilihmaps.visibility = View.VISIBLE
                logic = 2
            }

            edt_namalokasianda.setOnClickListener{
                mapview.visibility = View.INVISIBLE
                layoutBottomSheet.visibility = View.INVISIBLE
                accept_order.visibility = View.INVISIBLE

                pilihmaps.visibility = View.VISIBLE
                logic = 1

            }

            btn_accept.setOnClickListener {
                if (logic ==1){
                    mapview.visibility = View.VISIBLE
                    layoutBottomSheet.visibility = View.VISIBLE
                    accept_order.visibility = View.INVISIBLE
                    pilihmaps.visibility = View.INVISIBLE
                    val lat = mMap!!.cameraPosition.target.latitude
                    val long = mMap!!.cameraPosition.target.longitude
                    latitude = lat.toString()
                    longitude = long.toString()
                    rippleBg!!.startRippleAnimation()
                    Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
                    if (latitudetujuan !=null && longitudetujuan !=null){
                        peta!!.clear()
                        showMainMarker(latitude!!.toDouble(), longitude!!.toDouble(),"posisiku")
                        showMainMarker(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble(),"posisitujuan")
                        val nama = showName(latitude!!.toDouble(), longitude!!.toDouble())
                        edt_namalokasianda.setText(nama)
                        val namatujuan = showName(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                        edt_lokasitujuan.setText(namatujuan)
                        mapview.visibility = View.VISIBLE
                        layoutBottomSheet.visibility = View.INVISIBLE
                        accept_order.visibility = View.VISIBLE
                        pilihmaps.visibility = View.INVISIBLE
                        route(latitude!!, longitude!!, latitudetujuan!!, longitudetujuan!!)

                    }
                    else{
                        peta!!.clear()
                        showMainMarker(latitude!!.toDouble(), longitude!!.toDouble(),"posisiku")

                    }

                }

                else if (logic ==2){
                    val lat = mMap!!.cameraPosition.target.latitude
                    val long = mMap!!.cameraPosition.target.longitude
                    latitudetujuan = lat.toString()
                    longitudetujuan = long.toString()
                    rippleBg!!.startRippleAnimation()
                    val nama = showName(latitude!!.toDouble(), longitude!!.toDouble())
                    edt_namalokasianda.setText(nama)
                    val namatujuan = showName(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                    edt_lokasitujuan.setText(namatujuan)
                    peta!!.clear()
                    showMainMarker(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble(),"posisitujuan")
                    showMainMarker(latitude!!.toDouble(), longitude!!.toDouble(),"posisiku")
                    Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
                    mapview.visibility = View.VISIBLE
                    layoutBottomSheet.visibility = View.INVISIBLE
                    accept_order.visibility = View.VISIBLE
                    pilihmaps.visibility = View.INVISIBLE
                    route(latitude!!, longitude!!, latitudetujuan!!, longitudetujuan!!)
                }

            }

            btn_jemput.setOnClickListener {
                if (latitude != null && longitude != null && latitudetujuan != null && longitudetujuan !=null) {
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


            if (latitude !=null && longitude !=null && latitudetujuan !=null && longitudetujuan !=null){
              route(latitude!!, longitude!!, latitudetujuan!!, longitudetujuan!!)
            }



        }


    }

    private fun init(){
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setFastestInterval(3000)
        locationRequest.interval = 5000
        locationRequest.setSmallestDisplacement(10f)

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                latitude = locationResult!!.lastLocation.latitude.toString()
                longitude = locationResult!!.lastLocation.longitude.toString()
                val newPos = LatLng(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
                peta.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))
            }

        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())

    }


    @SuppressLint("CheckResult")
    private fun route(latitude : String, longitude : String, latitudedriver : String, longitudedriver : String) {
        val origin = latitude.toString() + "," + longitude.toString()
        val dest = latitudedriver.toString() + "," + longitudedriver.toString()
        NetworkModule.getService()
            .actionRoute(origin, dest, "AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ t: ResultRoute? ->
                showData(t?.routes)
            },
                {})
    }

    @SuppressLint("SetTextI18n")
    private fun showData(routes: List<RoutesItem?>?) {


        if (routes != null) {

            val point = routes.get(0)?.overviewPolyline?.points
            jarak = routes[0]?.legs?.get(0)?.distance?.text
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val pricex = jarakValue!!.toDouble().let { Math.round(it) }
            val price = pricex.div(1000.0).times(2000.0)
             jaraksebenarnya = jarakValue.toString().toFloat() / 1000
             pendekatan = Math.round(jaraksebenarnya)
            peta?.let {
                point?.let { it1 ->
                    DirectionMapsV2.gambarRoute(
                        it,
                        it1
                    )
                }
            }

            if (pendekatan <= 5) {
                toast("halo")
                harga = 9000
                txt_hargaongkir.text = "Rp. $harga"
                txt_jarakojek.text = "Rp. $jarak"
            } else if (pendekatan >5) {
                harga = 2000
                hargaongkir = pendekatan * harga!!
                txt_hargaongkir.text = "Rp. ${pendekatan * harga!!}  ($jarak)"
                txt_jarakojek.text = "Rp. ${pendekatan* harga!!}"
            }
        }
    }

    private fun insertServer() {
        val currentTime = Calendar.getInstance().time
        insertRequest(
            currentTime.toString(),
            userID.toString(),
            latitude!!.toDouble(),
            longitude!!.toDouble(),
            latitudetujuan!!.toDouble(),
            longitudetujuan!!.toDouble(),
            jarak.toString(),
            hargaongkir.toString()
        )
    }

    fun insertRequest(
        tanggal: String,
        uid: String,
        latAwal: Double?,
        lonAwal: Double?,
        latTujuan: Double?,
        lonTujuan: Double?,
        jarak: String,
        harga: String
    ): Boolean {

        val booking = Booking()
        booking.tanggal = tanggal
        booking.uid = uid
        booking.latAwal = latAwal
        booking.lonAwal = lonAwal
        booking.lonTujuan = lonTujuan
        booking.latTujuan = latTujuan
        booking.jarak = jarak
        booking.harga = harga


        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_BookingOjek)
        keyy = database.reference.push().key
        myRef.child(userID.toString()).child("latitudeTujuan").setValue(latTujuan.toString())
        myRef.child(userID.toString()).child("longitudeTujuan").setValue(lonTujuan.toString())
        myRef.child(userID.toString()).child("penumpang").setValue(nama.toString())
        myRef.child(userID.toString()).child("latitudePenumpang").setValue(latAwal.toString())
        myRef.child(userID.toString()).child("longitudePenumpang").setValue(lonAwal.toString())
        myRef.child(userID.toString()).child("harga").setValue(harga)
        myRef.child(userID.toString()).child("notelpon").setValue("082232469415")
        myRef.child(userID.toString()).child("alamat").setValue(showName(latTujuan!!,lonTujuan!!))


        showDialog(true)
        mencaridriverterdekat()

/*

        val hei = keyy

        hei?.let { bookingHistoryUser(it) }
        myRef.child(keyy ?: "").setValue(booking)
*/


        return true
    }

    private fun showDialog(status: Boolean) {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.tunggudriver)

        if (status) {


            dialog?.show()

        } else dialog?.dismiss()


    }

    private fun mencaridriverterdekat() {
        val posisiDriver =
            FirebaseDatabase.getInstance().reference.child("Driver").child("DriverTersedia")
        val geoFire = GeoFire(posisiDriver)
        geoQuery = geoFire.queryAtLocation(GeoLocation(latitude!!.toDouble(), longitude!!.toDouble()), radius)
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
                    db.child("Akun_Driver").child(driverID.toString()).child("statusOjek").setValue(userID)

                    val booking: DatabaseReference = FirebaseDatabase.getInstance().getReference("BookingOjek")
                    booking.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val data = p0.child(userID.toString()).child("status").value.toString()
                            if (data.equals(driverID.toString())){
                                showDialog(false)
                                startActivity<TrackingOrderOjekActivity>(   "kunci" to driverID,
                                    "latitudeawal" to latitude.toString(),
                                    "longitudeawal" to longitude.toString(),
                                    "latitudetujuan" to latitudetujuan.toString(),
                                    "longitudetujuan" to longitudetujuan.toString())
                                finish()
                            }
                        }

                    })

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

    fun showPermission() {
        showGps()
        if (this.let { ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
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

    private fun showGps() {

        val gps = GPSTracker(this)
        if (gps.canGetLocation()) {
            latitude = gps.latitude.toString()
            longitude = gps.longitude.toString()
            edt_namalokasianda.setText(namalokasi)

        } else gps.showSettingGps()


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                namalokasi = place?.address.toString()

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

    override fun onResume() {
        super.onResume()
        mapview?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapview?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapview?.onStop()
    }

    override fun onPause() {
        mapview?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
        mapview?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview?.onLowMemory()
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
                        strAddress.append(fetchedAddress.getAddressLine(i)).append(" ").toString()

                }

            }

        } catch (e: Exception) {

        }
        return name
    }

    fun showMainMarker(lat: Double, lon: Double, msg: String) {

        val coordinate = LatLng(lat, lon)
        peta.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .draggable(true)
        )
        val cameraPosition =
            CameraPosition.Builder().target(LatLng(lat, lon)).zoom(17f).build()
        peta.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    fun showTujuanMarker(lat: Double, lon: Double, msg: String) {

        val coordinate = LatLng(lat, lon)
        peta.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .draggable(true)
        )
        val cameraPosition =
            CameraPosition.Builder().target(LatLng(lat, lon)).zoom(17f).build()
        peta.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        val latitude = p0!!.position.latitude
        val longitude = p0.position.longitude
        namalokasi = showName(latitude,longitude)
        edt_namalokasianda.setText(namalokasi)
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object :PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    //aktifkan
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        toast("button di klik")
                        mFusedLocationProviderClient.lastLocation
                            .addOnFailureListener { e->
                                toast("permission ${p0!!.permissionName} + gagal ")
                            }.addOnSuccessListener { location ->
                                val userLatLng = LatLng(location.latitude,location.longitude)
                                peta.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f))
                            }
                        true
                    }

                    //layout

                        }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    toast("Permission ${p0!!.permissionName} + gagal")
                }
            })

        if (mapView != null && mapView!!.findViewById<View?>("1".toInt()) != null) {
            val locationButton =
                (mapView!!.findViewById<View>("1".toInt())
                    .parent as View).findViewById<View>("2".toInt())
            val layoutParams =
                locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 180)
        }


    }

}

class DemoBottomSheetFragment : SuperBottomSheetFragment() {
    lateinit var edtTujuan : EditText
    lateinit var edtAwal : EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root =  inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        return root

    }


    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
}

class BottomAccept : SuperBottomSheetFragment() {
    lateinit var edtTujuan : EditText
    lateinit var edtAwal : EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root =  inflater.inflate(R.layout.accept_order, container, false)

        return root

    }


    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)

    override fun getStatusBarColor() = Color.RED

    override fun getPeekHeight(): Int = 200
}



