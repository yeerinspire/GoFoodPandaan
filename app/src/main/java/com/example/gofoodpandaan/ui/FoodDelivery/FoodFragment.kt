package com.example.gofoodpandaan.ui.FoodDelivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.glide.slider.library.SliderLayout
import com.glide.slider.library.animations.DescriptionAnimation
import com.glide.slider.library.slidertypes.TextSliderView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class FoodFragment : Fragment(),AnkoLogger {
    lateinit var mSlider: SliderLayout
    lateinit var root: View
    lateinit var refinfo: DatabaseReference
    var nama:String? = null
    var foto:String? = null
    var email:String? = null
    lateinit var auth: FirebaseAuth
    var userID: String? = null
    var image_list: HashMap<String, String>? = null
    lateinit var reference: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    companion object {
        lateinit var textSliderView: TextSliderView

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_food, container, false)
        textSliderView = TextSliderView(context!!.applicationContext)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid
        ambildata()
        setupslider()
        foodpopular()
        return root
    }


    private fun ambildata() {
        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Pandaan")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val ambildata = p0.child("Costumers").child(userID.toString()).getValue(ModelUsers::class.java)
                nama = ambildata!!.name.toString()
                foto = ambildata.foto.toString()
            }

        })

    }

    private fun foodpopular() {
        recyclerView = root.find(R.id.recyclerinfo)
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
                    val itemView = LayoutInflater.from(context?.applicationContext)
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
                            holder.mtitle.setText(model.nama)
                            Picasso.get().load(model.gambar).fit().centerCrop().into(holder.mimage)
                            holder.mharga.setText(model.harga)
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


        recyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.name)
        var mimage: ImageView = itemView.findViewById(R.id.gambar_makanan)
        var mharga: TextView = itemView.findViewById(R.id.harga)
    }

    private fun setupslider() {
        mSlider = root.findViewById(R.id.slider)
        image_list = HashMap()
        reference = FirebaseDatabase.getInstance().reference.child("Pandaan").child("info")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (data in p0.children) {
                    var banner = data.getValue(ModelUsers::class.java)
                    image_list!![banner!!.nama.toString() + "_" + banner.id.toString()] =
                        banner.gambar.toString()
                }
                val requestOptions = RequestOptions()
                requestOptions.centerCrop()
                for (key in image_list!!.keys) {
                    var keysplit = key.split("_")
                    val nama = keysplit[0]
                    val id = keysplit[1]
                    textSliderView
                        .description(nama)
                        .image(image_list!!.get(key))
                        .setRequestOption(requestOptions)
                        .setProgressBarVisible(true)
/*
                        .setOnSliderClickListener(object : BaseSliderView.OnSliderClickListener {
                            override fun onSliderClick(slider: BaseSliderView?) {
                                val intent = Intent(context!!.applicationContext,information::class.java)
                                intent.putExtras(textSliderView.bundle)
                                startActivity(intent)
                            }

                        })
*/

                    //add extra bundle
                    textSliderView.bundle(Bundle())
                    textSliderView.bundle.putString("id", id)
                    mSlider.addSlider(textSliderView)


                    //remove banner
                    reference.removeEventListener(this)


                }
            }

        })

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground)
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
        mSlider.setCustomAnimation(DescriptionAnimation())
        mSlider.setDuration(4000)


    }
}