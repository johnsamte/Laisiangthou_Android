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
    private var bookmarks: List<Bookmark>,
    private val deleteListener: (Bookmark) -> Unit,
    private val shareListener: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    private var groupedBookmarks: List<BookmarkGroup> = listOf()

    data class BookmarkGroup(
        val bookName: String,
        val bookId: Int,
        val chapterNumber: Int,
        val verseNumbers: List<Int>,
        val combinedText: String,
        val bookmarksInGroup: List<Bookmark>
    )

    fun updateBookmarks(newBookmarks: List<Bookmark>) {
        bookmarks = newBookmarks
        groupBookmarks()
        notifyDataSetChanged()
    }

    private fun groupBookmarks() {
        groupedBookmarks = bookmarks
            .groupBy { it.bookId to it.chapterNumber }
            .map { (_, list) ->
                val bookName = list.first().bookName
                val bookId = list.first().bookId
                val chapterNumber = list.first().chapterNumber
                val verseNumbers = list.map { it.verseNumber }.sorted()
                val combinedText = list.joinToString("\n") { it.bookmarkText }
                BookmarkGroup(bookName, bookId, chapterNumber, verseNumbers, combinedText, list)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun getItemCount(): Int = groupedBookmarks.size

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(groupedBookmarks[position], deleteListener, shareListener)
    }

    inner class BookmarkViewHolder(private val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: BookmarkGroup, deleteListener: (Bookmark) -> Unit, shareListener: (Bookmark) -> Unit) {
            val condensed = getCondensedVerseString(group.verseNumbers)
            binding.bookschaptBm.text = binding.root.context.getString(R.string.bookmark_header, group.bookName, group.chapterNumber, condensed)

            // Join all cleaned bookmark texts into a single line
            val cleanedText = group.bookmarksInGroup.joinToString(" ") { it.bookmarkText.trim() }
            binding.textViewBookmark.text = cleanedText

            // Timestamp
            val firstBookmark = group.bookmarksInGroup.firstOrNull()
            val formattedDate = firstBookmark?.createdAt?.let { formatStoredDate(it) } ?: "Unknown Date & Time"
            binding.textViewTimestamp.text = formattedDate

            binding.root.setBackgroundResource(R.drawable.notes_bg)

            binding.iconMenu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.menuInflater.inflate(R.menu.bookmark_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_view -> {
                            val intent = Intent(it.context, VerseTextActivity::class.java).apply {
                                putExtra("BOOK_NAME", group.bookName)
                                putExtra("CHAPTER_NUMBER", group.chapterNumber)
                                putExtra("BOOK_ID", group.bookId)
                                putExtra("VERSE_NUMBER", group.verseNumbers.firstOrNull() ?: 1)
                            }
                            it.context.startActivity(intent)
                            true
                        }
                        R.id.action_delete -> {
                            // Delete all bookmarks in this group
                            group.bookmarksInGroup.forEach { deleteListener(it) }
                            true
                        }
                        R.id.action_share -> {
                            shareListener(Bookmark(
                                id = 0,
                                bookId = group.bookId,
                                bookName = group.bookName,
                                chapterNumber = group.chapterNumber,
                                verseNumber = 0,
                                bookmarkText = group.combinedText,
                                createdAt = "",
                                updatedAt = ""
                            ))
                            true
                        }
                        R.id.action_shareImage -> {
                            val intent = Intent(it.context, ImageShareActivity::class.java).apply {
                                putExtra("EXTRA_FULL_VERSE_TEXT", "${group.bookName} ${group.chapterNumber}\n${group.combinedText}")
                            }
                            it.context.startActivity(intent)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }

            // Click item to open first verse
            binding.root.setOnClickListener {
                val intent = Intent(it.context, VerseTextActivity::class.java).apply {
                    putExtra("BOOK_NAME", group.bookName)
                    putExtra("CHAPTER_NUMBER", group.chapterNumber)
                    putExtra("BOOK_ID", group.bookId)
                    putExtra("VERSE_NUMBER", group.verseNumbers.firstOrNull() ?: 1)
                }
                it.context.startActivity(intent)
            }
        }

        private fun getCondensedVerseString(verseNumbers: List<Int>): String {
            if (verseNumbers.isEmpty()) return ""
            val sorted = verseNumbers.sorted()
            val condensed = mutableListOf<String>()
            var start = sorted.first()
            var prev = start
            for (i in 1 until sorted.size) {
                val current = sorted[i]
                if (current == prev + 1) prev = current
                else {
                    if (start == prev) condensed.add("$start") else condensed.add("$start-$prev")
                    start = current
                    prev = current
                }
            }
            if (start == prev) condensed.add("$start") else condensed.add("$start-$prev")
            return condensed.joinToString(",")
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

}