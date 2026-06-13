package com.vaultgallery.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(
    private val photos: List<PhotoItem>,
    private val activity: MainActivity
) : RecyclerView.Adapter<PhotoAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.photo_image)
        val lockIcon: ImageView = view.findViewById(R.id.lock_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val photo = photos[position]

        if (photo.isHidden) {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.image.alpha = 0.3f
            holder.lockIcon.visibility = View.VISIBLE
        } else {
            holder.image.alpha = 1f
            holder.lockIcon.visibility = View.GONE
            Glide.with(holder.image)
                .load(photo.uri)
                .centerCrop()
                .thumbnail(0.1f)
                .into(holder.image)
        }

        holder.itemView.setOnClickListener {
            activity.onPhotoClick(photo)
        }
    }

    override fun getItemCount() = photos.size
}
