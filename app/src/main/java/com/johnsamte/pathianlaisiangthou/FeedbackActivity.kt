package com.johnsamte.pathianlaisiangthou

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.johnsamte.pathianlaisiangthou.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Set up the button click listener
        binding.sendFeedbackButton.setOnClickListener {
            val title = binding.feedbackTitle.text.toString().trim()
            val body = binding.feedbackBody.text.toString().trim()

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                sendFeedback(title, body)
            }
        }
    }

    private fun sendFeedback(title: String, body: String) {
        val modifiedTitle = "Laisiangthou: $title" // Prepend "Laisiangthou" to the title
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("johnsamte09@new.lamka.net")) // Replace with your email
            putExtra(Intent.EXTRA_SUBJECT, modifiedTitle)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.feedbackToolbar)

        // Set the navigation click listener
        binding.feedbackToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.collapsingToolbar.apply {
            title = "Feedback Piakna"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }

        // If you want the up button to show
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)  // Avoid double title (CollapsingToolbar handles it)
        }

    }
}
