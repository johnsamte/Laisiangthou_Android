package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemBookBinding

class LaisiangthouBuAdapter(
    private val context: Context
) : ListAdapter<LaisiangthouBu, LaisiangthouBuAdapter.BookViewHolder>(DiffCallback()) {

    private var expandedBookPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class BookViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: LaisiangthouBu, position: Int) {

            binding.bookName.text = book.name

            val isExpanded = position == expandedBookPosition
            binding.chapterRecyclerView.visibility =
                if (isExpanded) View.VISIBLE else View.GONE

            binding.bookName.setOnClickListener {

                // Special case for "Mapte"
                if (book.name == "Mapte") {
                    val intent = Intent(context, MapteActivity::class.java)
                    context.startActivity(intent)
                    return@setOnClickListener
                }

                if (expandedBookPosition == position) {
                    // Collapse current
                    expandedBookPosition = null
                    notifyItemChanged(position)
                } else {
                    // Expand new & collapse previous
                    val previousExpandedPosition = expandedBookPosition
                    expandedBookPosition = position

                    previousExpandedPosition?.let {
                        notifyItemChanged(it)
                    }

                    notifyItemChanged(position)

                    // Scroll selected item to top
                    val recyclerView = binding.root.parent as? RecyclerView
                    val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                    layoutManager?.scrollToPositionWithOffset(position, 0)
                }
            }

            // Always bind chapter recycler when expanded
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

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val scalingFactor = 60
        return (dpWidth / scalingFactor).toInt()
    }
}
