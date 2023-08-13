package com.example.twitterclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.twitterclone.R
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.databinding.FragmentHomeBinding
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.DATA_TWEETS
import com.example.twitterclone.util.DATA_TWEET_HASHTAGS
import com.example.twitterclone.util.DATA_TWEET_USER_IDS
import com.example.twitterclone.util.Tweet


class HomeFragment : TwitterFragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener = TwitterListenerImpl(binding.rvTweetList, currentUser, callback)

        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.rvTweetList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            updateList()
        }
    }

    override fun updateList() {
        binding.rvTweetList.visibility = View.GONE
        currentUser?.let {
            val tweets = arrayListOf<Tweet>()
            for (hashtag in it.followHashTags!!) {
                firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_HASHTAGS, hashtag)
                    .get()
                    .addOnSuccessListener { list ->
                        for (document in list) {
                            val tweet = document.toObject(Tweet::class.java)
                            tweet.let {
                                tweets.add(it)
                            }
                            updateAdapter(tweets)
                            binding.rvTweetList.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener { e ->
                        e.printStackTrace()
                        binding.rvTweetList.visibility = View.VISIBLE
                    }
            }
            for (followedUser in it.followUsers!!) {
                firebaseDB.collection(DATA_TWEETS)
                    .whereArrayContains(DATA_TWEET_USER_IDS, followedUser).get()
                    .addOnSuccessListener { list ->
                        for (document in list) {
                            val tweet = document.toObject(Tweet::class.java)
                            tweet.let {
                                tweets.add(it)
                            }
                            updateAdapter(tweets)
                            binding.rvTweetList.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener { e ->
                        e.printStackTrace()
                        binding.rvTweetList.visibility = View.VISIBLE
                    }
            }

        }
    }

    private fun updateAdapter(tweets: List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
        tweetsAdapter?.updateTweets(removeDuplicates(sortedTweets))
    }

    private fun removeDuplicates(tweets: List<Tweet>) = tweets.distinctBy { it.tweetId }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle()
            }
    }
}