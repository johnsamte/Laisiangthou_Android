package com.johnsamte.pathianlaisiangthou

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemImageShareBinding

class ImageShareAdapter(
    private val images: List<Int>, // Replace Int with the actual type (e.g., URI)
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageShareAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageShareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageResId = images[position]
        holder.bind(imageResId)
        holder.binding.root.setOnClickListener { onImageClick(imageResId) }
    }

    override fun getItemCount(): Int = images.size

    class ImageViewHolder(val binding: ItemImageShareBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int) {
            binding.imageView.setImageResource(imageResId)
        }
    }
}
