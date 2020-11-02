package com.example.gofoodpandaan.IkiOjek

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
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
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.directions.route.*
import com.example.gofoodpandaan.*
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.Notification.NotifikasiData
import com.example.gofoodpandaan.Notification.PushNotifikasi
import com.example.gofoodpandaan.Notification.RetrofitInstance
import com.example.gofoodpandaan.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.skyfishjy.library.RippleBackground
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.accept_order.*
import kotlinx.android.synthetic.main.activity_iki_ojek.*
import kotlinx.android.synthetic.main.content_map.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.util.*

const val TOPIC = "/topics/myTopic2"

class IkiOjekActivity : AppCompatActivity(), GoogleMap.OnMarkerDragListener, AnkoLogger,
    RoutingListener {
    var harga: Int? = null
    var hargamobil: Int? = null
    var pendekatan = 0
    var jaraksebenarnya = 0f
    private val sum: Int? = null
    var jarak: String? = null
    var jarakvalue: Float? = null
    lateinit var sessionManager: SessionManager

    //punya pilihmaps
    lateinit var materialSearchBar: MaterialSearchBar
    private var rippleBg: RippleBackground? = null
    private var mapView: View? = null
    private var placesClient: PlacesClient? = null
    lateinit var predictionList: List<AutocompletePrediction>
    var keyy: String? = null
    var nama: String? = null
    var dialog: Dialog? = null
    lateinit var geoQuery: GeoQuery
    private var radius: Double = 1.0
    private var driverFound = false
    var driverID: String? = null
    val TAG = "IkiOjekActivity"
    private var requestBol = false
    var logika = 0
    var valueListener: ValueEventListener? = null
    private var mLastKnownLocation: Location? = null
    var logic = 0
    var logikastatus = 0
    lateinit var auth: FirebaseAuth
    var userID: String? = null
    private lateinit var mapFragment: SupportMapFragment
    var latitude: String? = null
    var longitude: String? = null
    var latitudetujuan: String? = null
    var longitudetujuan: String? = null
    private var mFirestore: FirebaseFirestore? = null

    //tipekendaraan
    //0 = motor
    //1 = mobil
    var tipekendaraan = 0

    //======================
    companion object {


        private lateinit var peta: GoogleMap
        var namalokasi: String? = null
        var namalokasitujuan: String? = null
        var logic: Int? = null
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        var hargaongkir: Int? = null
        var hargaongkirmobil: Int? = null
    }

    lateinit var geoQueryEventListener: GeoQueryEventListener

    //lokasi system
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient


    lateinit var progressDialog: Dialog

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iki_ojek)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Sedang mencari lokasi")
        progressDialog.setCanceledOnTouchOutside(false)

        progressDialog.show()
        val bundle: Bundle? = intent.extras
        checkMyLocationPermission()
        init()
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)


        nama = bundle!!.getString("namacostumer")
        mapview.onCreate(savedInstanceState)
        ikiae.visibility = View.INVISIBLE
        accept_order.visibility = View.INVISIBLE
        sessionManager = SessionManager(this)
        sessionManager.setiduser("1")
        materialSearchBar = findViewById(R.id.searchBar)
        rippleBg = findViewById(R.id.ripple_bg)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

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
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setLocationBias(
                            RectangularBounds.newInstance(
                                LatLng(-7.657273, 112.696534),
                                LatLng(-7.608442, 112.769147)
                            )
                        )
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
                imm.hideSoftInputFromWindow(
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
                            peta.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            val apiException = e
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
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("sedang mencari rute")
            progressDialog.setCanceledOnTouchOutside(false)
            val latituded = peta.cameraPosition.target.latitude
            val longituded = peta.cameraPosition.target.longitude
            rippleBg!!.startRippleAnimation()
            latitude = latituded.toString()
            longitude = longituded.toString()

            startActivity<IkiOjekActivity>()
            Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
        })

        mapview.getMapAsync { googleMap ->
            peta = googleMap
            peta.isMyLocationEnabled = true
            peta.uiSettings.isMyLocationButtonEnabled = true
            peta.uiSettings.isCompassEnabled = true
            if (latitude != null && longitude != null) {
                peta.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            latitude!!.toDouble(),
                            longitude!!.toDouble()
                        ), 18f
                    )
                )
                peta.uiSettings.isMyLocationButtonEnabled = true
                peta.uiSettings.isCompassEnabled = true
            }




            Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        //aktifkan
                        peta.uiSettings.isCompassEnabled = true
                        peta.isMyLocationEnabled = true
                        peta.uiSettings.isMyLocationButtonEnabled = true

                        peta.setOnMyLocationClickListener {
                            toast("button di klik")
                            mFusedLocationProviderClient.lastLocation
                                .addOnFailureListener { e ->
                                    toast("permission ${p0!!.permissionName} + gagal ")
                                }.addOnSuccessListener { location ->
                                    val userLatLng = LatLng(location.latitude, location.longitude)
                                    peta.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            userLatLng,
                                            18f
                                        )
                                    )
                                    peta.addMarker(
                                        MarkerOptions().position(
                                            LatLng(
                                                latitude!!.toDouble(),
                                                longitude!!.toDouble()
                                            )
                                        ).title("posisiku")
                                            .icon(
                                                BitmapDescriptorFactory.defaultMarker(
                                                    BitmapDescriptorFactory.HUE_GREEN
                                                )
                                            )
                                    )

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

            peta.setOnMarkerDragListener(this@IkiOjekActivity)



            edt_lokasitujuan.setOnClickListener {
                mapview.visibility = View.VISIBLE
                layoutBottomSheet.visibility = View.INVISIBLE
                accept_order.visibility = View.INVISIBLE
                ikiae.visibility = View.VISIBLE
                logic = 2
            }

            edt_namalokasianda.setOnClickListener {
                peta.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            latitude!!.toDouble(),
                            longitude!!.toDouble()
                        ), 18f
                    )
                )

                mapview.visibility = View.VISIBLE
                layoutBottomSheet.visibility = View.INVISIBLE
                accept_order.visibility = View.INVISIBLE
                ikiae.visibility = View.VISIBLE
                logic = 1

            }

            btn_accept.setOnClickListener {
                if (logic == 1) {
                    mapview.visibility = View.VISIBLE
                    layoutBottomSheet.visibility = View.VISIBLE
                    accept_order.visibility = View.INVISIBLE
                    ikiae.visibility = View.INVISIBLE
                    val lat = peta.cameraPosition.target.latitude
                    val long = peta.cameraPosition.target.longitude
                    latitude = lat.toString()
                    longitude = long.toString()
                    rippleBg!!.startRippleAnimation()
                    Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
                    if (latitudetujuan != null && longitudetujuan != null) {
                        peta.clear()
                        showbetweenmarker(
                            latitude!!.toDouble(), longitude!!.toDouble(),
                            latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble(),
                            "posisiku",
                            "posisi tujuan"
                        )
                        val nama = showName(latitude!!.toDouble(), longitude!!.toDouble())
                        edt_namalokasianda.text = nama
                        val namatujuan =
                            showName(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                        edt_lokasitujuan.text = namatujuan
                        mapview.visibility = View.VISIBLE
                        layoutBottomSheet.visibility = View.INVISIBLE
                        accept_order.visibility = View.VISIBLE
                        ikiae.visibility = View.INVISIBLE
                        Findroutes(
                            LatLng(latitude!!.toDouble(), longitude!!.toDouble()),
                            LatLng(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                        )
//                        route(latitude!!, longitude!!, latitudetujuan!!, longitudetujuan!!)

                    } else {
                        peta.clear()
                        showMainMarker(latitude!!.toDouble(), longitude!!.toDouble(), "posisiku")

                    }

                } else if (logic == 2) {
                    val lat = peta.cameraPosition.target.latitude
                    val long = peta.cameraPosition.target.longitude
                    latitudetujuan = lat.toString()
                    longitudetujuan = long.toString()
                    rippleBg!!.startRippleAnimation()
                    val nama = showName(latitude!!.toDouble(), longitude!!.toDouble())
                    edt_namalokasianda.text = nama
                    val namatujuan =
                        showName(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                    edt_lokasitujuan.text = namatujuan
                    peta.clear()
                    showbetweenmarker(
                        latitude!!.toDouble(), longitude!!.toDouble(),
                        latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble(),
                        "posisiku",
                        "posoi"
                    )
                    showMainMarker(
                        latitudetujuan!!.toDouble(),
                        longitudetujuan!!.toDouble(),
                        "posisitujuan"
                    )
                    showMainMarker(latitude!!.toDouble(), longitude!!.toDouble(), "posisiku")
                    Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
                    mapview.visibility = View.VISIBLE
                    layoutBottomSheet.visibility = View.INVISIBLE
                    accept_order.visibility = View.VISIBLE
                    ikiae.visibility = View.INVISIBLE
                    Findroutes(
                        LatLng(latitude!!.toDouble(), longitude!!.toDouble()),
                        LatLng(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                    )
                }

            }

            rv_mobil.setOnClickListener {
                //mobil
                tipekendaraan = 1
                rv_mobil.setBackgroundColor(Color.BLUE)
                rv_motor.setBackgroundColor(Color.WHITE)
                peta.clear()
                Findroutes(
                    LatLng(latitude!!.toDouble(), longitude!!.toDouble()),
                    LatLng(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                )
            }

            rv_motor.setOnClickListener {
                //motor
                tipekendaraan = 0
                rv_motor.setBackgroundColor(Color.BLUE)
                rv_mobil.setBackgroundColor(Color.WHITE)
                peta.clear()
                Findroutes(
                    LatLng(latitude!!.toDouble(), longitude!!.toDouble()),
                    LatLng(latitudetujuan!!.toDouble(), longitudetujuan!!.toDouble())
                )

            }

            btn_jemput.setOnClickListener {
                logikastatus = 1
                val ref =
                    FirebaseDatabase.getInstance().reference.child("Pandaan").child("Costumers")
                        .child(userID.toString()).child("status").removeValue()
                if (tipekendaraan == 0) {
                    if (pendekatan < 30) {
                        if (latitude != null && longitude != null && latitudetujuan != null && longitudetujuan != null && hargaongkir != null) {
                            when (requestBol) {
                                false -> {
                                    requestBol = true
                                    insertServer()
                                }
                            }
                        } else {
                            toast("masukan lokasi terlebih dahulu")
                        }
                    } else {
                        toast("Lokasi tujuan maksimal 30KM untuk OJEK")
                    }
                } else if (tipekendaraan == 1) {
                    if (latitude != null && longitude != null && latitudetujuan != null && longitudetujuan != null && hargaongkir != null) {
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


            }


            if (latitude != null && longitude != null && latitudetujuan != null && longitudetujuan != null) {
                route(latitude!!, longitude!!, latitudetujuan!!, longitudetujuan!!)
            }


        }


    }

    private fun init() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = 3000
        locationRequest.interval = 5000
        locationRequest.smallestDisplacement = 10f

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                val newPos = LatLng(
                    locationResult!!.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                latitude = locationResult.lastLocation.latitude.toString()
                longitude = locationResult.lastLocation.longitude.toString()



                progressDialog.dismiss()

            }

        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )

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
            peta.addMarker(MarkerOptions().position(marker1).title(namaawal))
        val playerMarker: Marker =
            peta.addMarker(MarkerOptions().position(marker2).title(namatujuan))

        markersList.add(youMarker)
        markersList.add(playerMarker)

        val builder = LatLngBounds.Builder()
        for (m in markersList) {
            builder.include(m.position)
        }
        val padding = 50
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        peta.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback { //animate camera here
            peta.animateCamera(cu)
        })

    }


    @SuppressLint("CheckResult")
    private fun route(
        latitude: String,
        longitude: String,
        latitudedriver: String,
        longitudedriver: String
    ) {
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
            peta.let {
                point?.let { it1 -> DirectionMapsV2.gambarRoute(it, it1) }
            }
            progressDialog.dismiss()

            if (pendekatan <= 5) {
                harga = 9000
                hargamobil = 20000
                txt_hargaongkir.text = "Rp. $harga"
                txt_jarakojek.text = "Rp. $jarak"
                txt_hargaongkirmobil.text = "Rp. $hargamobil"
            } else if (pendekatan > 5) {
                harga = 2000
                hargamobil = 5000
                hargaongkir = pendekatan * harga!! - 1000
                hargaongkirmobil = pendekatan * hargamobil!! - 1000
                txt_hargaongkir.text = "Rp. ${pendekatan * harga!!}  ($jarak)"
                txt_jarakojek.text = "Rp. ${pendekatan * harga!!}"
                txt_hargaongkirmobil.text = "Rp. ${pendekatan * hargaongkir!!}"
            }
        }
    }

    private fun insertServer() {
        val currentTime = Calendar.getInstance().time
        if (tipekendaraan == 0) {
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
        } else if (tipekendaraan == 1) {
            insertRequest(
                currentTime.toString(),
                userID.toString(),
                latitude!!.toDouble(),
                longitude!!.toDouble(),
                latitudetujuan!!.toDouble(),
                longitudetujuan!!.toDouble(),
                jarak.toString(),
                hargamobil.toString()
            )

        }
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
        if (tipekendaraan == 0) {
            val myRef = database.getReference(Constan.tb_BookingOjek)
            keyy = database.reference.push().key
            myRef.child(userID.toString()).child("latitudeTujuan").setValue(latTujuan.toString())
            myRef.child(userID.toString()).child("longitudeTujuan").setValue(lonTujuan.toString())
            myRef.child(userID.toString()).child("penumpang").setValue(nama.toString())
            myRef.child(userID.toString()).child("latitudePenumpang").setValue(latAwal.toString())
            myRef.child(userID.toString()).child("longitudePenumpang").setValue(lonAwal.toString())
            myRef.child(userID.toString()).child("harga").setValue(harga)
            myRef.child(userID.toString()).child("notelpon")
                .setValue(sessionManager.gettelefon().toString())
            myRef.child(userID.toString()).child("jarak")
                .setValue("${jaraksebenarnya.toString()} KM")
            myRef.child(userID.toString()).child("alamat")
                .setValue(showName(latTujuan!!, lonTujuan!!))
            myRef.child(userID.toString()).child("area").setValue(showArea(latTujuan, lonTujuan))

            showDialog(true)

            mencaridriverterdekat()

        } else if (tipekendaraan == 1) {
            val myRef = database.getReference(Constan.tb_BookingTaksi)
            keyy = database.reference.push().key
            myRef.child(userID.toString()).child("latitudeTujuan").setValue(latTujuan.toString())
            myRef.child(userID.toString()).child("longitudeTujuan").setValue(lonTujuan.toString())
            myRef.child(userID.toString()).child("penumpang").setValue(nama.toString())
            myRef.child(userID.toString()).child("latitudePenumpang").setValue(latAwal.toString())
            myRef.child(userID.toString()).child("longitudePenumpang").setValue(lonAwal.toString())
            myRef.child(userID.toString()).child("harga").setValue(harga)
            myRef.child(userID.toString()).child("notelpon")
                .setValue(sessionManager.gettelefon().toString())
            myRef.child(userID.toString()).child("jarak")
                .setValue("${jaraksebenarnya.toString()} KM")
            myRef.child(userID.toString()).child("alamat")
                .setValue(showName(latTujuan!!, lonTujuan!!))
            myRef.child(userID.toString()).child("area").setValue(showArea(latTujuan, lonTujuan))


            showDialog(true)

            mencaridriverterdekat()

        }


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
        dialog?.setCanceledOnTouchOutside(false)
        if (status) {
            dialog?.show()

        } else dialog?.dismiss()


    }

    var data = ""
    var alfan: String? = null
    val booking = FirebaseDatabase.getInstance().reference.child("BookingOjek")
    val kode = kodeorder()
    val listener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            TODO("Not yet implemented")
        }

        override fun onDataChange(p0: DataSnapshot) {
            val data = p0.child(userID.toString()).child("status").value.toString()
            if (data.equals(driverID.toString())) {
                showDialog(false)
                peta.clear()

                val usermap: HashMap<String, Any?> = HashMap()
                usermap["status"] = "ojek"
                usermap["kodeorder"] = kode
                usermap["uidku"] = userID.toString()
                usermap["uiddriver"] = driverID.toString()
                usermap["latitudePenumpang"] = latitude.toString()
                usermap["longitudePenumpang"] = longitude.toString()
                usermap["latitudeTujuan"] = latitudetujuan.toString()
                usermap["longitudeTujuan"] = longitudetujuan.toString()
                if (logika == 0) {
                    val ref =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(userID.toString()).child(kode).setValue(usermap)
                    logika = 1
                }
                toast(radius.toString())
                sessionManager.setiduser("2")
                startActivity<TrackingOrderOjekActivity>(
                    "kunci" to driverID,
                    "latitudeawal" to latitude.toString(),
                    "longitudeawal" to longitude.toString(),
                    "latitudetujuan" to latitudetujuan.toString(),
                    "longitudetujuan" to longitudetujuan.toString(),
                    "kode" to kode,
                    "harga" to hargaongkir.toString()
                )

                finish()


            }
        }

    }

    private fun geo() {
        geoQueryEventListener = object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
                if (!driverFound && radius < 8.0) {
                    radius++
                    Log.d("here", "here")
                    mencaridriverterdekat()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!driverFound && requestBol) {
                    driverFound = true
                    driverID = key!!
                    //hapus driverTersedia
                    if (sessionManager.getiduser().equals("1")) {
                        if (tipekendaraan == 0) {
                            val hapusdriverTersedia: DatabaseReference =
                                FirebaseDatabase.getInstance().reference
                            hapusdriverTersedia.child("Driver").child("DriverTersedia")
                                .child(driverID.toString()).removeValue()
                            if (driverID != null) {
                                ams()
                                kirimpesan("IKIJEK", "ada orderan siap siap yaa")
                            } else {
                                ams()
                                kirimpesan("IKIJEK", "ada orderan siap siap yaa")
                            }
                            if (logikastatus == 1) {
                                val db: DatabaseReference =
                                    FirebaseDatabase.getInstance().getReference("Pandaan")
                                db.child("Akun_Driver").child(driverID.toString())
                                    .child("statusOjek").setValue(userID)
                                db.child("Akun_Driver").child(driverID.toString())
                                    .child("kodeorder").setValue(kode)
                            }

                            booking.addValueEventListener(listener)
                        } else if (tipekendaraan == 1) {
                            val hapusdriverTersedia: DatabaseReference =
                                FirebaseDatabase.getInstance().reference
                            hapusdriverTersedia.child("Driver").child("TaksiTersedia")
                                .child(driverID.toString()).removeValue()
                            if (driverID != null) {
                                ams()
                                kirimpesan("IKITAKSI", "ada orderan siap siap yaa")
                            } else {
                                ams()
                                kirimpesan("IKITAKSI", "ada orderan siap siap yaa")
                            }
                            if (logikastatus == 1) {
                                val db: DatabaseReference =
                                    FirebaseDatabase.getInstance().getReference("Pandaan")
                                db.child("Akun_Taksi").child(driverID.toString())
                                    .child("statusTaksi").setValue(userID)
                                db.child("Akun_Taksi").child(driverID.toString()).child("kodeorder")
                                    .setValue(kode)
                            }

                            booking.addValueEventListener(listener)
                        }

                    }


                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                geoQuery.removeAllListeners()
            }

            override fun onKeyExited(key: String?) {
                geoQuery.removeAllListeners()
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                geoQuery.removeAllListeners()
            }


        }

    }

    private fun mencaridriverterdekat() {
        if (tipekendaraan == 0) {
            val posisiDriver =
                FirebaseDatabase.getInstance().reference.child("Driver").child("DriverTersedia")
            val geoFire = GeoFire(posisiDriver)
            geoQuery = geoFire.queryAtLocation(
                GeoLocation(latitude!!.toDouble(), longitude!!.toDouble()),
                radius
            )
            geo()
            geoQuery.removeAllListeners()
            geoQuery.addGeoQueryEventListener(geoQueryEventListener)
        } else if (tipekendaraan == 1) {
            val posisiDriver =
                FirebaseDatabase.getInstance().reference.child("Driver").child("TaksiTersedia")
            val geoFire = GeoFire(posisiDriver)
            geoQuery = geoFire.queryAtLocation(
                GeoLocation(latitude!!.toDouble(), longitude!!.toDouble()),
                radius
            )
            geo()
            geoQuery.removeAllListeners()
            geoQuery.addGeoQueryEventListener(geoQueryEventListener)
        }


    }

    fun kodeorder(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val outputStrLength = (20..36).shuffled().first()

        return (1..outputStrLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun ams() {
        val tokendriver = FirebaseDatabase.getInstance().getReference("Driver")
        tokendriver.child("DriverTersedia")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    data = p0.child(driverID.toString()).child("token").value.toString()
                    alfan = data
                    kirimpesan("IKIJEK", "ada orderan siap siap yaa")
                }

            })
    }

    fun kirimpesan(judul: String, pesan: String) {
        val recipientToken = "$data"
        if (judul.isNotEmpty() && pesan.isNotEmpty() && recipientToken.isNotEmpty()) {
            PushNotifikasi(
                NotifikasiData(judul, pesan),
                recipientToken
            ).also {
                sendNotification(it)
            }
        }
    }

    private fun sendNotification(notification: PushNotifikasi) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }

    private fun checkMyLocationPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    when {
                        report!!.areAllPermissionsGranted() -> {
                            init()
                        }
                        report.isAnyPermissionPermanentlyDenied -> {
                            Log.d("PERMISSIONCHECK", "ANY PERMISSION PERMANENTLY DENIED")
                        }
                        else -> {
                            checkMyLocationPermission()
                            Log.d("PERMISSIONCHECK", "ANY PERMISSION DENIED")
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    /** This will display a dialog if you need to allow permissions
                     *
                     * */
                    checkMyLocationPermission()
                    Log.d("PERMISSIONCHECK", "onPermissionRationaleShouldBeShown")
                    token?.continuePermissionRequest()
                }

            })
            .check()
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


    override fun onBackPressed() {
        super.onBackPressed()
        val hapus =
            FirebaseDatabase.getInstance().reference.child("BookingOjek").child(userID.toString())
                .removeValue()
        startActivity<HomeActivity>()
        if (logikastatus == 1) {
            geoQuery.removeAllListeners()
            logikastatus = 0
        }
        sessionManager.setiduser("2")


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
//        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
        if (logikastatus == 1) {
            geoQuery.removeAllListeners()
            logikastatus = 0

        }
        sessionManager.setiduser("2")
        booking.removeEventListener(listener)
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

    fun showArea(lat: Double, lon: Double): String {

        var name = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (addresses.size > 0) {
                val fetchedAddress = addresses.get(0)
                val strAddress = StringBuilder()

                for (i in 0..fetchedAddress.maxAddressLineIndex) {
                    name = fetchedAddress.locality
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            toast("tidak dapat mendapatkan aplikasi")
        } else {
            if (tipekendaraan == 0) {
                val routing: Routing = Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .avoid(AbstractRouting.AvoidKind.TOLLS)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE") //also define your api key here.
                    .build()
                routing.execute()
            } else if (tipekendaraan == 1) {
                val routing: Routing = Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE") //also define your api key here.
                    .build()
                routing.execute()

            }

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
                polyOptions.color(Color.GREEN)
                polyOptions.width(10F)
                polyOptions.addAll(routes.get(shortestRouteIndex).points)
                val polyline = peta.addPolyline(polyOptions)
                polylineStartLatLng = polyline.points.get(0)
                val k = polyline.points.size
                polylineEndLatLng = polyline.points.get(k - 1)
                (polylines as ArrayList<Polyline>).add(polyline)
                jaraksebenarnya = routes.get(i).distanceValue.toString().toFloat() / 1000
                pendekatan = Math.round(jaraksebenarnya)

                txt_jarakojek.text = routes.get(i).distanceText.toString()
                if (pendekatan <= 5) {
                    hargaongkir = 9000
                    hargamobil = 20000
                    toast(jaraksebenarnya.toString())
                    txt_hargaongkir.text = "Rp. $hargaongkir"
                    txt_hargaongkirmobil.text = "Rp. $hargamobil"


                } else if (pendekatan > 5) {
                    val hargamobilpunya = 5000
                    harga = 2000
                    hargaongkir = pendekatan * harga!! - 1000
                    hargamobil = pendekatan * hargamobilpunya - 1000
                    txt_hargaongkir.text = "Rp. ${hargaongkir}"
                    txt_hargaongkirmobil.text = "Rp. $hargamobil"

                }
            } else {
            }
        }

    }


    override fun onMarkerDragEnd(p0: Marker?) {
        val latitude = p0!!.position.latitude
        val longitude = p0.position.longitude
        namalokasi = showName(latitude, longitude)
        edt_namalokasianda.text = namalokasi
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }


}

