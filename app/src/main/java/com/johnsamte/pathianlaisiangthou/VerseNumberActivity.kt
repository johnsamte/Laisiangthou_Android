package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.johnsamte.pathianlaisiangthou.databinding.ActivityVerseNumberBinding

class VerseNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerseNumberBinding
    private lateinit var verseAdapter: VerseNumberAdapter
    private lateinit var bibleRepository: LaisiangthouRepository
    private var bookId: Int = -1
    private var chapterId : Int = -1
    private var chapterNumber: Int = -1
    private lateinit var bookName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerseNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bibleRepository = LaisiangthouRepository(applicationContext)

        chapterNumber = intent.getIntExtra("CHAPTER_NUMBER", -1)
        bookId = intent.getIntExtra("BOOK_ID", -1)
        chapterId = intent.getIntExtra("CHAPTER_ID", -1)
        bookName = bibleRepository.getBookNameById(bookId)

        setupToolbar(bookName, chapterNumber)

        val verseNumbers = bibleRepository.getVerseNumbers(chapterId)

        verseAdapter = VerseNumberAdapter(verseNumbers, onVerseClick = { verseNumber ->
            // Launch VerseActivity when a verse number is clicked
            val intent = Intent(this, VerseTextActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("CHAPTER_NUMBER", chapterNumber)
            intent.putExtra("CHAPTER_ID", chapterId)
            intent.putExtra("VERSE_NUMBER", verseNumber)
            startActivity(intent)
        } )
        val spanCount = calculateNoOfColumns(this)  // Calculate columns based on screen width
        binding.recyclerViewVerseNumber.layoutManager = GridLayoutManager(this, spanCount)
        binding.recyclerViewVerseNumber.adapter = verseAdapter

    }

    private fun setupToolbar(bookName: String, chapterNumber: Int) {
        setSupportActionBar(binding.verseNumberToolbar)
        binding.verseNumberToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.collapsingToolbar.apply {
            title = "$bookName $chapterNumber"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val scalingFactor = 60
        return (dpWidth / scalingFactor).toInt()
    }

}