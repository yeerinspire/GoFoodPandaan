package com.example.gofoodpandaan.IkiWarung.FoodDelivery.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gofoodpandaan.Model.ModelUsers
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.DetailFoodActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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



        return  root
    }



}