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

<b>Laisiangthou peuhmah Pathian hatkhum a piak ahi a; 
huaiziak in thuhilhna ding, taihilhna ding, bawlhoihna ding, 
diktatna lam a kichilna ding in aphatuam hi.</b> <i>2 Tim 3:16</i><br><br>

Huaiziak in hiai Laisiangthou apps in Laisiangthou hawmzakna nasem ahih man in, 
gingtu tamsem hong kibehlapna ding bang in phattuamna hon neih khak leh chih lam etna toh, 
hiai app kuhkal leh chihtakna toh hong kibawlkhia hi.<br><br>


Kuapeuhmah hiai <b>Laisiangthou App</b> toh kisai a kupnop hiam, hichi zodeuh leh chih nei hiam i om leh
chiklaipeuh in a nuai a Email kipia leh <b>Feedback piakna</b> tungtawn in kithuzak theih gige ding hi.<br><br>

<b>Developed by:</b> Johnsamte<br>
<b>Email:</b> <a href="mailto:johnsamte09@new.lamka.net">johnsamte09@new.lamka.net</a><br><br>

<b>Disclaimer:</b><br>
All scripture text is for personal study and reference.<br> © All rights reserved.<br><br>
        """.trimIndent()

        // Convert HTML to Spannable for TextView
        binding.aboutText.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)

    }
}