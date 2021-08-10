package com.pinus.pakis.ui.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pinus.pakis.R
import com.pinus.pakis.ui.auth.SigninActivity
import com.pinus.pakis.ui.auth.User
import com.pinus.pakis.ui.chatting.NewMessageActivity.Companion.USER_KEY
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageActivity : AppCompatActivity() {
    companion object{
        var currentuser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)

        recyclerview_latestmessage.adapter = adapter
        recyclerview_latestmessage.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatlogActivity::class.java)

            val row = item as latestmessagerow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chattouser)
            startActivity(intent)
        }

        listenforlatestmessage()

        fetchcurrentuser()

        verifyUserLoggedIn()
    }
    class latestmessagerow(val chatmessage:ChatlogActivity.chatmessage): Item<GroupieViewHolder>(){
        var chattouser: User? = null
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.latest_message.text = chatmessage.text
            val chattoid: String
            if(chatmessage.fromid == FirebaseAuth.getInstance().uid){
                chattoid = chatmessage.toid!!
            }else{
                chattoid = chatmessage.fromid
            }

            val reference = FirebaseDatabase.getInstance().getReference("/users/$chattoid")
            reference.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chattouser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.email_latest_message.text = chattouser?.email
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    val latestmessagemap = HashMap<String, ChatlogActivity.chatmessage>()

    private fun refreshrecyclerviewmessage(){
        adapter.clear()
        latestmessagemap.values.forEach{
            adapter.add(latestmessagerow(it))
        }
    }

    private fun listenforlatestmessage(){
        val fromid = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/latestmessage/$fromid")
        reference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage = snapshot.getValue(ChatlogActivity.chatmessage::class.java) ?: return
                latestmessagemap[snapshot.key!!] = chatmessage
                refreshrecyclerviewmessage()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage = snapshot.getValue(ChatlogActivity.chatmessage::class.java) ?: return
                latestmessagemap[snapshot.key!!] = chatmessage
                refreshrecyclerviewmessage()
            }
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
        })
    }

    private fun fetchcurrentuser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/user/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentuser = snapshot.getValue(User::class.java)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }
}