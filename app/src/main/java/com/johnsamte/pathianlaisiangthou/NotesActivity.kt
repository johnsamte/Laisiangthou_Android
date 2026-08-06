package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.johnsamte.pathianlaisiangthou.databinding.ActivityNotesBinding

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var laisiangthouRepo: LaisiangthouRepository
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        laisiangthouRepo = LaisiangthouRepository(applicationContext)

        setupRecyclerView()

        loadNotes()
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            editListener = ::onEditNote,
            deleteListener = ::onDeleteNote,
            shareListener = ::onShareNote
        )

        binding.recyclerViewNotes.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = notesAdapter
        }
    }

    private fun loadNotes() {
        val notes = laisiangthouRepo.getAllNotes()

        notesAdapter.submitList(notes)

        if (notes.isEmpty()) {
            binding.recyclerViewNotes.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerViewNotes.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.notesToolbar)

        binding.notesToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.collapsingToolbar.apply {
            title = "Notes A Koihlute"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    // =========================
    // EDIT
    // =========================

    private fun onEditNote(note: Note) {
        showEditNoteDialog(note)
    }

    private fun showEditNoteDialog(note: Note) {

        val inputEditText = TextInputEditText(this).apply {
            setText(note.noteText)
            hint = "bawldik thak dingte"
            setPadding(32, 16, 32, 16)
            background = null
        }

        val inputLayout = TextInputLayout(this).apply {
            setPadding(32, 16, 32, 0)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxStrokeColor = ContextCompat.getColor(context, R.color.red)
            addView(inputEditText)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Bawldik Thakna")
            .setView(inputLayout)
            .setPositiveButton("Bawldikta") { _, _ ->

                val updatedText = inputEditText.text.toString().trim()

                if (updatedText.isBlank()) {
                    Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = laisiangthouRepo.saveNote(
                    chapterId = note.chapterId,
                    referenceKey = note.referenceKey,
                    noteText = updatedText,
                    noteColor = note.noteColor
                )

                if (success) {
                    Toast.makeText(this, "Kibawlpha thak hi", Toast.LENGTH_SHORT).show()
                    loadNotes()
                } else {
                    Toast.makeText(this, "Kibawlpha thaklou hi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Nungkik", null)
            .show()
    }

    // =========================
    // DELETE
    // =========================

    private fun onDeleteNote(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Note akoihlutte Nulmangna")
            .setMessage("Note a na koihlutte na nulmang/paikhia taktak nuam maw?")
            .setPositiveButton("Hi") { _, _ ->

                val result = laisiangthouRepo.deleteNote(note.id)

                if (result > 0) {
                    loadNotes()
                }
            }
            .setNegativeButton("Nungkik", null)
            .show()
    }

    // =========================
    // SHARE
    // =========================

    private fun onShareNote(note: Note) {

        val heading = "${note.bookName} ${note.chapterNumber}:${note.referenceKey}"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "$heading\n\n${note.noteText}"
            )
        }

        startActivity(Intent.createChooser(intent, "Share Note"))
    }
}
