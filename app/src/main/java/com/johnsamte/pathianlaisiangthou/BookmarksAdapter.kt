package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemBookmarkBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookmarksAdapter(
    private var bookmark: List<Bookmark>,
    private val deleteListener: (Bookmark) -> Unit,
    private val shareListener: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    class BookmarkViewHolder(private val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            bookmark: Bookmark,
            deleteListener: (Bookmark) -> Unit,
            shareListener: (Bookmark) -> Unit
        ) {

            binding.bookschaptBm.text = binding.root.context.getString(
                R.string.bookmark_update,
                bookmark.bookName,
                bookmark.chapterNumber,
                bookmark.verseNumber
            )

            binding.textViewBookmark.text = bookmark.bookmarkText

            val formattedDate = formatStoredDate(bookmark.createdAt) // Use createdAt or updatedAt field from the note

            binding.textViewTimestamp.text = formattedDate

            // Set the drawable as the background
            binding.root.setBackgroundResource(R.drawable.notes_bg)

            // Show a popup menu when the menu icon is clicked
            binding.iconMenu.setOnClickListener {
                showPopupMenu(it, bookmark, deleteListener, shareListener)
            }
        }

        private fun formatStoredDate(storedDate: String): String {
            try {
                // Convert the stored date (string) into a Long and then to Date
                val timestamp = storedDate.toLong()
                val date = Date(timestamp)

                // Format the date
                val dateFormat = SimpleDateFormat("dd MMM yyyy | h:mm a", Locale.getDefault())
                return dateFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                // Return a fallback value in case of an error
                return "Unknown Date & Time"
            }
        }

        private fun showPopupMenu(
            view: View,
            bookmark: Bookmark,
            deleteListener: (Bookmark) -> Unit,
            shareListener: (Bookmark) -> Unit
        ) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.bookmark_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_view -> {
                        val intent = Intent(itemView.context, VerseTextActivity::class.java).apply {
                            // Ensure this is correct for all books
                            putExtra("BOOK_NAME", bookmark.bookName)
                            putExtra("CHAPTER_NUMBER", bookmark.chapterNumber)
                            putExtra("VERSE_NUMBER", bookmark.verseNumber) // Ensure this is the correct verse
                            putExtra("BOOK_ID", bookmark.bookId)

                        }
                        itemView.context.startActivity(intent)

                        true
                    }

                    R.id.action_delete -> {
                        deleteListener(bookmark) // Call the delete listener
                        true
                    }

                    R.id.action_share -> {
                        shareListener(bookmark) // Call the share listener
                        true
                    }
                    R.id.action_shareImage -> {
                        // Start ImageShareActivity with the full text as an extra
                        val intent = Intent(itemView.context, ImageShareActivity::class.java).apply {
                            putExtra(
                                "EXTRA_FULL_VERSE_TEXT",
                                "${bookmark.bookName} ${bookmark.chapterNumber}\n${bookmark.bookmarkText}")
                        }
                        itemView.context.startActivity(intent)

                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding =
            ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = bookmark[position]

        holder.bind(bookmark, deleteListener, shareListener)

        holder.itemView.setOnClickListener {
            // Navigate to the VersesActivity
            val intent = Intent(holder.itemView.context, VerseTextActivity::class.java).apply {
                putExtra("BOOK_NAME", bookmark.bookName)
                putExtra("CHAPTER_NUMBER", bookmark.chapterNumber)
                putExtra("VERSE_NUMBER", bookmark.verseNumber)
                putExtra("BOOK_ID", bookmark.bookId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = bookmark.size

    fun updateBookmarks(newBookmarks: List<Bookmark>) {
        bookmark = newBookmarks
        notifyDataSetChanged()
    }

}