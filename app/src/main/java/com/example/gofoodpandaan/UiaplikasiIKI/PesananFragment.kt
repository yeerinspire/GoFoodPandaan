package com.example.gofoodpandaan.UiaplikasiIKI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gofoodpandaan.IkiOjek.TrackingOrderOjekActivity
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.TrackingOrderActivity
import com.example.gofoodpandaan.Model.Pesanan
import com.example.gofoodpandaan.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class PesananFragment : Fragment(), AnkoLogger {

    private lateinit var recyclerView: RecyclerView
    lateinit var refinfo: DatabaseReference
    lateinit var auth: FirebaseAuth
    var userID: String? = null
    lateinit var relativeLayout: RelativeLayout
    lateinit var constraint: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pesanan, container, false)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        relativeLayout = root.find(R.id.adapesanan)
        constraint = root.find(R.id.belum_ada_pesanan)

        constraint.visibility = View.VISIBLE
        relativeLayout.visibility = View.INVISIBLE

        //Pesanan//
        recyclerView = root.find(R.id.rv_prosespesanan)
        val LayoutManager = LinearLayoutManager(context!!.applicationContext)
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = LayoutManager
        refinfo =
            FirebaseDatabase.getInstance().reference.child("DaftarBooking").child(userID.toString())
        //=======================

        //reclerview komentar
        val option =
            FirebaseRecyclerOptions.Builder<Pesanan>().setQuery(refinfo, Pesanan::class.java)
                .build()
        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Pesanan, MyViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val itemView = LayoutInflater.from(context?.applicationContext)
                        .inflate(R.layout.daftarpesanan, parent, false)
                    return MyViewHolder(itemView)
                }

                override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Pesanan) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }


                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                constraint.visibility = View.INVISIBLE
                                relativeLayout.visibility = View.VISIBLE
                                holder.mstatus.text = "IKI-${model.status.toString()}"
                                 holder.itemView.setOnClickListener {
                                     if (model.status.equals("warung")){
                                         startActivity<TrackingOrderActivity>(
                                             "kunci" to model.uiddriver,
                                             "latitudeawal" to model.latitudePenumpang.toString(),
                                             "longitudeawal" to model.longitudePenumpang.toString(),
                                             "latitudetoko" to model.latitudeToko.toString(),
                                             "longitudetoko" to model.longitudeToko.toString(),
                                             "alamat" to model.namalokasi.toString(),
                                             "namapenjual" to model.namapenjual.toString(),
                                             "jarak" to model.jarak.toString(),
                                             "gambar" to model.gambar.toString(),
                                             "id" to model.id.toString(),
                                             "harga" to model.harga.toString()
                                         )                                     }
                                     else if (model.status.equals("ojek")){
                                         startActivity<TrackingOrderOjekActivity>(
                                             "kunci" to model.uiddriver,
                                             "latitudeawal" to model.latitudePenumpang,
                                             "longitudeawal" to model.longitudePenumpang,
                                             "latitudetujuan" to model.latitudeTujuan,
                                             "longitudetujuan" to model.longitudeTujuan,
                                             "kode" to model.kodeorder
                                         )

                                     }

                                }
                            }

                        }

                    })
                }
            }

        recyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

        return root
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mkodeorder: TextView = itemView.findViewById(R.id.kodeorder)
        var mstatus: TextView = itemView.findViewById(R.id.status)


    }

}