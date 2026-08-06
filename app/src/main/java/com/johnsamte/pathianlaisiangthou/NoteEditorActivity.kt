package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.johnsamte.pathianlaisiangthou.databinding.ActivityNoteEditorBinding

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private var selectedColor: Int = NoteColors.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.colorRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapter = NoteColorAdapter(NoteColors.COLORS) { color ->
            selectedColor = color

            // Update the root background live
            val drawable = binding.root.background as? GradientDrawable
            drawable?.setColor(color) ?: binding.root.setBackgroundColor(color)
        }

        binding.colorRecycler.adapter = adapter

        // ✅ Get data from intent
        val verseTexts = intent.getStringArrayListExtra("VERSE_TEXTS") ?: arrayListOf()
        val referenceKey = intent.getStringExtra("REFERENCE_KEY") ?: ""
        val bookName = intent.getStringExtra("BOOK_NAME") ?: ""
        val chapterNumber = intent.getIntExtra("CHAPTER_NUMBER", 0)
        val selectedVerseNumbers = intent.getIntegerArrayListExtra("VERSE_NUMBERS") ?: arrayListOf()

        // Combine verse numbers as a string: 1,2,3
        val verseNumberString = selectedVerseNumbers.joinToString(",")

        // Combine and clean for preview
        val combinedVerseText = verseTexts.joinToString("\n") { cleanText(it) }

        // Add book, chapter, and verse info at the top
        val displayText = SpannableStringBuilder().apply {
            // Book + chapter + verses in bold + bigger
            val header = "$bookName $chapterNumber:$verseNumberString\n\n"
            val spanHeader = SpannableString(header)
            spanHeader.setSpan(StyleSpan(Typeface.BOLD), 0, header.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanHeader.setSpan(RelativeSizeSpan(1.2f), 0, header.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(spanHeader)

            // Append the verse text normally
            append(combinedVerseText)
        }

        binding.textVersePreview.text = displayText
        binding.textVersePreview.setBackgroundResource(R.drawable.rounded_ouline)

        binding.btnSave.setOnClickListener {
            val noteText = binding.editNote.text.toString().trim()

            // If empty → use verse text
            val finalNote = noteText.ifBlank { cleanText(combinedVerseText) }

            val resultIntent = Intent().apply {
                putExtra("REFERENCE_KEY", referenceKey)
                putExtra("NOTE_TEXT", finalNote)
                putExtra("NOTE_COLOR", selectedColor)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun cleanText(verseText: String): String {
        return verseText
            .replace("*", "")
            .replace("\n\n", "") // Replace double newlines with a single space
            .replace("\\n", "\n")
            .replace("^\\s*\\d+[:.]?\\d*\\s+".toRegex(), "")  // remove verse number prefix
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}