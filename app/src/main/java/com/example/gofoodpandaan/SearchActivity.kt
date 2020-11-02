package com.example.gofoodpandaan

import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.with
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.DetailFoodActivity
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.FoodFragment
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.Utlis.RoundedCornersTransformation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.startActivity

class SearchActivity : AppCompatActivity() {
    lateinit var mSearchText : MaterialSearchBar
    lateinit var mRecyclerView : RecyclerView
    var nama:String? = null

    lateinit var mDatabase : DatabaseReference

    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mSearchText =findViewById(R.id.searachfood)
        mRecyclerView = findViewById(R.id.rv_food)



        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this))


        mSearchText.addTextChangeListener(object  : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText = mSearchText.getText().toString().trim()

                loadFirebaseData(searchText)
            }
        } )



    }

    private fun loadFirebaseData(searchText : String) {

        if(searchText.isEmpty()){

            FirebaseRecyclerAdapter.notifyDataSetChanged()
            mRecyclerView.adapter = FirebaseRecyclerAdapter

        }else {


            val LayoutManager = LinearLayoutManager(this)
            LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            rv_food.layoutManager = LayoutManager
            mDatabase = FirebaseDatabase.getInstance().reference.child("Pandaan").child("Resto")

            val firebaseSearchQuery = mDatabase.orderByChild("penjual").startAt(searchText).endAt(searchText + "\uf8ff")

            val newOptions = FirebaseRecyclerOptions.Builder<ModelUsers>()
                .setQuery(firebaseSearchQuery, ModelUsers::class.java)
                .build()

            FirebaseRecyclerAdapter =
                object : FirebaseRecyclerAdapter<ModelUsers, MyViewHolder>(newOptions) {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): MyViewHolder {
                        val itemView = LayoutInflater.from(this@SearchActivity)
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
                        mDatabase.child(refid).addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                holder.mtitle.text = model.nama
                                holder.mharga.text = model.harga
                                holder.mketerangan.text = model.keterangan
                                Picasso.get().load(model.gambar).transform(transformation).fit()
                                    .into(holder.mimage)
                                holder.itemView.setOnClickListener {
                                    startActivity<DetailFoodActivity>(
                                        "Firebase_gambarMakanan" to model.gambar,
                                        "Firebase_Makanan" to model.nama,
                                        "firebase_idMakanan" to model.id,
                                        "firebase_hargaMakanan" to model.harga,
                                        "firebase_penjual" to model.penjual,
                                        "firebase_namaCostumer" to nama
                                    )

                                }
                            }

                        })
                    }
                }


            rv_food.adapter = FirebaseRecyclerAdapter
            FirebaseRecyclerAdapter.startListening()


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(intentFor<HomeActivity>().clearTask().newTask())
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.name)
        var mimage: ImageView = itemView.findViewById(R.id.gambar_makanan)
        var mharga: TextView = itemView.findViewById(R.id.hargamakanan)
        var mketerangan: TextView = itemView.findViewById(R.id.keterangan)
    }

}