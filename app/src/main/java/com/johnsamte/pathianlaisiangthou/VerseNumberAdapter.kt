package com.johnsamte.pathianlaisiangthou

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemVerseNumberBinding

class VerseNumberAdapter (
        private val verseNumber: List<Int>,
        private val onVerseClick: (Int) -> Unit,
        //private val typeface: Typeface?
    ) : RecyclerView.Adapter<VerseNumberAdapter.VerseViewHolder>() {

        inner class VerseViewHolder(private val binding: ItemVerseNumberBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(verseNumber: Int) {
                binding.verseNumber.text = verseNumber.toString()
               // binding.verseNumber.typeface = typeface // Apply custom typeface
                itemView.setOnClickListener {
                    onVerseClick(verseNumber)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
            val binding = ItemVerseNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VerseViewHolder(binding)
        }

        override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
            val verseNumber = verseNumber[position]
            holder.bind(verseNumber)
        }

        override fun getItemCount(): Int = verseNumber.size
    }
