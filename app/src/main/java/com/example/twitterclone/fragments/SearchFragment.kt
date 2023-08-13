package com.example.twitterclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.twitterclone.R
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.databinding.FragmentSearchBinding
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.DATA_TWEETS
import com.example.twitterclone.util.DATA_TWEET_HASHTAGS
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.DATA_USER_HASHTAGS
import com.example.twitterclone.util.Tweet
import com.example.twitterclone.util.User
import com.example.twitterclone.util.loadUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SearchFragment : TwitterFragment() {

    private lateinit var binding: FragmentSearchBinding

    private var currentHashtag = ""
    private var hashtagFollowed = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener = TwitterListenerImpl(binding.rvTweetList, currentUser, callback)
        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.rvTweetList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            updateList()
        }
        binding.btnFollowHashtag.setOnClickListener {
            binding.btnFollowHashtag.isClickable = false
            val followed = currentUser?.followHashTags
            if (hashtagFollowed) {
                followed?.remove(currentHashtag)

            } else {
                followed?.add(currentHashtag)
            }
            firebaseDB.collection(DATA_USERS).document(userId).update(DATA_USER_HASHTAGS, followed)
                .addOnSuccessListener {
                    binding.btnFollowHashtag.isClickable = true
                    callback?.onUserUpdated()
                }.addOnFailureListener {
                    it.printStackTrace()
                    binding.btnFollowHashtag.isClickable = true
                }

        }
    }

    fun newHashtag(term: String) {
        currentHashtag = term
        binding.btnFollowHashtag.visibility = View.VISIBLE
        updateList()
    }

    private fun updateFollowBtn() {
        hashtagFollowed = currentUser?.followHashTags?.contains(currentHashtag) == true
        context?.let {
            if (hashtagFollowed) {
                binding.btnFollowHashtag.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.follow
                    )
                )
            } else {
                binding.btnFollowHashtag.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.follow_inactive
                    )
                )
            }
        }
    }

    override fun updateList() {
        binding.rvTweetList.visibility = View.GONE
        firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_HASHTAGS, currentHashtag)
            .get()
            .addOnSuccessListener { list ->
                binding.rvTweetList.visibility = View.VISIBLE
                val tweets = arrayListOf<Tweet>()
                for (document in list.documents) {
                    val tweet = document.toObject(Tweet::class.java)
                    tweet?.let {
                        tweets.add(it)
                    }
                    val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
                    tweetsAdapter?.updateTweets(sortedTweets)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
        updateFollowBtn()

    }

    companion object {

        @JvmStatic
        fun newInstance() =
            SearchFragment()
    }
}