package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemMapteBinding

class MapteAdapter (
    private val context: Context,
    private val imageList: List<Pair<String, String>>,
    private val onItemClick: (String) -> Unit

) : RecyclerView.Adapter<MapteAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemMapteBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val (imageFilename, name) = imageList[position]
        val assetManager = context.assets
        val inputStream = assetManager.open(imageFilename)
        val drawable = Drawable.createFromStream(inputStream, null)
        holder.binding.maptePreview.setImageDrawable(drawable)
        holder.binding.mapteName.text = name
        holder.binding.root.setOnClickListener {
            onItemClick(imageFilename) // Pass the image filename to the click handler
        }
    }

    override fun getItemCount(): Int = imageList.size

    class ImageViewHolder(val binding: ItemMapteBinding) : RecyclerView.ViewHolder(binding.root)
}