package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemBookBinding

class LaisiangthouBuAdapter(
    private val context: Context
) : ListAdapter<LaisiangthouBu, LaisiangthouBuAdapter.BookViewHolder>(DiffCallback()) {

    // Track expanded item by ID (NOT position)
    private var expandedBookId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: LaisiangthouBu) {

            binding.bookName.text = book.name

            // Expand state based on ID
            val isExpanded = book.id == expandedBookId
            binding.chapterRecyclerView.visibility =
                if (isExpanded) View.VISIBLE else View.GONE

            binding.bookName.setOnClickListener {

                // Special case for "Mapte"
                if (book.name == "Mapte") {
                    val intent = Intent(context, MapteActivity::class.java)
                    context.startActivity(intent)
                    return@setOnClickListener
                }

                val previousExpandedId = expandedBookId

                // Toggle expansion
                expandedBookId = if (expandedBookId == book.id) {
                    null
                } else {
                    book.id
                }

                // Refresh previously expanded item
                previousExpandedId?.let { id ->
                    val previousIndex = currentList.indexOfFirst { it.id == id }
                    if (previousIndex != -1) {
                        notifyItemChanged(previousIndex)
                    }
                }

                // Refresh current clicked item
                notifyItemChanged(bindingAdapterPosition)

                // Scroll to top when expanding
                if (expandedBookId == book.id) {
                    val recyclerView = binding.root.parent as? RecyclerView
                    val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                    layoutManager?.scrollToPositionWithOffset(bindingAdapterPosition, 0)
                }
            }

            // Bind chapters only if expanded
            if (isExpanded) {

                val chapterAdapter = ChapterAdapter(
                    book.chapters.map { it.chapterNumber }
                ) { chapterNumber ->

                    val chapter = book.chapters.first {
                        it.chapterNumber == chapterNumber
                    }

                    val intent = Intent(context, VerseNumberActivity::class.java).apply {
                        putExtra("BOOK_ID", book.id)
                        putExtra("CHAPTER_ID", chapter.id)
                        putExtra("CHAPTER_NUMBER", chapterNumber)
                    }

                    context.startActivity(intent)
                }

                binding.chapterRecyclerView.layoutManager =
                    GridLayoutManager(context, calculateNoOfColumns(context))

                binding.chapterRecyclerView.adapter = chapterAdapter
            }
        }
    }

    fun updateBooks(newBooks: List<LaisiangthouBu>) {
        submitList(newBooks)
    }

    fun clearExpansion() {
        expandedBookId?.let { id ->
            val index = currentList.indexOfFirst { it.id == id }
            expandedBookId = null
            if (index != -1) {
                notifyItemChanged(index)
            }
        }
    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val scalingFactor = 60
        return (dpWidth / scalingFactor).toInt()
    }

    class DiffCallback : DiffUtil.ItemCallback<LaisiangthouBu>() {

        override fun areItemsTheSame(
            oldItem: LaisiangthouBu,
            newItem: LaisiangthouBu
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LaisiangthouBu,
            newItem: LaisiangthouBu
        ): Boolean {
            return oldItem == newItem
        }
    }
}
