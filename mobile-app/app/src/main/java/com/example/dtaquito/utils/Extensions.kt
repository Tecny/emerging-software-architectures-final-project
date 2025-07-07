package com.example.dtaquito.utils

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.dtaquito.R

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
fun loadImageFromUrl(imageUrl: String?, imageView: ImageView) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .error(R.drawable.default_image)
            .into(imageView)
    } else {
        imageView.setImageResource(R.drawable.default_image)
    }
}