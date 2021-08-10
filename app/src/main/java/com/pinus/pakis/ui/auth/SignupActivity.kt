package com.pinus.pakis.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pinus.pakis.databinding.ActivitySignupBinding
import kotlinx.android.parcel.Parcelize


class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var ref: DatabaseReference
    var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mFirebaseAuth = FirebaseAuth.getInstance()

        supportActionBar?.hide()

        ref = FirebaseDatabase.getInstance().getReference("orangtua")

        binding.btnDaftar.setOnClickListener {
            isLoading(true)
            signUp()
        }

        binding.tvMasuk.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUp() {
        var isEmptyFields = false

        with(binding) {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    isEmptyFields = true
                    etEmail.error = "Field ini tidak boleh kosong"
                }
                password.isEmpty() -> {
                    isEmptyFields = true
                    etPassword.error = "Field ini tidak boleh kosong"
                }
            }

            if (!isEmptyFields) {
                mFirebaseAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isLoading(false)
                            Toast.makeText(
                                applicationContext,
                                "Berhasil Membuat Akun",
                                Toast.LENGTH_SHORT
                            ).show()
                            uploadDataToDatabase()
                            finish()
                            startActivity(Intent(this@SignupActivity,SigninActivity::class.java))
                        } else {
                            isLoading(false)
                            Toast.makeText(
                                applicationContext,
                                "Gagal Membuat Akun",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
        }
    }

    private fun uploadDataToDatabase(){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val email = binding.etEmail.text.toString().trim()
        val user= User(uid,email)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("/users/$uid")

        myRef.setValue(user)
    }

    private fun isLoading(state: Boolean) {
        if (state) {
            binding.btnDaftar.visibility = View.INVISIBLE
            binding.pbDaftar.visibility = View.VISIBLE
        } else {
            binding.btnDaftar.visibility = View.VISIBLE
            binding.pbDaftar.visibility = View.INVISIBLE
        }
    }
}

@Parcelize
class User(val uid: String, val email:String): Parcelable{
    constructor() : this("","")
}