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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.johnsamte.pathianlaisiangthou.databinding.ActivitySearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var laisiangthouRepo: LaisiangthouRepository
    private var chapterId: Int = -1
    private var bookId: Int = -1
    private var selectedRadioButtonId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouRepo = LaisiangthouRepository(applicationContext)

        // Retrieve chapterId and bookId from the intent
        chapterId = intent.getIntExtra("chapterId", -1)
        bookId = intent.getIntExtra("bookId", -1)

        // Set up RecyclerView with adapter
        searchAdapter = SearchAdapter()
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = searchAdapter

        // Set up search button click listener
        binding.searchBtn.setOnClickListener {
            performSearch()
        }

        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    searchAdapter.submitListWithQuery(emptyList(), "")
                    binding.totalCountTextView.visibility = View.GONE

                    // 🧹 Clear RadioButton selection too
                    binding.radioCurrentChapter.isChecked = false
                    binding.radioWholeBook.isChecked = false
                    selectedRadioButtonId = -1
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // Set up toolbar
        setupToolbar()

        // Set up search options (RadioButtons)
        setupSearchOptions()
    }

    private fun setupSearchOptions() {
        // Set listeners for the RadioButtons
        binding.radioCurrentChapter.setOnClickListener {
            updateRadioButtonSelection(binding.radioCurrentChapter.id)
        }

        binding.radioWholeBook.setOnClickListener {
            updateRadioButtonSelection(binding.radioWholeBook.id)
        }
    }

    private fun updateRadioButtonSelection(selectedId: Int) {
        // Deselect all RadioButtons
        binding.radioCurrentChapter.isChecked = false
        binding.radioWholeBook.isChecked = false

        // Select the clicked RadioButton
        when (selectedId) {
            binding.radioCurrentChapter.id -> binding.radioCurrentChapter.isChecked = true
            binding.radioWholeBook.id -> binding.radioWholeBook.isChecked = true
        }

        // Update the selectedRadioButtonId variable
        selectedRadioButtonId = selectedId
    }

    private fun cleanText(verseText: String): String {
        return verseText
            .replace("\n\n", "") // Replace double newlines with a single space
            .replace("\n", " ")   // Replace single newlines with a space
            .replace("\\s+".toRegex(), " ") // Replace multiple spaces with a single space
            .trim() // Trim leading and trailing spaces
    }

    private fun performSearch() {
        val query = binding.searchText.text.toString()

        if (query.isNotBlank()) {
            // Show progress bar and hide search button
            binding.progressBar.visibility = View.VISIBLE
            binding.searchBtn.visibility = View.GONE

            lifecycleScope.launch {
                val results = withContext(Dispatchers.IO) {
                    when (selectedRadioButtonId) {
                        binding.radioCurrentChapter.id -> laisiangthouRepo.searchInWholeBook(query, bookId)
                        binding.radioWholeBook.id -> laisiangthouRepo.searchBiblePumpi(query)
                        else -> emptyList()
                    }
                }

                val cleanedResults = results.map { result ->
                    result.copy(text = cleanText(result.text))
                }

                val exactWordRegex = Regex("\\b${Regex.escape(query)}\\b", RegexOption.IGNORE_CASE)
                val filteredResults = cleanedResults.filter { result ->
                    exactWordRegex.containsMatchIn(result.text)
                }

                searchAdapter.submitListWithQuery(filteredResults, query)

                val totalHighlightCount = filteredResults.sumOf {
                    countExactWordOccurrences(it.text, query)
                }

                // Hide progress bar and show search button again
                binding.progressBar.visibility = View.GONE
                binding.searchBtn.visibility = View.VISIBLE

                if (totalHighlightCount > 0) {
                    val totalCountText = getString(R.string.total_results, totalHighlightCount)
                    val spannable = SpannableString(totalCountText)
                    val countString = totalHighlightCount.toString()
                    val startIndex = totalCountText.indexOf(countString)
                    val endIndex = startIndex + countString.length

                    if (startIndex != -1) {
                        spannable.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(this@SearchActivity, R.color.red)),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        // Make text bold
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
                    Toast.makeText(this@SearchActivity, "Na thil zon kimulou.!!\nZonna mun ding teel masak di.!!!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.totalCountTextView.visibility = View.GONE
            Toast.makeText(this, "Hehpih in Zon ding thumalte gelh masa in!!", Toast.LENGTH_SHORT).show()
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
            title = "Thumal zon-awlna"
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
