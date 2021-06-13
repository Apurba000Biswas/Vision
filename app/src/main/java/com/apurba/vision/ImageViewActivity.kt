package com.apurba.vision

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class ImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val uri = intent.getStringExtra("uri")

        Picasso.get()
            .load(uri)
            .placeholder(R.drawable.smile)
            .into(findViewById<ImageView>(R.id.imageView))

    }
}