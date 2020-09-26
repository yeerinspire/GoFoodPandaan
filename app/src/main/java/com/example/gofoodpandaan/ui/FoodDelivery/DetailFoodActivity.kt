package com.example.gofoodpandaan.ui.FoodDelivery

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.ChangeFormat
import com.example.gofoodpandaan.GPSTracker
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail_food.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import java.util.*

class DetailFoodActivity : AppCompatActivity(), AnkoLogger {
    lateinit var sessionManager: SessionManager
    lateinit var refinfo: DatabaseReference
    var penjual: String? = null
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_food)
        showPermission()
        sessionManager = SessionManager(this)
        val bundle: Bundle? = intent.extras
        id = bundle!!.getString("firebase_id")
        gambar = bundle.getString("Firebase_Image")
        tulisan = bundle.getString("Firebase_title")
        harga = bundle.getString("firebase_harga")
        penjual = bundle.getString("firebase_penjual")
        foodpopular()
        minuman()
        ambildata()
        Picasso.get().load(gambar).into(foto)
        txt_namatoko.text = penjual



        fab.setOnClickListener {
            var latitude : String
            var longitude : String
            latitude = latAkhir.toString()
            longitude = lonAkhir.toString()
            startActivity<CartActivity>(
                "Firebase_latakhir" to latitude,
                "Firebase_lonakhir" to longitude,
                "firebase_penjual" to penjual
            )
        }
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
            object : FirebaseRecyclerAdapter<ModelUsers, FoodFragment.MyViewHolder>(newOptions) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FoodFragment.MyViewHolder {
                    val itemView = LayoutInflater.from(this@DetailFoodActivity)
                        .inflate(R.layout.list_item, parent, false)
                    return FoodFragment.MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: FoodFragment.MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            holder.mtitle.setText(model.nama)
                            Picasso.get().load(model.gambar).fit().centerCrop().into(holder.mimage)
                            holder.mharga.setText(model.harga)
                            holder.itemView.setOnClickListener {
                                startActivity<TambahDetailCartActivity>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "firebase_id" to model.id,
                                    "firebase_harga" to model.harga,
                                    "firebase_kode" to model.kode
                                )

                            }
                        }

                    })
                }
            }


        rv_makanan.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    private fun minuman() {
        val LayoutManager = LinearLayoutManager(this)
        LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rv_minuman.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
            .child(id.toString())

        //3. SELECT * FROM Artists WHERE country = "India"
        val query3 =
            FirebaseDatabase.getInstance().reference.child("Pandaan").child("User_Resto")
                .child(id.toString())
                .orderByChild("kategori")
                .equalTo("Minuman")

        val newOptions = FirebaseRecyclerOptions.Builder<ModelUsers>()
            .setQuery(query3, ModelUsers::class.java)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, FoodFragment.MyViewHolder>(newOptions) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FoodFragment.MyViewHolder {
                    val itemView = LayoutInflater.from(this@DetailFoodActivity)
                        .inflate(R.layout.list_item, parent, false)
                    return FoodFragment.MyViewHolder(
                        itemView
                    )
                }

                override fun onBindViewHolder(
                    holder: FoodFragment.MyViewHolder,
                    position: Int,
                    model: ModelUsers
                ) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            holder.mtitle.setText(model.nama)
                            Picasso.get().load(model.gambar).fit().centerCrop().into(holder.mimage)
                            holder.mharga.setText(model.harga)
                            holder.itemView.setOnClickListener {
                                startActivity<TambahDetailCartActivity>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "firebase_id" to id,
                                    "firebase_harga" to model.harga,
                                    "Firebase_latakhir" to latAkhir.toString(),
                                    "Firebase_lonakhir" to lonAkhir.toString(),
                                    "firebase_penjual" to penjual

                                )
                            }
                        }

                    })
                }
            }


        rv_minuman.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    private fun showData(routes: List<RoutesItem?>?) {


        if (routes != null) {

            jarak = routes[0]?.legs?.get(0)?.distance?.text
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val waktu = routes[0]?.legs?.get(0)?.duration?.text

            jaraktoko.text = waktu + " ( " + jarak + " )"

            val pricex = jarakValue?.toDouble()?.let { Math.round(it) }


            val price = pricex?.div(1000.0)?.times(2000.0)

            val price2 = ChangeFormat.toRupiahFormat2(price.toString())
            ongkirtoko.text = "Rp. " + price2

        } else {
            alert {
                message = "Data Route Null"
            }.show()
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

    private fun showGps() {

        val gps = GPSTracker(this)
        if (gps.canGetLocation()) {
            latAwal = gps.latitude
            lonAwal = gps.longitude

            val name = showName(latAwal ?: 0.0, lonAwal ?: 0.0)

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

                route()

            }

        })
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


}