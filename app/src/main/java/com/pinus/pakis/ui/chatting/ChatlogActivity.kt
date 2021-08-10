package com.pinus.pakis.ui.chatting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.pinus.pakis.R
import com.pinus.pakis.ui.auth.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.chat_left_row.view.*
import kotlinx.android.synthetic.main.chat_right_row.view.*

class ChatlogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlog)

        recyclerview_chatlog.adapter = adapter

        val touser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = touser?.email

        listenformessage()

        sendbutton_chatlog.setOnClickListener {
            sendmessage()
        }
    }
    private fun listenformessage(){
        val fromid = FirebaseAuth.getInstance().uid
        val touser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toid = touser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromid/$toid")

        reference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage = snapshot.getValue(chatmessage::class.java)
                if (chatmessage != null){
                    if(chatmessage.fromid == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatLeftItem(chatmessage.text))
                    }else{
                        adapter.add(ChatRightItem(chatmessage.text))
                    }

                }
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    class chatmessage(
        val id: String,
        val text: String,
        val fromid: String, val toid: String?, val timestamp: Long){
        constructor() : this("","","","",-1)
    }

    private fun sendmessage(){
        val message = edittext_chatlog.text.toString()
        val fromid = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toid = user?.uid

        if(fromid == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromid/$toid").push()

        val toreference = FirebaseDatabase.getInstance().getReference("/user-messages/$toid/$fromid").push()

        val chatmessage = chatmessage(reference.key!!,message,fromid!!,toid, System.currentTimeMillis()/1000)
        reference.setValue(chatmessage)
            .addOnSuccessListener {
                edittext_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }
        toreference.setValue(chatmessage)

        val latestmessageref = FirebaseDatabase.getInstance().getReference("/latestmessage/$fromid/$toid")
        latestmessageref.setValue(chatmessage)
        val revlatestmessageref = FirebaseDatabase.getInstance().getReference("/latestmessage/$toid/$fromid")
        revlatestmessageref.setValue(chatmessage)
    }
}

class ChatLeftItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_left_chatlog.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_left_row
    }
}

class ChatRightItem(val text:String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_right_chatlog.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_right_row
    }
}