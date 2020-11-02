package com.singpaulee.example_sqlite_kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gofoodpandaan.R
import kotlinx.android.synthetic.main.cart_sqlite.view.*
import kotlinx.android.synthetic.main.item_student.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick

class StudentAdapter(val context: Context, val list: List<StudentContract>)
    : RecyclerView.Adapter<StudentAdapter.ViewHolder>(){

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
        fun bind(studentContract: StudentContract) {
            itemView.namamakanan.text = studentContract.name
            itemView.txt_hargamakanan.text = studentContract.age.toString()

            itemView.onClick {
            }
        }

    }

}