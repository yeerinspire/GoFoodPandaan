package com.example.gofoodpandaan.IkiWarung.FoodDelivery

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.directions.route.*
import com.example.gofoodpandaan.*
import com.example.gofoodpandaan.IkiOjek.IkiOjekActivity
import com.example.gofoodpandaan.IkiOjek.TrackingOrderOjekActivity
import com.example.gofoodpandaan.Model.ModelOrder
import com.example.gofoodpandaan.Model.ModelUsers
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
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.accept_order.*
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_cart.rv1
import kotlinx.android.synthetic.main.activity_detail_food.*
import kotlinx.android.synthetic.main.activity_iki_ojek.*
import kotlinx.android.synthetic.main.activity_pilih_mapswarung.*
import kotlinx.android.synthetic.main.content_map.*
import kotlinx.android.synthetic.main.content_mapwarung.*
import kotlinx.android.synthetic.main.content_mapwarung.btn_accept
import kotlinx.android.synthetic.main.content_mapwarung.ikiae
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

@Suppress("NAME_SHADOWING")
class CartActivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback, RoutingListener {
    lateinit var refinfo: DatabaseReference
    var hargatotal = 0
    var userID: String? = null
    var tanggal: String? = null
    var latAwal: Double? = null
    var lonAwal: Double? = null
    var latitude : String? = null
    var longitude : String? = null
    var latAkhir: Double? = null
    var lonAkhir: Double? = null
    var jarak: String? = null
    var waktufix: String? = null
    var dialog: Dialog? = null
    var lattoko: String? = null
    var lontoko: String? = null
    var uang: Int? = null
    var counter = 0
    var nilai: Int? = null
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
    var nama : String? = null
    var logikastatus = 0
    val TAG = "CartActivity"

    lateinit var sessionManager: SessionManager
    companion object {
        private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private val markerIconSize = 90
        private const val DEFAULT_ZOOM = 15
    }

    //search place
    lateinit var materialSearchBar: MaterialSearchBar
    private var placesClient: PlacesClient? = null
    lateinit var predictionList : List<AutocompletePrediction>
    private lateinit var peta: GoogleMap
    var logika= 0


    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAA1B1Ef7o:APA91bFzLzKZ3jrez02VjjiQ4sGDoUwDc6TyAph-8pfzfc-ifchkr8iYnFkpjXCK08LJhros4EbSvsvDdvzhy1fX5gNNGuqDIhQR8_vend8V6UEWh4LkMeNwYZy2-EM8izC2xLbrF0v2"
    private val contentType = "application/json"

    val booking = FirebaseDatabase.getInstance().reference.child("Booking")

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private var map: GoogleMap? = null

    var pendekatan = 0
    var jaraksebenarnya = 0f
    var ongkir: Int? = null
    var hargatransport: Int? = null

    lateinit var refDataFood : DatabaseReference
    lateinit var eventDataFood : ValueEventListener

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        val bundle: Bundle? = intent.extras
        lattoko = bundle!!.getString("Firebase_latakhir")
        lontoko = bundle.getString("Firebase_lonakhir")
        namapenjual = bundle.getString("firebase_penjual")
        namaCostumer = bundle.getString("firebase_namaCostumer")
        gambar = bundle.getString("Firebase_gambarMakanan")
        id = bundle.getString("firebase_id")
        latitude = bundle.getString("latitude")
        longitude = bundle.getString("longitude")
        latAkhir = lattoko.toString().toDouble()
        lonAkhir = lontoko.toString().toDouble()
        maps.onCreate(savedInstanceState)
        maps.getMapAsync(this)
        sessionManager = SessionManager(this)
        sessionManager.setlogicwarung("1")
        materialSearchBar = findViewById(R.id.searchplace)
        Findroutes(LatLng(latitude!!.toDouble(), longitude!!.toDouble()), LatLng(latAkhir!!, lonAkhir!!))
        nama = showName(latitude!!.toDouble(), longitude!!.toDouble())
        homeAwal.text = nama

        if (sum != null) {
            rv_2.visibility = View.INVISIBLE
            maps.visibility = View.INVISIBLE
        } else {
            rv_2.visibility = View.VISIBLE
            maps.visibility = View.VISIBLE

        }
        rv_2.visibility = View.INVISIBLE
        maps.visibility = View.INVISIBLE

        maps.setOnClickListener {
            toast("halo")
        }
        keyy?.let { bookingHistoryUser(it) }


