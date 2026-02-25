package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemChapterBinding

class ChapterAdapter(
    //context: Context,
    private val chapters: List<Int>,
    private val onChapterClick: (Int) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(private val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chapterNumber: Int) {
            binding.chapterNumber.text = chapterNumber.toString()
            binding.root.setOnClickListener {
                onChapterClick(chapterNumber)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(chapters[position])
    }

    override fun getItemCount(): Int = chapters.size
}