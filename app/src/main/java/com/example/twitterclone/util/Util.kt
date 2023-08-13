package com.example.twitterclone.util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.twitterclone.R
import java.text.DateFormat
import java.util.Date


fun ImageView.loadUrl(url: String?, errorDrawable: Int = R.drawable.empty){
    val options = RequestOptions()
        .placeholder(progressDrawable(context))
        .error(errorDrawable)
    Glide.with(context.applicationContext).load(url).apply(options).into(this)
}

fun progressDrawable(context: Context): CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }
}

fun getDate(s: Long?): String{
    s?.let {
        val dt = DateFormat.getDateInstance()
        return dt.format(Date(it))
    }
    return "Unknown"
}