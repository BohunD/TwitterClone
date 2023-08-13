package com.example.twitterclone.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.listeners.HomeCallback
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.RuntimeException

abstract class TwitterFragment: Fragment() {
    protected var tweetsAdapter: TweetListAdapter?= null
    protected var currentUser: User?= null
    protected val firebaseDB = FirebaseFirestore.getInstance()
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid
    protected var listener: TwitterListenerImpl?=null
    protected var callback: HomeCallback?= null

    fun setUser(user: User){
        this.currentUser = user
        listener?.user = user
    }

    abstract fun updateList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is HomeCallback){
            callback = context
        }
        else throw RuntimeException("$context must implement callback")
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

}