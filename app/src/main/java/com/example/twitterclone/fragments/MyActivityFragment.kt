package com.example.twitterclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.twitterclone.R
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.databinding.FragmentHomeBinding
import com.example.twitterclone.databinding.FragmentMyActivityBinding
import com.example.twitterclone.util.DATA_TWEETS
import com.example.twitterclone.util.DATA_TWEET_USER_IDS
import com.example.twitterclone.util.Tweet

private lateinit var binding: FragmentMyActivityBinding

class MyActivityFragment : TwitterFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyActivityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.rvTweetList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            updateList()
        }
    }

    override fun updateList() {
        binding.rvTweetList?.visibility = View.GONE
        val tweets = arrayListOf<Tweet>()
        firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_USER_IDS, userId!!).get()
            .addOnSuccessListener { list ->
                for (document in list.documents) {
                    val tweet = document.toObject(Tweet::class.java)
                    tweet?.let { tweets.add(tweet) }

                }
                val sortedList = tweets.sortedWith(compareByDescending { it.timestamp })
                tweetsAdapter?.updateTweets(sortedList)
                binding.rvTweetList.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                binding.rvTweetList.visibility = View.VISIBLE
            }
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            MyActivityFragment()
    }
}