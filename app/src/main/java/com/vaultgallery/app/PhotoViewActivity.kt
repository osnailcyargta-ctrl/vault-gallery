package com.vaultgallery.app

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        val uriStr = intent.getStringExtra("photo_uri") ?: return finish()
        val uri = Uri.parse(uriStr)
        val imageView = findViewById<ImageView>(R.id.full_image)

        Glide.with(this).load(uri).into(imageView)

        imageView.setOnClickListener { finish() }
    }
}
