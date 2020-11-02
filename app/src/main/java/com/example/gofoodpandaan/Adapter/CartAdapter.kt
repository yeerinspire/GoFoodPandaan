package com.example.gofoodpandaan.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gofoodpandaan.Model.CartSQlite
import com.example.gofoodpandaan.R
import kotlinx.android.synthetic.main.activity_cart.view.*
import kotlinx.android.synthetic.main.cart_sqlite.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick

class CartAdapter(val context: Context, val list: List<CartSQlite>)
    : RecyclerView.Adapter<CartAdapter.ViewHolder>(){

    lateinit var itemview: View

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        itemview = LayoutInflater.from(context).inflate(R.layout.cart_sqlite, p0, false)
        return ViewHolder(itemview)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(list[p1])
    }

    class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        fun bind(cartSQlite: CartSQlite) {
            itemView.namamakanan.text = cartSQlite.name
            itemView.txt_hargamakanan.text = cartSQlite.age.toString()

            itemView.onClick {
//                itemView.context.startActivity(itemView.context.intentFor<DetailStudentActivity>("student" to studentContract))
            }
        }

    }

}