package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val editListener: (Note) -> Unit,
    private val deleteListener: (Note) -> Unit,
    private val shareListener: (Note) -> Unit,

) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {

            // Book + Chapter + Verse
            binding.booksChapt.text = "${note.bookName} ${note.chapterNumber}:${note.referenceKey}"

            // Note text
           // binding.textViewNote.text = note.noteText

            val spannable = SpannableString(note.noteText)
            val db = LaisiangthouDatabaseManager.getDatabase(binding.root.context)

            // 1️⃣ Get all book names from database
            val bookNames = mutableListOf<String>()
            val cursor = db.rawQuery("SELECT name FROM Books", null)
            while (cursor.moveToNext()) {
                bookNames.add(cursor.getString(0))
            }
            cursor.close()

            // 2️⃣ Scan text word by word
            val words = note.noteText.split(" ")
            for (i in words.indices) {
                for (bookName in bookNames) {
                    val bookWords = bookName.split(" ")
                    if (i + bookWords.size < words.size + 1) {
                        val candidate = words.subList(i, i + bookWords.size).joinToString(" ")
                        if (candidate.equals(bookName, ignoreCase = true)) {
                            // Check next word for chapter:verse
                            if (i + bookWords.size < words.size) {
                                val chapVerse = words[i + bookWords.size]
                                val parts = chapVerse.split(":")
                                if (parts.size == 2) {
                                    val chapter = parts[0].toIntOrNull()
                                    val verse = parts[1].toIntOrNull()
                                    if (chapter != null && verse != null) {
                                        // ✅ Create clickable span
                                        val start = note.noteText.indexOf(candidate + " " + chapVerse)
                                        val end = start + (candidate + " " + chapVerse).length
                                        val chapterId = getChapterId(binding.root.context, bookName, chapter)
                                        if (chapterId != -1) {
                                            val clickableSpan = object : ClickableSpan() {
                                                override fun onClick(widget: View) {
                                                    val intent = Intent(widget.context, VerseTextActivity::class.java).apply {
                                                        putExtra("BOOK_NAME", bookName)
                                                        putExtra("CHAPTER_NUMBER", chapter)
                                                        putExtra("REFERENCE_KEY", verse.toString())
                                                        putExtra("CHAPTER_ID", chapterId)
                                                    }
                                                    widget.context.startActivity(intent)
                                                }

                                                override fun updateDrawState(ds: TextPaint) {
                                                    super.updateDrawState(ds)
                                                    ds.isUnderlineText = true
                                                    ds.color = if (isColorDark(note.noteColor)) Color.YELLOW else Color.BLUE
                                                }
                                            }
                                            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            binding.textViewNote.text = spannable
            binding.textViewNote.movementMethod = LinkMovementMethod.getInstance()
            binding.textViewNote.highlightColor = Color.TRANSPARENT

            // Timestamp (shows updatedAt if edited)
            val timestampToUse = if (note.updatedAt != note.createdAt) note.updatedAt else note.createdAt
            binding.textViewTimestamp.text = formatStoredDate(timestampToUse)

            // Background
            val drawable = binding.root.background as? GradientDrawable
            drawable?.setColor(note.noteColor) ?: binding.root.setBackgroundColor(note.noteColor)

            // Text color based on background brightness
            val textColor = if (isColorDark(note.noteColor)) Color.WHITE else Color.BLACK
            binding.booksChapt.setTextColor(textColor)
            binding.textViewNote.setTextColor(textColor)
            binding.textViewTimestamp.setTextColor(textColor)
            binding.iconMenu.setColorFilter(textColor)


            // Whole item click → open verse
            binding.root.setOnClickListener {
                val intent = Intent(it.context, VerseTextActivity::class.java).apply {
                    putExtra("BOOK_NAME", note.bookName)
                    putExtra("CHAPTER_NUMBER", note.chapterNumber)
                    putExtra("REFERENCE_KEY", note.referenceKey)
                    putExtra("CHAPTER_ID", note.chapterId)
                }
                it.context.startActivity(intent)
            }

            // Menu click
            binding.iconMenu.setOnClickListener {
                showPopupMenu(it, note)
            }
        }

        private fun getChapterId(context: Context, bookName: String, chapterNumber: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            var chapterId = -1

            // 1️⃣ Get book_id from book name
            var bookId = -1
            val bookCursor = db.rawQuery(
                "SELECT id FROM Books WHERE name = ?",
                arrayOf(bookName)
            )
            if (bookCursor.moveToFirst()) {
                bookId = bookCursor.getInt(0)
            }
            bookCursor.close()

            if (bookId != -1) {
                // 2️⃣ Get chapter id using book_id
                val cursor = db.rawQuery(
                    "SELECT id FROM Chapters WHERE book_id = ? AND chapter_number = ?",
                    arrayOf(bookId.toString(), chapterNumber.toString())
                )
                if (cursor.moveToFirst()) {
                    chapterId = cursor.getInt(0)
                }
                cursor.close()
            }

            return chapterId
        }

        // Helper function to determine if color is dark
        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }

        private fun showPopupMenu(view: View, note: Note) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.note_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.action_view -> {
                        val intent = Intent(view.context, VerseTextActivity::class.java).apply {
                            putExtra("BOOK_NAME", note.bookName)
                            putExtra("CHAPTER_NUMBER", note.chapterNumber)
                            putExtra("REFERENCE_KEY", note.referenceKey)
                            putExtra("CHAPTER_ID", note.chapterId)

                        }
                        view.context.startActivity(intent)
                        true
                    }

                    R.id.action_edit -> {
                        editListener(note)
                        true
                    }

                    R.id.action_delete -> {
                        deleteListener(note)
                        true
                    }

                    R.id.action_share -> {
                        shareListener(note)
                        true
                    }

                    R.id.action_shareImage -> {
                        val intent = Intent(view.context, ImageShareActivity::class.java).apply {
                            putExtra(
                                "EXTRA_FULL_VERSE_TEXT",
                                "${note.bookName} ${note.chapterNumber}\n${note.noteText}"
                            )
                        }
                        view.context.startActivity(intent)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        private fun formatStoredDate(storedDate: String): String {
            return try {
                val timestamp = storedDate.toLong()
                val date = Date(timestamp)
                val dateFormat = SimpleDateFormat(
                    "dd MMM yyyy | h:mm a",
                    Locale.getDefault()
                )
                dateFormat.format(date)
            } catch (e: Exception) {
                "Unknown Date & Time"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}

