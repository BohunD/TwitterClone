package com.example.twitterclone.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityProfileBinding
import com.example.twitterclone.util.DATABASE_URL
import com.example.twitterclone.util.DATA_IMAGES
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.DATA_USER_EMAIL
import com.example.twitterclone.util.DATA_USER_IMAGE_URL
import com.example.twitterclone.util.DATA_USER_USERNAME
import com.example.twitterclone.util.REQUEST_CODE_PHOTO
import com.example.twitterclone.util.User
import com.example.twitterclone.util.loadUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl: String? = null
    private val firebaseStorage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (userId == null)
            finish()
        initViews()
        binding.btnApply.setOnClickListener {
            onApply()
        }
        binding.btnSignout.setOnClickListener {
            signOut()
        }
        binding.ivPhoto.setOnClickListener {
            launchPhotoLoading()
        }
    }

    private fun launchPhotoLoading() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO){
            storeImage(data?.data)
        }
    }

    private fun storeImage(imageUri: Uri?) {
        imageUri?.let {
            Toast.makeText(this@ProfileActivity, "Uploading...", Toast.LENGTH_SHORT).show()
            binding.profileProgressLayout.visibility = View.VISIBLE
            val filepath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filepath.putFile(imageUri)
                .addOnSuccessListener {
                filepath.downloadUrl.addOnSuccessListener {
                    val url = it.toString()
                    firebaseDB.collection(DATA_USERS)
                        .document(userId)
                        .update(DATA_USER_IMAGE_URL, url)
                        .addOnSuccessListener {
                            imageUrl = url
                            binding.ivPhoto.loadUrl(imageUrl, R.drawable.logo)
                        }
                }
                    .addOnFailureListener {
                        onUploadFailure()
                    }
                    binding.profileProgressLayout.visibility = View.GONE

                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }

    private fun onUploadFailure() {
        Toast.makeText(
            this@ProfileActivity,
            "Upload failed. Try again later", Toast.LENGTH_SHORT
        ).show()
        binding.profileProgressLayout.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() = with(binding) {

        profileProgressLayout.setOnTouchListener { _, _ -> true }
        profileProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                etEmail.setText(user?.email, TextView.BufferType.EDITABLE)
                etUsername.setText(user?.username, TextView.BufferType.EDITABLE)
                imageUrl = user?.imageUrl
                imageUrl?.let {
                    ivPhoto.loadUrl(user?.imageUrl, R.drawable.logo)
                }
                profileProgressLayout.visibility = View.GONE

            }
            .addOnFailureListener {
                it.printStackTrace()
                Log.d("MyLog", it.message.toString())
                finish()
            }
    }

    private fun onApply() = with(binding) {
        profileProgressLayout.visibility = View.VISIBLE
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = username
        map[DATA_USER_EMAIL] = email
        firebaseDB.collection(DATA_USERS).document(userId!!).update(map)
            .addOnSuccessListener {
                Toast.makeText(
                    this@ProfileActivity,
                    "Update successful", Toast.LENGTH_LONG
                ).show()
                profileProgressLayout.visibility = View.GONE
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(
                    this@ProfileActivity,
                    "Update failed", Toast.LENGTH_LONG
                ).show()
                profileProgressLayout.visibility = View.GONE
            }


    }

    private fun signOut() {
        firebaseAuth.signOut()
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}