package com.example.gofoodpandaan.IkiWarung.FoodDelivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.gofoodpandaan.Model.ChatMessage
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.Utlis.DateUtils.getFormattedTimeChatLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class chatActivity : AppCompatActivity(),AnkoLogger {
    companion object {
        val TAG = chatActivity::class.java.simpleName
    }

    val adapter = GroupAdapter<ViewHolder>()
    var uiddriver : String? = null
    var namadriver : String? = null
    var platdriver : String? = null
    lateinit var auth: FirebaseAuth
    var userID : String? = null
    // Bundle Data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val bundle: Bundle? = intent.extras
        namadriver = bundle!!.getString("nama_driver")
        uiddriver = bundle.getString("uid_driver")
        platdriver = bundle.getString("platdriver")
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        swiperefresh.setColorSchemeColors(ContextCompat.getColor(this,R.color.colorAccent))
        recyclerview_chat_log.adapter = adapter

        txt_uidlawan.text = namadriver.toString()
        txt_platnomor.text = platdriver.toString()

        ambildata()
        send_button_chat_log.setOnClickListener {
            performSendMessage()
        }
        val hapusdata = FirebaseDatabase.getInstance().getReference("chat").child("statusnotif").child(userID.toString()).removeValue()


    }

    private fun ambildata() {
        swiperefresh.isEnabled = true
        swiperefresh.isRefreshing = true

        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = uiddriver
        val ref = FirebaseDatabase.getInstance().getReference("/chat/user-messages/$fromId/$toId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    swiperefresh.isRefreshing = false
                    swiperefresh.isEnabled = false
                }
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    if (it.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(it.text, it.timestamp))

                    } else {
                        adapter.add(ChatFromItem(it.text, it.timestamp))

                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                swiperefresh.isRefreshing = false
                swiperefresh.isEnabled = false
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

        })
    }
    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = uiddriver.toString()

        val reference = FirebaseDatabase.getInstance().getReference("/chat/user-messages/$userID/$uiddriver").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/chat/user-messages/$uiddriver/$userID").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                edittext_chat_log.text.clear()
                recyclerview_chat_log.smoothScrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)


        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/chat/statusnotif/$uiddriver")
        latestMessageRef.child("status").setValue("adanotif")

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/chat/latest-messages/$uiddriver/$userID")
        latestMessageToRef.setValue(chatMessage)
    }


}
class ChatFromItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {


        viewHolder.itemView.txt_chatdriver.text = text
        viewHolder.itemView.txt_timedriver.text = getFormattedTimeChatLog(timestamp)


        /*   if (!user.profileImageUrl!!.isEmpty()) {

               val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)


               Glide.with(targetImageView.context)
                   .load(user.profileImageUrl)
                   .thumbnail(0.1f)
                   .apply(requestOptions)
                   .into(targetImageView)

           }*/
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}


class ChatToItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txt_chatuser.text = text
        viewHolder.itemView.txt_timeuser.text = getFormattedTimeChatLog(timestamp)



    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}

