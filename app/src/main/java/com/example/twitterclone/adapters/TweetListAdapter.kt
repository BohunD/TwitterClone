package com.example.twitterclone.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ItemTweetBinding
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.util.Tweet
import com.example.twitterclone.util.getDate
import com.example.twitterclone.util.loadUrl

class TweetListAdapter(
    val userId: String,
    val tweets: ArrayList<Tweet>
) :RecyclerView.Adapter<TweetListAdapter.TweetViewHolder>() {

    private var listener: TweetListener?=null

    class TweetViewHolder(private val binding: ItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root){


        fun bind(userId: String, tweet: Tweet, listener: TweetListener?) = with(binding){
            tweetUsername.text = tweet.username
            tweetText.text = tweet.text
            tweetDate.text= getDate( tweet.timestamp)
            if(tweet.imageUrl.isNullOrEmpty()){
                tweetImage.visibility = View.GONE
            }
            else{
                Glide.with(this.root).load(tweet.imageUrl).into(tweetImage)
                tweetImage.visibility = View.VISIBLE
            }
            Log.d("ImageUrl", tweet.imageUrl.toString())
            tweetLikesCount.text = tweet.likes?.size.toString()
            tweetRetweetsCount.text = tweet.userIds?.size?.minus(1).toString()

            tweetLayout.setOnClickListener { listener?.onLayoutClick(tweet) }
            tweetLike.setOnClickListener { listener?.onLike(tweet) }
            tweetRetweet.setOnClickListener { listener?.onRetweet(tweet) }

            if(tweet.likes?.contains(userId)==true)
                tweetLike.setImageDrawable(ContextCompat.getDrawable(tweetLike.context,
                    R.drawable.like))
            else
                tweetLike.setImageDrawable(ContextCompat.getDrawable(tweetLike.context,
                    R.drawable.like_inactive))


            if(tweet.userIds?.get(0).equals(userId)){
                tweetRetweet.setImageDrawable(ContextCompat.getDrawable(tweetRetweet.context,
                    R.drawable.original))
                tweetRetweet.isClickable = false
            }
            else if(tweet.userIds?.contains(userId) == true){
                tweetRetweet.setImageDrawable(ContextCompat.getDrawable(tweetRetweet.context,
                    R.drawable.retweet))
            }
            else
                tweetRetweet.setImageDrawable(ContextCompat.getDrawable(tweetRetweet.context,
                    R.drawable.retweet_inactive))

        }
    }


    fun setListener(listener: TweetListener?){
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTweets(newTweets: List<Tweet>){
        tweets.clear()
        tweets.addAll(newTweets)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val binding = ItemTweetBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return TweetViewHolder(binding)
    }

    override fun getItemCount(): Int = tweets.size

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(userId, tweets[position], listener)
    }


}