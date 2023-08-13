package com.example.twitterclone.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityTweetBinding
import com.example.twitterclone.util.DATA_IMAGES
import com.example.twitterclone.util.DATA_TWEETS
import com.example.twitterclone.util.DATA_TWEET_IMAGES
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.REQUEST_CODE_PHOTO
import com.example.twitterclone.util.Tweet
import com.example.twitterclone.util.loadUrl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class TweetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTweetBinding
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private var imageUrl: String? = null
    private var userId: String? = null
    private var userName: String?= null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        binding.tweetProgressLayout.setOnTouchListener { view, motionEvent ->
            true
        }
        binding.fabSend.setOnClickListener {
            postTweet()
        }
        binding.fabPhoto.setOnClickListener {
            addImage()
        }

    }

    private fun getExtras(){
        if(intent.hasExtra(PARAM_USER_ID)&& intent.hasExtra(PARAM_USER_NAME)){
            userId = intent.getStringExtra(PARAM_USER_ID)
            userName = intent.getStringExtra(PARAM_USER_NAME)
        } else{
            Toast.makeText(this, "Error creating tweet", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun addImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK){
            storeImage(data?.data)
        }
    }

    private fun storeImage(imageUri: Uri?){
        imageUri?.let {
            Toast.makeText(this@TweetActivity, "Uploading...", Toast.LENGTH_SHORT).show()
            binding.tweetProgressLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_TWEET_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            imageUrl = url
                            binding.ivTweetImage.loadUrl(imageUrl, R.drawable.logo)
                            binding.tweetProgressLayout.visibility = View.GONE
                        }

                        .addOnFailureListener {
                            it.printStackTrace()
                            onUploadFailure()
                        }
                }.addOnFailureListener {
                    it.printStackTrace()
                    onUploadFailure()
                }
        }
    }

    private fun onUploadFailure() {
        Toast.makeText(
            this@TweetActivity,
            "Upload failed. Try again later", Toast.LENGTH_SHORT
        ).show()
        binding.tweetProgressLayout.visibility = View.GONE
    }

    private fun postTweet(){
        binding.tweetProgressLayout.visibility = View.VISIBLE
        val text = binding.etTweetText.text.toString()
        val hashtags = getHashTagsFromText(text)
        val tweetId = firebaseDB.collection(DATA_TWEETS).document()
        val tweet = Tweet(tweetId.id, arrayListOf(userId!!), userName, text,
            imageUrl, System.currentTimeMillis(),hashtags, arrayListOf() )
        tweetId.set(tweet).addOnCompleteListener { finish() }
            .addOnFailureListener {
                it.printStackTrace()
                binding.tweetProgressLayout.visibility = View.GONE
                Toast.makeText(this@TweetActivity, "Failed to post", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getHashTagsFromText(src: String): ArrayList<String>{
        val hashtags = arrayListOf<String>()
        var text = src
        while(text.contains("#")){
            var hashtag = ""
            val hash =  text.indexOf("#")
            text = text.substring(hash + 1)
            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if(firstSpace == -1 && firstHash == -1){
                hashtag = text.substring(0)
            } else if(firstSpace!= -1 && firstSpace< firstHash){
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace+1)
            } else{
                hashtag = text.substring(0, firstHash)
                text = text.substring(firstHash+1)
            }

            if(!hashtag.isNullOrEmpty()){
                hashtags.add(hashtag)
            }
        }
        return hashtags
    }

    companion object{
        val PARAM_USER_ID = "UserId"
        val PARAM_USER_NAME = "UserName"
        fun newIntent(context: Context, userId: String?, userName: String?): Intent {
            val intent = Intent(context, TweetActivity::class.java)
            intent.putExtra(PARAM_USER_ID, userId)
            intent.putExtra(PARAM_USER_NAME, userName)
            return intent
        }
    }
}