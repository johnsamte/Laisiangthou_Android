package com.johnsamte.pathianlaisiangthou

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.johnsamte.pathianlaisiangthou.databinding.ActivityMainSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainSearchBinding
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var laisiangthouRepo: LaisiangthouRepository
    private var searchJob: Job? = null
    private var chapterId: Int = -1
    private var bookId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouRepo = LaisiangthouRepository(applicationContext)

        // Retrieve chapterId and bookId from the intent
        chapterId = intent.getIntExtra("chapterId", -1)
        bookId = intent.getIntExtra("bookId", -1)

        // Set up RecyclerView with adapter
        searchAdapter = SearchAdapter()
        binding.searchMainRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchMainRecyclerView.adapter = searchAdapter

        binding.searchEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {

                val query = s?.toString()?.trim().orEmpty()

                // Show / hide clear button
                if (query.isEmpty()) {

                    if (binding.clearCard.visibility == View.VISIBLE) {
                        binding.clearCard.animate()
                            .alpha(0f)
                            .scaleX(0.7f)
                            .scaleY(0.7f)
                            .setDuration(180)
                            .withEndAction {
                                binding.clearCard.visibility = View.GONE
                            }
                            .start()
                    }

                } else {

                    if (binding.clearCard.visibility != View.VISIBLE) {
                        binding.clearCard.visibility = View.VISIBLE
                        binding.clearCard.alpha = 0f
                        binding.clearCard.scaleX = 0.7f
                        binding.clearCard.scaleY = 0.7f

                        binding.clearCard.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(180)
                            .start()
                    }
                }

                searchJob?.cancel()

                if (query.isBlank()) {
                    searchAdapter.submitListWithQuery(emptyList(), "")
                    binding.totalCountTextView.visibility = View.GONE
                    return
                }

                searchJob = lifecycleScope.launch {
                    delay(300)
                    performSearch(query)
                }
            }
        })

        binding.clearButton.setOnClickListener {
            binding.searchEdit.text?.clear()
        }

        // Set up toolbar
        setupToolbar()

    }

    private fun cleanText(verseText: String): String {
        return verseText
            .replace("\n\n", "") // Replace double newlines with a single space
            .replace("\n", " ")   // Replace single newlines with a space
            .replace("\\s+".toRegex(), " ") // Replace multiple spaces with a single space
            .trim() // Trim leading and trailing spaces
    }

    private suspend fun performSearch(query: String) {

        val results = withContext(Dispatchers.IO) {
            // this will automatically check if it's a reference or normal text
            laisiangthouRepo.searchBiblePumpi(query)
        }

        val cleanedResults = results.map { result ->
            result.copy(text = cleanText(result.text))
        }

        // only apply exact word filter for normal text search
        val filteredResults =
            if (Regex("""^\s*[a-zA-Z]+\s+\d+:\d+\s*$""").matches(query)) {
                cleanedResults
            } else {
                val exactWordRegex =
                    Regex("\\b${Regex.escape(query)}\\b", RegexOption.IGNORE_CASE)

                cleanedResults.filter { result ->
                    exactWordRegex.containsMatchIn(result.text)
                }
            }

        // Ignore stale searches
        if (binding.searchEdit.text.toString().trim() != query) {
            return
        }

        searchAdapter.submitListWithQuery(filteredResults, query)

        val totalHighlightCount = filteredResults.sumOf {
            countExactWordOccurrences(it.text, query)
        }

        if (totalHighlightCount > 0) {

            val totalCountText =
                getString(R.string.total_results, totalHighlightCount)

            val spannable = SpannableString(totalCountText)

            val countString = totalHighlightCount.toString()
            val startIndex = totalCountText.indexOf(countString)
            val endIndex = startIndex + countString.length

            if (startIndex != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@MainSearchActivity,
                            R.color.red
                        )
                    ),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding.totalCountTextView.text = spannable
            binding.totalCountTextView.visibility = View.VISIBLE

        } else {
            binding.totalCountTextView.visibility = View.GONE
        }
    }



    /**
     * Count exact word occurrences in a text
     */
    private fun countExactWordOccurrences(text: String, word: String): Int {
        val regex = Regex("\\b$word\\b", RegexOption.IGNORE_CASE) // \b ensures whole-word matching
        return regex.findAll(text).count()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.searchToolbars)

        // Set the navigation click listener
        binding.searchToolbars.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.collapsingToolbar.apply {
            title = "Bible Pumpi Zon-awlna"
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