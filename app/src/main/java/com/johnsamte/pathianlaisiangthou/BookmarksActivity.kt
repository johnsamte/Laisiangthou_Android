package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.johnsamte.pathianlaisiangthou.databinding.ActivityBookmarksBinding

class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBookmarksBinding
    private lateinit var bookmarksAdapter: BookmarksAdapter
    private var bookmarkList: List<Bookmark> = listOf()
    private lateinit var laisiangthouRepo: LaisiangthouRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        laisiangthouRepo = LaisiangthouRepository(applicationContext)

        binding.recyclerViewBookmarks.layoutManager = LinearLayoutManager(this)


        bookmarksAdapter = BookmarksAdapter(bookmarkList, ::onDeleteBookmark, ::onShareBookmark)
        binding.recyclerViewBookmarks.adapter = bookmarksAdapter

        loadBookmarks()

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.bookmarkToolbar)

        // Set the navigation click listener
        binding.bookmarkToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.collapsingToolbar.apply {
            title = "Bookmark A Chiamtehte"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }

        // If you want the up button to show
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)  // Avoid double title (CollapsingToolbar handles it)
        }
    }

    private fun loadBookmarks() {
        val bookmarkList = laisiangthouRepo.getAllBookmarks()
        bookmarksAdapter.updateBookmarks(bookmarkList)

        if (bookmarkList.isEmpty()) {
            binding.recyclerViewBookmarks.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerViewBookmarks.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }



    private fun onDeleteBookmark(bookmark: Bookmark) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Chiamtehte Nulmangna")
            .setMessage("Chiamtehte na nulmang/paikhia taktak nuam maw?")
            .setPositiveButton("Hi") { _, _ ->
                val result = laisiangthouRepo.deleteBookmark(bookmark.id)
                if (result > 0) {
                    loadBookmarks() // Refresh list
                }
            }
            .setNegativeButton("Nungkik", null)
            .show()
    }


    private fun onShareBookmark(bookmark: Bookmark) {
        // Share the note
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${bookmark.bookName} ${bookmark.chapterNumber}\n${bookmark.bookmarkText}")
        }
        startActivity(Intent.createChooser(intent, "Share Bookmarks"))
    }
}
