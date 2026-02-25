package com.johnsamte.pathianlaisiangthou

import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.johnsamte.pathianlaisiangthou.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadContent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.aboutToolbar)

        // Set the navigation click listener
        binding.aboutToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.collapsingToolbar.apply {
            title = "About"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }

        // If you want the up button to show
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)  // Avoid double title (CollapsingToolbar handles it)
        }
    }

    private fun loadContent() {
        // Example rich text (HTML)
        val htmlText = """
            <b>Laisiangthou App</b><br>

The <b>Laisiangthou</b> app is designed to bring the word of God to your fingertips. 
It provides a faithful translation of the Bible in Paite (Laisiangthou) language, making it easier for users to read, understand, and reflect on scripture.<br><br>

The app is intended for everyone — from students and pastors to families and individuals seeking spiritual guidance. 
Whether you are reading for daily devotion, study, or worship, <b>Laisiangthou</b> offers tools to enhance your experience, including bookmarking, note-taking, and highlighting. 
With its intuitive interface, users can navigate chapters and verses effortlessly and focus on spiritual growth without distraction.<br><br>

<b>Key Features:</b><br>
• Read the full Bible in Paite with chapter and verse navigation.<br>
• Bookmark your favorite verses for quick access.<br>
• Add personal notes to verses for reflection or study.<br>
• Multi-verse selection for sharing or copying text.<br>
• Search for specific words, phrases, or verses.<br>
• Share verses as text or as styled images with friends and family.<br><br>

<b>Design Philosophy:</b><br>
The app is built for simplicity and accessibility. Our goal is to provide a smooth reading experience with clean typography, responsive layouts, and easy navigation.<br><br>

<b>Contact & Feedback:</b><br>
We welcome feedback, suggestions, and questions. Please reach out to our support team at <i>johnsamte09@new.lamka.net</i>.<br><br>

<b>Disclaimer:</b><br>
All scripture text is for personal study and reference.<br> © All rights reserved.
        """.trimIndent()

        // Convert HTML to Spannable for TextView
        binding.aboutText.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Load image if needed dynamically
       // binding.aboutImage.setImageResource(R.drawable.ic_launcher_foreground)
    }

    //override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //if (item.itemId == android.R.id.home) {
           // finish()
           // return true
       // }
       // return super.onOptionsItemSelected(item)
   // }
}