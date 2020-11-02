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
import com.example.gofoodpandaan.DetailRiwayatOjekActivity
import com.example.gofoodpandaan.DetailRiwayatWarung
import com.example.gofoodpandaan.IkiOjek.TrackingOrderOjekActivity
import com.example.gofoodpandaan.Model.Pesanan
import com.example.gofoodpandaan.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity


class NotifikasiFragment : Fragment(),AnkoLogger {
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
        val root = inflater.inflate(R.layout.fragment_notifikasi, container, false)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        relativeLayout = root.find(R.id.adariwayat)
        constraint = root.find(R.id.belum_ada_pesanan)

        constraint.visibility = View.VISIBLE
        relativeLayout.visibility = View.INVISIBLE

        //Pesanan//
        recyclerView = root.find(R.id.rv_riwayat)
        val LayoutManager = LinearLayoutManager(context!!.applicationContext)
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = LayoutManager
        refinfo =
            FirebaseDatabase.getInstance().reference.child("Pandaan").child("HistoryCostumer")
                .child(userID.toString())
        //=======================

        //reclerview komentar
        val option =
            FirebaseRecyclerOptions.Builder<Pesanan>().setQuery(refinfo, Pesanan::class.java)
                .build()
        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Pesanan, PesananFragment.MyViewHolder>(option) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): PesananFragment.MyViewHolder {
                    val itemView = LayoutInflater.from(context?.applicationContext)
                        .inflate(R.layout.daftarriwayat, parent, false)
                    return PesananFragment.MyViewHolder(itemView)
                }

                override fun onBindViewHolder(
                    holder: PesananFragment.MyViewHolder,
                    position: Int,
                    model: Pesanan
                ) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }


                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                constraint.visibility = View.INVISIBLE
                                relativeLayout.visibility = View.VISIBLE
                                holder.mkodeorder.text =
                                    "Kode Order : ${model.kodeorder.toString()}"
                                holder.mstatus.text = "IKI-${model.status.toString()}"
                                info { "alfan ${model.alamat_warung.toString()}" }
                                holder.itemView.setOnClickListener {
                                    if (model.status.equals("WARUNG")) {
                                        startActivity<DetailRiwayatWarung>(
                                            "kode" to model.kodeorder.toString(),
                                            "namawarung" to model.namaToko.toString(),
                                            "status" to model.status.toString(),
                                            "alamatwarung" to model.alamat_warung.toString(),
                                            "alamatcostumer" to model.alamat_costumer.toString(),
                                            "uiddriver" to model.uiddriver.toString(),
                                            "harga" to model.harga.toString()
                                        )
                                    } else if (model.status.equals("OJEK")) {
                                        startActivity<DetailRiwayatOjekActivity>(
                                            "kode" to model.kodeorder.toString(),
                                            "namawarung" to model.penumpang.toString(),
                                            "status" to model.status.toString(),
                                            "alamattujuan" to model.alamat_tujuan.toString(),
                                            "alamatcostumer" to model.alamat_costumer.toString(),
                                            "uiddriver" to model.uiddriver.toString(),
                                            "harga" to model.harga.toString())
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
        var mstatus: TextView = itemView.findViewById(R.id.kodeorder)

    }


}