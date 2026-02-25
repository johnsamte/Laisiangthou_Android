package com.johnsamte.pathianlaisiangthou

import android.content.Intent
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
    private val shareListener: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {

            // Book + Chapter + Verse
            binding.booksChapt.text =
                "${note.bookName} ${note.chapterNumber}:${note.referenceKey}"

            // Note text
            binding.textViewNote.text = note.noteText

            // Timestamp (shows updatedAt if edited)
            val timestampToUse =
                if (note.updatedAt != note.createdAt)
                    note.updatedAt
                else
                    note.createdAt

            binding.textViewTimestamp.text = formatStoredDate(timestampToUse)

            // Background
            binding.root.setBackgroundResource(R.drawable.notes_bg)

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

