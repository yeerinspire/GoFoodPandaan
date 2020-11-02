package com.example.gofoodpandaan.IkiWarung.FoodDelivery

import android.app.Dialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.directions.route.*
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.Utlis.RoundedCornersTransformation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.singpaulee.example_sqlite_kotlin.StudentAdapter
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_detail_food.*
import kotlinx.android.synthetic.main.bottomsheet_cart.*
import kotlinx.android.synthetic.main.bottomsheet_tambahmakanan.*
import org.jetbrains.anko.*
import java.util.*

@Suppress("PLUGIN_WARNING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailFoodActivity : AppCompatActivity(), AnkoLogger, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener, RoutingListener {
    lateinit var sessionManager: SessionManager
    lateinit var refinfo: DatabaseReference
    lateinit var auth: FirebaseAuth
    var userID: String? = null
    var penjual: String? = null
    var namacostumer: String? = null
    var fotocostumer: String? = null
    var alamat: String? = null
    var id: String? = null
    var gambar: String? = null
    var tulisan: String? = null
    var harga: String? = null
    var jarak: String? = null
    var latAwal: Double? = null
    var lonAwal: Double? = null
    var latAkhir: Double? = null
    var lonAkhir: Double? = null

    var pendekatan = 0
    var jaraksebenarnya = 0f
    var hargaongkir: Int? = null
    var hargatransport: Int? = null

    private lateinit var bottomSheet: BottomSheetBehavior<*>
    private lateinit var bottomSheetTambah: BottomSheetBehavior<*>
    private var LOCATION_REQUEST_CODE = 1

    //lokasi
    private lateinit var locationRequest: LocationRequest
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var lastLocation: Location
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var progressDialog: Dialog


    var adapter: StudentAdapter? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_food)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Sedang mencari lokasi")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        permission()
        bottomSheet = BottomSheetBehavior.from(bottomsheet_detail_order)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetTambah = BottomSheetBehavior.from(bottomsheet_tambah)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        sessionManager = SessionManager(this)
        val bundle: Bundle? = intent.extras
        id = bundle!!.getString("firebase_idMakanan")
        gambar = bundle.getString("Firebase_gambarMakanan")
        tulisan = bundle.getString("Firebase_Makanan")
        harga = bundle.getString("firebase_hargaMakanan")
        penjual = bundle.getString("firebase_penjual")
        namacostumer = bundle.getString("firebase_namaCostumer")
        buildGoogleApiClient()
        foodpopular()
        minuman()
        ambildata()
        Picasso.get().load(gambar).fit().centerCrop().into(foto)
        txt_namatoko.text = penjual
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        cartpopular()
        hargatotal()
        adaorder()

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            card_tagih.visibility = View.VISIBLE
        }
        scrollView.maxScrollAmount

        btn_detail.setOnClickListener {
            if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        btn_checkout.setOnClickListener {
            if (latAwal!=null && lonAwal!=null){
                var latitude: String
                var longitude: String
                latitude = latAkhir.toString()
                longitude = lonAkhir.toString()
                startActivity(intentFor<CartActivity>("Firebase_latakhir" to latitude,
                    "Firebase_lonakhir" to longitude,
                    "firebase_penjual" to penjual,
                    "firebase_id" to id,
                    "firebase_namaCostumer" to namacostumer,
                    "Firebase_gambarMakanan" to gambar,
                    "latitude" to latAwal.toString(),
                    "longitude" to lonAwal.toString()
                ).newTask())

            }
        }
    }

    private fun adaorder() {
        val query3 =
            FirebaseDatabase.getInstance().reference.child("DaftarBooking").child(userID.toString()).orderByChild("id")
                .equalTo(id.toString())
        query3.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    startActivity(intentFor<HomeActivity>().clearTask().newTask())
                    toast("anda telah order di toko yang sama")
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
    }

    private fun foodpopular() {
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rv_makanan.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
            .child(id.toString())

        val query3 =
            FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
                .child(id.toString())
                .orderByChild("kategori")
                .equalTo("Makanan")

        val newOptions = FirebaseRecyclerOptions.Builder<ModelUsers>()
            .setQuery(query3, ModelUsers::class.java)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(newOptions) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): MyViewHolder {
                    val itemView = LayoutInflater.from(this@DetailFoodActivity)
                        .inflate(R.layout.list_item_toko, parent, false)
                    return MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val radius = 30
                    val margin = 10
                    val transformation: Transformation =
                        RoundedCornersTransformation(radius, margin)

                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            holder.mtitle.text = model.nama
                            holder.mharga.text = model.harga
                            holder.mketerangan.text = model.keterangan
                            Picasso.get().load(model.gambar).transform(transformation).fit()
                                .into(holder.mimage)
                            holder.itemView.setOnClickListener {
                                var counter = 0
                                var nilai: Int? = null
                                if (bottomSheetTambah.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetTambah.state == BottomSheetBehavior.STATE_HIDDEN) {
                                    bottomSheetTambah.state = BottomSheetBehavior.STATE_EXPANDED
                                    card_tagih.visibility = View.INVISIBLE
                                } else {
                                    bottomSheetTambah.state = BottomSheetBehavior.STATE_HIDDEN
                                    card_tagih.visibility = View.VISIBLE

                                }
                                namebottom.text = model.nama.toString()
                                Picasso.get().load(model.gambar).fit().into(fotocart)
                                hargacart.text = "Rp. ${model.harga.toString()}"
                                val nama = model.nama.toString()
                                var hargatotal = counter + model.harga.toString().toInt()
                                btn_downbottom.setOnClickListener {
                                    if (counter <= 0) {
                                        counter == 0
                                    } else {
                                        counter = counter - 1
                                    }
                                    nilai = model.harga.toString().toInt() * counter
                                    txt_counterbottom.text = counter.toString()
                                    hargatotalbottom.text = "Rp. ${nilai.toString()}"

                                }

                                btn_upbottom.setOnClickListener {
                                    counter += 1
                                    nilai = model.harga.toString().toInt() * counter
                                    txt_counterbottom.text = counter.toString()
                                    hargatotalbottom.text = "Rp. ${nilai.toString()}"
                                }

                                tambahbottom.setOnClickListener {
                                    /*      database.use {
                                              insert(CartSQlite.TABLE_STUDENT,
                                                  CartSQlite.NAME to model.nama.toString(),
                                                  CartSQlite.HARGA to model.harga.toString().toInt(),
                                                  CartSQlite.ADDRESS to "sdf",
                                                  CartSQlite.PHOTO to null,
                                                  CartSQlite.MAJORITY to "asd"
                                              )
                                              val listRefresh = getListDataStudent()
                                              adapter = StudentAdapter(this@DetailFoodActivity, listRefresh)
                                              adapter?.notifyDataSetChanged()
                                              rv_selectproduct.adapter = adapter
                                              toast("Berhasil menambah data siswa baru")
                                            }*/
                                    val usermap: MutableMap<String, Any?> = HashMap()
                                    usermap["nama"] = model.nama.toString()
                                    usermap["harga"] = model.harga.toString()
                                    usermap["jumlah"] = counter.toString()
                                    usermap["foto"] = model.gambar.toString()
                                    usermap["hargatotal"] = model.harga.toString().toInt() * counter
                                    if (counter == 0) {
                                        val ref =
                                            FirebaseDatabase.getInstance().reference.child("Pandaan")
                                                .child("keranjang").child(userID.toString())
                                                .child(model.nama.toString())
                                        ref.removeValue()
                                    } else {
                                        val reftambah =
                                            FirebaseDatabase.getInstance().reference.child("Pandaan")
                                                .child("keranjang").child(userID.toString())
                                                .child(id.toString())
                                        reftambah.child(model.nama.toString()).setValue(usermap)
                                            .addOnCompleteListener {
                                                if (it.isComplete) {
                                                    toast("berhasil")
                                                    bottomSheetTambah.state =
                                                        BottomSheetBehavior.STATE_HIDDEN
                                                    card_tagih.visibility = View.VISIBLE
                                                } else {
                                                    toast("Pastikan koneksi anda stabil")
                                                }
                                            }
                                    }
                                }
                            }
                        }

                    })
                }
            }


        rv_makanan.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    private fun cartpopular() {
        var hargatotal = 0
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_selectproduct.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("keranjang")
            .child(userID.toString()).child(id.toString())
        val newOptions = FirebaseRecyclerOptions.Builder<ModelUsers>()
            .setQuery(refinfo, ModelUsers::class.java)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, CartHolder>(newOptions) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): CartHolder {
                    val itemView = LayoutInflater.from(this@DetailFoodActivity)
                        .inflate(R.layout.cart_sqlite, parent, false)
                    return CartHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: CartHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val radius = 30
                    val margin = 10
                    val transformation: Transformation =
                        RoundedCornersTransformation(radius, margin)
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
                                    total_price.text = "Rp. $hargatotal"
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
                                total_price.text = "Rp. $hargatotal"

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
        rv_selectproduct.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    private fun minuman() {
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rv_minuman.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
            .child(id.toString())

        val query3 =
            FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
                .child(id.toString())
                .orderByChild("kategori")
                .equalTo("Minuman")

        val newOptions = FirebaseRecyclerOptions.Builder<ModelUsers>()
            .setQuery(query3, ModelUsers::class.java)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(newOptions) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): MyViewHolder {
                    val itemView = LayoutInflater.from(this@DetailFoodActivity)
                        .inflate(R.layout.list_item_toko, parent, false)
                    return MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val radius = 30
                    val margin = 10
                    val transformation: Transformation =
                        RoundedCornersTransformation(radius, margin)

                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            holder.mtitle.text = model.nama
                            holder.mharga.text = model.harga
                            holder.mketerangan.text = model.keterangan
                            Picasso.get().load(model.gambar).transform(transformation).fit()
                                .into(holder.mimage)
                            holder.itemView.setOnClickListener {
                                var counter = 0
                                var nilai: Int? = null
                                if (bottomSheetTambah.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetTambah.state == BottomSheetBehavior.STATE_HIDDEN) {
                                    bottomSheetTambah.state = BottomSheetBehavior.STATE_EXPANDED
                                    card_tagih.visibility = View.INVISIBLE
                                } else {
                                    bottomSheetTambah.state = BottomSheetBehavior.STATE_HIDDEN
                                    card_tagih.visibility = View.VISIBLE

                                }
                                namebottom.text = model.nama.toString()
                                Picasso.get().load(model.gambar).fit().into(fotocart)
                                hargacart.text = "Rp. ${model.harga.toString()}"
                                val nama = model.nama.toString()
                                var hargatotal = counter + model.harga.toString().toInt()
                                btn_downbottom.setOnClickListener {
                                    if (counter <= 0) {
                                        counter == 0
                                    } else {
                                        counter = counter - 1
                                    }
                                    nilai = model.harga.toString().toInt() * counter
                                    txt_counterbottom.text = counter.toString()
                                    hargatotalbottom.text = "Rp. ${nilai.toString()}"

                                }

                                btn_upbottom.setOnClickListener {
                                    counter += 1
                                    nilai = model.harga.toString().toInt() * counter
                                    txt_counterbottom.text = counter.toString()
                                    hargatotalbottom.text = "Rp. ${nilai.toString()}"
                                }

                                tambahbottom.setOnClickListener {
                                    /*      database.use {
                                              insert(CartSQlite.TABLE_STUDENT,
                                                  CartSQlite.NAME to model.nama.toString(),
                                                  CartSQlite.HARGA to model.harga.toString().toInt(),
                                                  CartSQlite.ADDRESS to "sdf",
                                                  CartSQlite.PHOTO to null,
                                                  CartSQlite.MAJORITY to "asd"
                                              )
                                              val listRefresh = getListDataStudent()
                                              adapter = StudentAdapter(this@DetailFoodActivity, listRefresh)
                                              adapter?.notifyDataSetChanged()
                                              rv_selectproduct.adapter = adapter
                                              toast("Berhasil menambah data siswa baru")
                                            }*/
                                    val usermap: MutableMap<String, Any?> = HashMap()
                                    usermap["nama"] = model.nama.toString()
                                    usermap["harga"] = model.harga.toString()
                                    usermap["jumlah"] = counter.toString()
                                    usermap["foto"] = model.gambar.toString()
                                    usermap["hargatotal"] = model.harga.toString().toInt() * counter
                                    if (counter == 0) {
                                        val ref =
                                            FirebaseDatabase.getInstance().reference.child("Pandaan")
                                                .child("keranjang").child(userID.toString())
                                                .child(model.nama.toString())
                                        ref.removeValue()
                                    } else {
                                        val reftambah =
                                            FirebaseDatabase.getInstance().reference.child("Pandaan")
                                                .child("keranjang").child(userID.toString())
                                                .child(id.toString())
                                        reftambah.child(model.nama.toString()).setValue(usermap)
                                            .addOnCompleteListener {
                                                if (it.isComplete) {
                                                    toast("berhasil")
                                                    bottomSheetTambah.state =
                                                        BottomSheetBehavior.STATE_HIDDEN
                                                    card_tagih.visibility = View.VISIBLE
                                                } else {
                                                    toast("Pastikan koneksi anda stabil")
                                                }
                                            }
                                    }
                                }
                            }
                        }

                    })
                }
            }


        rv_minuman.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    private fun hargatotal() {
        val list = arrayListOf<Int>()
        var ref = FirebaseDatabase.getInstance().getReference("Pandaan")
            .child("keranjang").child(userID.toString()).child(id.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list.clear()
                var total = 0
                for (ds in dataSnapshot.children) {
                    val bazar = ds.getValue(ModelUsers::class.java)
                    val cost: Int = Integer.valueOf(bazar!!.hargatotal.toString())
                    total = total + cost

                }
                info { "alfan $total" }
                total_price.text = total.toString()
            }


        })
    }

    private fun permission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {

            toast("sudah diijinkan")
        }
    }


    fun ambildata() {
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("Akun_Resto")
            .child(id.toString())
        refinfo.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                var ambildata = p0.getValue(ModelUsers::class.java)
                latAkhir = ambildata?.latitude.toString().toDouble()
                lonAkhir = ambildata?.longitude.toString().toDouble()

            }

        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Please provide the permission for map", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }


    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient
            .Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient.connect()
    }

    override fun onConnected(p0: Bundle?) {
        locationRequest = LocationRequest()
            .setInterval(1000)
            .setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
        else
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        latAwal = lastLocation.latitude
        lonAwal = lastLocation.longitude
            if (latAwal!=null && lonAwal!=null){
                Findroutes(LatLng(latAwal!!, lonAwal!!), LatLng(latAkhir!!, lonAkhir!!))
            }
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.name)
        var mimage: ImageView = itemView.findViewById(R.id.gambar_makanan)
        var mharga: TextView = itemView.findViewById(R.id.hargamakanan)
        var mketerangan: TextView = itemView.findViewById(R.id.keterangan)
    }

    class CartHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.namamakanan)
        var mprice: TextView = itemView.findViewById(R.id.txt_hargamakanan)
        var mOwner: TextView = itemView.findViewById(R.id.txt_catatan)
        var counter: TextView = itemView.findViewById(R.id.counter)
        var buttonmin: ImageView = itemView.findViewById(R.id.btn_min)
        var buttonplus: ImageView = itemView.findViewById(R.id.btn_plus)
        var fotomakanan: ImageView = itemView.findViewById(R.id.fotomakanan)
        var btnhapus: ImageView = itemView.findViewById(R.id.btn_hapus)

    }

    override fun onRoutingCancelled() {
        Findroutes(LatLng(latAwal!!, lonAwal!!), LatLng(latAkhir!!, lonAkhir!!))
    }

    override fun onRoutingStart() {

    }

    override fun onRoutingFailure(p0: RouteException?) {
        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, p0.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()

    }

    //polyline object
    private var polylines: List<Polyline>? = null

    override fun onRoutingSuccess(routes: ArrayList<Route>?, shortestRouteIndex: Int) {
        for (i in 0 until routes!!.size) {
            if (i == shortestRouteIndex) {
                jaraksebenarnya = routes.get(i).distanceValue.toString().toFloat() / 1000
                pendekatan = Math.round(jaraksebenarnya)
                distance.text = routes.get(i).distanceText.toString()
                if (pendekatan <= 5) {
                    hargaongkir = 9000
                    price_ongkir.text = hargaongkir.toString()
                } else if (pendekatan > 5) {
                    hargatransport = 2000
                    hargaongkir = pendekatan * hargatransport!! - 1000
                    price_ongkir.text = hargaongkir.toString()

                }

                progressDialog.dismiss()
            } else {
            }
        }
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


    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onStart() {
        super.onStart()
        LOCATION_REQUEST_CODE = 1
    }

}