        setVisible(false)
        val auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        Places.initialize(this@CartActivity, getString(R.string.google_maps_key))
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
                            RectangularBounds.newInstance(LatLng(-7.657273,112.696534),
                            LatLng(-7.608442,112.769147)
                        ))
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
                            map!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    Companion.DEFAULT_ZOOM.toFloat()
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

        homeAwal.onClick {
            rv1.visibility = View.INVISIBLE
            rv_2.visibility = View.VISIBLE
            maps.visibility = View.VISIBLE

        }


        btn_accept.setOnClickListener {
            maps.visibility = View.INVISIBLE
            rv_2.visibility = View.INVISIBLE
            rv1.visibility = View.VISIBLE
            val lat = map!!.cameraPosition.target.latitude
            val long = map!!.cameraPosition.target.longitude
            latitude = lat.toString()
            longitude = long.toString()
            latAwal = lat
            lonAwal = long
                map!!.clear()
                 nama = showName(latAwal!!, lonAwal!!)
                homeAwal.text = nama
            Findroutes(LatLng(latAwal!!, lonAwal!!), LatLng(latAkhir!!, lonAkhir!!))

        }
        foodpopular()
        datafood()
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

    var totalharga : Int? = null
    var total : Int? = null
    private fun datafood(){
        val list = arrayListOf<Int>()
         refDataFood = FirebaseDatabase.getInstance().getReference("Pandaan")
            .child("keranjang").child(userID.toString()).child(id.toString())
        eventDataFood = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list.clear()
                var totalist = 0
                for (ds in dataSnapshot.children) {
                    val bazar = ds.getValue(ModelUsers::class.java)
                    val cost: Int = Integer.valueOf(bazar!!.hargatotal.toString())
                    totalist = totalist + cost

                }

                Findroutes(LatLng(latitude!!.toDouble(), longitude!!.toDouble()), LatLng(latAkhir!!, lonAkhir!!))
                if (ongkir!=null){
                    foodprice.text = totalist.toString()
                    totalharga = totalist + ongkir!!
                    total = totalist
                    totalprice.text = "Rp. $totalharga"
                }
                else{
                    datafood()
                }


                btn_proses.setOnClickListener {
                    logikastatus = 1
                    if (latitude != null && longitude != null) {
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
        }
        refDataFood.addValueEventListener(eventDataFood)
    }
    private fun showDialog(status: Boolean) {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.tunggudriver)

        if (status) {
            dialog?.show()
        } else dialog?.dismiss()


    }

    var sum = 0
    var alfan = 0
    private fun foodpopular() {
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_cart.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("keranjang")
            .child(userID.toString()).child(id.toString())
        val option =
            FirebaseRecyclerOptions.Builder<ModelUsers>().setQuery(refinfo, ModelUsers::class.java)
                .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val itemView = LayoutInflater.from(this@CartActivity)
                        .inflate(R.layout.cart_sqlite, parent, false)
                    return MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var jumlah =
                                model.harga.toString().toInt() * model.jumlah.toString().toInt()
                            var counter = model.jumlah.toString().toInt()
                            holder.buttonmin.setOnClickListener {
                                if (counter > 0) {
                                    counter -= 1
                                    var harga = model.harga.toString().toInt()
                                    var data = harga * counter
                                    hargatotal -= data
                                    holder.mprice.text = data.toString()
                                    holder.counter.text = counter.toString()
                                    foodprice.text = "Rp. $hargatotal"
                                    var ref = FirebaseDatabase.getInstance().getReference("Pandaan")
                                        .child("keranjang").child(userID.toString())
                                        .child(id.toString())
                                        .child(model.nama.toString()).child("jumlah")
                                        .setValue(counter.toString())
                                    var ref2 =
                                        FirebaseDatabase.getInstance().getReference("Pandaan")
                                            .child("keranjang").child(userID.toString())
                                            .child(id.toString())
                                            .child(model.nama.toString()).child("hargatotal")
                                            .setValue(data)
                                }
                            }

                            holder.buttonplus.setOnClickListener {
                                counter += 1
                                var harga = model.harga.toString().toInt()
                                var data = harga * counter
                                holder.mprice.text = data.toString()
                                holder.counter.text = counter.toString()
                                foodprice.text = "Rp. $hargatotal"

                                hargatotal += data
                                var ref = FirebaseDatabase.getInstance().getReference("Pandaan")
                                    .child("keranjang").child(userID.toString())
                                    .child(id.toString())
                                    .child(model.nama.toString()).child("jumlah")
                                    .setValue(counter.toString())
                                var ref2 = FirebaseDatabase.getInstance().getReference("Pandaan")
                                    .child("keranjang").child(userID.toString())
                                    .child(id.toString())
                                    .child(model.nama.toString()).child("hargatotal")
                                    .setValue(data)


                            }

                            holder.btnhapus.setOnClickListener {
                                var ref = FirebaseDatabase.getInstance().getReference("Pandaan")
                                    .child("keranjang").child(userID.toString())
                                    .child(id.toString())
                                    .child(model.nama.toString()).removeValue()
                            }
                            holder.mtitle.setText(model.nama)
                            holder.counter.setText(model.jumlah)
                            holder.mprice.text = "Rp. ${jumlah}"

                            Picasso.get().load(model.foto).fit().into(holder.fotomakanan)
                        }

                    })
                }
            }


        rv_cart.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

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

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.namamakanan)
        var mprice: TextView = itemView.findViewById(R.id.txt_hargamakanan)
        var mOwner: TextView = itemView.findViewById(R.id.txt_catatan)
        var counter: TextView = itemView.findViewById(R.id.counter)
        var buttonmin: ImageView = itemView.findViewById(R.id.btn_min)
        var buttonplus: ImageView = itemView.findViewById(R.id.btn_plus)
        var fotomakanan: ImageView = itemView.findViewById(R.id.fotomakanan)
        var btnhapus: ImageView = itemView.findViewById(R.id.btn_hapus)


    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.uiSettings?.isMyLocationButtonEnabled = false
        val zoom = CameraUpdateFactory.zoomTo(16F)

        val pasuruan = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        map!!.moveCamera(CameraUpdateFactory.newLatLng(pasuruan))
        map!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                pasuruan,
                DEFAULT_ZOOM.toFloat()
            )
        )

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
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
            homeAwal.text.toString(),
            latAwal,
            lonAwal,
            homeAwal.text.toString(),
            latAkhir,
            lonAkhir,
            totalharga.toString(),
            jarak.toString(),
            id.toString()
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
        jarak: String,
        idtoko : String
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
        booking.idtoko = idtoko


        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_Booking)
        keyy = database.reference.push().key
        myRef.child(userID.toString()).child(id.toString()).child("latitudeToko").setValue(latAkhir.toString())
        myRef.child(userID.toString()).child(id.toString()).child("longitudeToko").setValue(lonAkhir.toString())
        myRef.child(userID.toString()).child(id.toString()).child("namatoko").setValue(namapenjual.toString())
        myRef.child(userID.toString()).child(id.toString()).child("penumpang").setValue(namaCostumer.toString())
        myRef.child(userID.toString()).child(id.toString()).child("latitudePenumpang").setValue(latitude.toString())
        myRef.child(userID.toString()).child(id.toString()).child("longitudePenumpang").setValue(longitude.toString())
        myRef.child(userID.toString()).child(id.toString()).child("harga").setValue(totalharga.toString())
        myRef.child(userID.toString()).child(id.toString()).child("ongkir").setValue(ongkir.toString())
        myRef.child(userID.toString()).child(id.toString()).child("notelpon").setValue(sessionManager.gettelefon().toString())
        myRef.child(userID.toString()).child(id.toString()).child("idtoko").setValue(id.toString())
        myRef.child(userID.toString()).child(id.toString()).child("uidpelanggan").setValue(userID.toString())
        myRef.child(userID.toString()).child(id.toString()).child("hargapelanggan").setValue(total.toString())
        myRef.child(userID.toString()).child(id.toString()).child("area").setValue(showArea(latTujuan!!.toDouble(),lonTujuan!!.toDouble()))



        showDialog(true)
        mencaridriverterdekat()

