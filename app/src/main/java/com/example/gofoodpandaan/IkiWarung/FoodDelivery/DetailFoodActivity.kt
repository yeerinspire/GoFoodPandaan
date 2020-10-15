package com.example.gofoodpandaan.IkiWarung.FoodDelivery

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.ChangeFormat
import com.example.gofoodpandaan.GPSTracker
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.Network.NetworkModule
import com.example.gofoodpandaan.Network.ResultRoute
import com.example.gofoodpandaan.Network.RoutesItem
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.Utlis.RoundedCornersTransformation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail_food.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import java.util.*

class DetailFoodActivity : AppCompatActivity(), AnkoLogger {
    lateinit var sessionManager: SessionManager
    lateinit var refinfo: DatabaseReference
    lateinit var auth: FirebaseAuth
    var userID : String? = null
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_food)
        showPermission()
        sessionManager = SessionManager(this)
        val bundle: Bundle? = intent.extras
        id = bundle!!.getString("firebase_idMakanan")
        gambar = bundle.getString("Firebase_gambarMakanan")
        tulisan = bundle.getString("Firebase_Makanan")
        harga = bundle.getString("firebase_hargaMakanan")
        penjual = bundle.getString("firebase_penjual")
        namacostumer = bundle.getString("firebase_namaCostumer")
        foodpopular()
        minuman()
        ambildata()
        Picasso.get().load(gambar).fit().into(foto)
        txt_namatoko.text = penjual
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        carticon(userID.toString())

        fab.setOnClickListener {
            var latitude : String
            var longitude : String
            latitude = latAkhir.toString()
            longitude = lonAkhir.toString()
            startActivity<CartActivity>(
                "Firebase_latakhir" to latitude,
                "Firebase_lonakhir" to longitude,
                "firebase_penjual" to penjual,
                "firebase_id" to id,
                "firebase_namaCostumer" to namacostumer,
                "Firebase_gambarMakanan" to gambar
            )
        }
    }

    private fun carticon(userid : String) {
        val reference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Pandaan").child("keranjang").child(userid).child(id.toString())
        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    fab.visibility = View.VISIBLE
                    faboff.visibility = View.INVISIBLE
                }
                else{
                    fab.visibility = View.INVISIBLE
                    faboff.visibility = View.VISIBLE

                }
            }

        })
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
                    val transformation: Transformation = RoundedCornersTransformation(radius, margin)

                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            holder.mtitle.text = model.nama
                            holder.mharga.text = model.harga
                            holder.mketerangan.text = model.keterangan
                            Picasso.get().load(model.gambar).transform(transformation).fit().into(holder.mimage)
                            holder.itemView.setOnClickListener {
                                startActivity<TambahDetailCartActivity>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "firebase_id" to id.toString(),
                                    "firebase_harga" to model.harga,
                                    "Firebase_latakhir" to latAkhir.toString(),
                                    "Firebase_lonakhir" to lonAkhir.toString(),
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
                    val transformation: Transformation = RoundedCornersTransformation(radius, margin)
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }
                        override fun onDataChange(p0: DataSnapshot) {
                            holder.mtitle.text = model.nama
                            holder.mharga.text = model.harga
                            holder.mketerangan.text = model.keterangan
                            Picasso.get().load(model.gambar).transform(transformation).fit().into(holder.mimage)

                            holder.itemView.setOnClickListener {
                                startActivity<TambahDetailCartActivity>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "firebase_id" to id.toString(),
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

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.name)
        var mimage: ImageView = itemView.findViewById(R.id.gambar_makanan)
        var mharga: TextView = itemView.findViewById(R.id.hargamakanan)
        var mketerangan: TextView = itemView.findViewById(R.id.keterangan)
    }



}