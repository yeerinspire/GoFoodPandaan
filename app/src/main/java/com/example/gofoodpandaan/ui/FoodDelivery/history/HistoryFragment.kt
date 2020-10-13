package com.example.gofoodpandaan.ui.FoodDelivery.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gofoodpandaan.Model.DriverWorking
import com.example.gofoodpandaan.Model.ModelOrder
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.ui.FoodDelivery.DetailFoodActivity
import com.example.gofoodpandaan.ui.FoodDelivery.FoodFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.startActivity

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    lateinit var root : View
    lateinit var refinfo: DatabaseReference
    lateinit var auth: FirebaseAuth
    var userID : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         root =  inflater.inflate(R.layout.fragment_history, container, false)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        foodpopular(root)
        return  root
    }

    private fun foodpopular(root : View) {
        recyclerView = root.find(R.id.rv_history)
        val LayoutManager = LinearLayoutManager(context!!.applicationContext)
        LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Pandaan").child("Resto")
        val option =
            FirebaseRecyclerOptions.Builder<ModelUsers>().setQuery(refinfo, ModelUsers::class.java)
                .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val itemView = LayoutInflater.from(context!!.applicationContext)
                        .inflate(R.layout.list_item, parent, false)
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
                            holder.mname.setText(model.nama)
                            holder.mharga.setText(model.harga)
                            holder.itemView.setOnClickListener {
                                startActivity<DetailFoodActivity>(
                                    "Firebase_gambarMakanan" to model.gambar,
                                    "Firebase_Makanan" to model.nama,
                                    "firebase_idMakanan" to model.id,
                                    "firebase_hargaMakanan" to model.harga,
                                    "firebase_penjual" to model.penjual)

                            }
                        }

                    })
                }
            }


        recyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mharga: TextView = itemView.findViewById(R.id.txt_itemhargahistory)
        var mname: TextView = itemView.findViewById(R.id.name)
    }



}