/*

        val hei = keyy

        hei?.let { bookingHistoryUser(it) }
        myRef.child(keyy ?: "").setValue(booking)
*/


        return true
    }

    val listener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
            TODO("Not yet implemented")
        }
        override fun onDataChange(p0: DataSnapshot) {
            val data = p0.child(userID.toString()).child(id.toString()).child("status").value.toString()
            if (data.equals(driverID.toString())){
                showDialog(false)
                val usermap: java.util.HashMap<String, Any?> = java.util.HashMap()
                usermap["status"] = "warung"
                usermap["kodeorder"] = kode
                usermap["uidku"] = userID.toString()
                usermap["uiddriver"] = driverID.toString()
                usermap["latitudePenumpang"] = latitude.toString()
                usermap["longitudePenumpang"] = longitude.toString()
                usermap["latitudeToko"] = latAkhir.toString()
                usermap["longitudeToko"] = lonAkhir.toString()
                usermap["harga"] = totalharga.toString()
                usermap["ongkir"] = ongkir.toString()
                usermap["notelpon"] = "085655021997"
                usermap["alamat"] = namalokasi
                usermap["namapenjual"] = namapenjual
                usermap["jarak"] = jarak
                usermap["gambar"] = gambar
                usermap["id"] = id
                if (logika==0){
                    val ref =
                        FirebaseDatabase.getInstance().getReference("DaftarBooking")
                            .child(userID.toString()).child(id.toString()).setValue(usermap)
                    logika = 1
                }
                toast(radius.toString())
                sessionManager.setlogicwarung("2")
                startActivity<TrackingOrderActivity>(
                    "kunci" to driverID,
                    "latitudeawal" to latitude.toString(),
                    "longitudeawal" to longitude.toString(),
                    "latitudetoko" to latAkhir.toString(),
                    "longitudetoko" to lonAkhir.toString(),
                    "alamat" to namalokasi,
                    "namapenjual" to namapenjual,
                    "jarak" to jarak,
                    "gambar" to gambar,
                    "id" to id,
                    "harga" to totalharga.toString()
                )
                finish()


            }
        }

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

    private fun mencaridriverterdekat() {
        val posisiDriver =
            FirebaseDatabase.getInstance().reference.child("Driver").child("DriverTersedia")
        val geoFire = GeoFire(posisiDriver)
        geoQuery = geoFire.queryAtLocation(GeoLocation(latAkhir!!, lonAkhir!!), radius)
        geoQuery.removeAllListeners()
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
                if (!driverFound && radius<8.0) {
                    radius++
                    Log.d("here", "here")
                    mencaridriverterdekat()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!driverFound && requestBol) {
                    driverFound = true
                    driverID = key!!
                if (sessionManager.getlogicwarung().equals("1")){
                    val hapusdriverTersedia: DatabaseReference =
                        FirebaseDatabase.getInstance().reference
                    hapusdriverTersedia.child("Driver").child("DriverTersedia")
                        .child(driverID.toString()).removeValue()
                    if (driverID!=null){
                        ams()
                        kirimpesan("IKIJEK","ada orderan siap siap yaa")
                    }
                    else{
                        ams()
                        kirimpesan("IKIJEK","ada orderan siap siap yaa")
                    }
                    if (logikastatus==1){
                        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Pandaan")
                        db.child("Akun_Driver").child(driverID.toString()).child("status").setValue(userID)
                        db.child("Akun_Driver").child(driverID.toString()).child("kodeorder").setValue(kode)
                    }


                    val db: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Pandaan")
                    db.child("Akun_Driver").child(driverID.toString()).child("status")
                        .setValue(userID)
                    db.child("Akun_Driver").child(driverID.toString()).child("uidtoko")
                        .setValue(id.toString())


                    val booking: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Booking")
                    booking.addValueEventListener(listener)

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

        })


    }


    fun kodeorder(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val outputStrLength = (20..36).shuffled().first()

        return (1..outputStrLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    var data = ""
    var pesanalfan : String? = null
    val kode = kodeorder()

    private fun ams(){
        val tokendriver  = FirebaseDatabase.getInstance().getReference("Driver")
        tokendriver.child("DriverTersedia").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                data = p0.child(driverID.toString()).child("token").value.toString()
                pesanalfan = data
                kirimpesan("IKI-WARUNG","ada orderan siap siap yaa")
            }

        })
    }

    fun kirimpesan(judul : String, pesan : String){
        val recipientToken = "$data"
        if(judul.isNotEmpty() && pesan.isNotEmpty() && recipientToken.isNotEmpty()) {
            PushNotifikasi(
                NotifikasiData(judul, pesan),
                recipientToken
            ).also {
                sendNotification(it)
            }
        }
    }

    private fun sendNotification(notification: PushNotifikasi) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
        if (logikastatus ==1){
            geoQuery.removeAllListeners()
            logikastatus =0
        }
        sessionManager.setlogicwarung("2")


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
        if (logikastatus==1){
            geoQuery.removeAllListeners()
            logikastatus =0

        }
        sessionManager.setlogicwarung("2")
        booking.removeEventListener(listener)
        refDataFood.removeEventListener(eventDataFood)
        finishAndRemoveTask()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            toast("tidak dapat mendapatkan aplikasi")
        } else {
            val routing: Routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .avoid(AbstractRouting.AvoidKind.TOLLS)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyADQdBkk1SNyX7jWXRZFlJQz8TWT-M-TeE") //also define your api key here.
                .build()
            routing.execute()
        }
    }

    override fun onRoutingCancelled() {
        Findroutes(LatLng(latitude!!.toDouble(), longitude!!.toDouble()), LatLng(latAkhir!!, lonAkhir!!))

    }
    override fun onRoutingStart() {
    }

    override fun onRoutingFailure(p0: RouteException?) {
        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, p0.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()

    }

    override fun onRoutingSuccess(routes: ArrayList<Route>?, shortestRouteIndex: Int) {
        for (i in 0 until routes!!.size) {
            if (i == shortestRouteIndex) {
                jaraksebenarnya = routes.get(i).distanceValue.toString().toFloat() / 1000
                pendekatan = Math.round(jaraksebenarnya)
                if (pendekatan <= 5) {
                    ongkir = 7000
                    shipping.text = ongkir.toString()
                } else if (pendekatan > 5) {
                    hargatransport = 2000
                    ongkir = pendekatan * hargatransport!! - 3000
                    shipping.text = ongkir.toString()

                }
            } else {
            }
        }

    }

}