package com.pinus.pakis.ui.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pinus.pakis.R
import com.pinus.pakis.databinding.ActivityNewMessageBinding
import com.pinus.pakis.databinding.UserShowNewMessageBinding
import com.pinus.pakis.ui.auth.User
import com.quickbirdstudios.surveykit.backend.helpers.logTag
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_show_new_message.view.*
import kotlin.math.log

class NewMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach{
                    it.toString()
                    val user = it.getValue(User::class.java)
                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context,ChatlogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user.email)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()
                }

                binding.recyclerviewNewmessage.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_show_new_message
    }

    private lateinit var binding: UserShowNewMessageBinding
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.emailview_newmessage.text = user.email
    }

}