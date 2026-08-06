package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AppCompatActivity
import com.johnsamte.pathianlaisiangthou.databinding.ActivityCompareBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CompareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareBinding
    private lateinit var laisiangthouRepository: LaisiangthouRepository
    private var bookId: Int = -1
    private var chapterNumber: Int = -1
    private lateinit var bookName: String

    // 🔥 Reuse Retrofit (do NOT rebuild every call)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://bible-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val bookNameMap = mapOf(
        "Genesis" to "Genesis",  "Pianchilna" to "Genesis",
        "Eksodas" to "Exodus", "Pemkhiakna" to "Exodus",
        "Levitikas" to "Leviticus", "Siampilaibu" to "Leviticus",
        "Nambars" to "Numbers", "Kisimna" to "Numbers",
        "Diuteronomi" to "Deuteronomy", "Danpiakthakna" to "Deuteronomy",
        "Joshua" to "Joshua",
        "Vaihawmte" to "Judges",
        "Ruth" to "Ruth",
        "1 Samuel" to "1 Samuel",
        "2 Samuel" to "2 Samuel",
        "1 Kumpipate" to "1 Kings",
        "2 Kumpipate" to "2 Kings",
        "1 Kronikil" to "1 Chronicles", "1 Khangtangthubute" to "1 Chronicles",
        "2 Kronikil" to "2 Chronicles", "2 Khangtangthubute" to "2 Chronicles",
        "Ezra" to "Ezra",
        "Nehemiah" to "Nehemiah", "Nehemia" to "Nehemiah",
        "Esther" to "Esther",
        "Job" to "Job",
        "Sam" to "Psalms", "Phatnalate" to "Psalms",
        "Paunakte" to "Proverbs",
        "Eklisiasti" to "Ecclesiastes", "Sinsakpa" to "Ecclesiastes",
        "Solomon Late" to "Song of Songs", "Late’ La" to "Song of Songs",
        "Isai" to "Isaiah",
        "Jeremiah" to "Jeremiah", "Jeremia" to "Jeremiah",
        "Kahlate" to "Lamentations",
        "Ezekiel" to "Ezekiel",
        "Daniel" to "Daniel",
        "Hosia" to "Hosea",
        "Joel" to "Joel",
        "Amos" to "Amos",
        "Obadiah" to "Obadiah", "Obadia" to "Obadiah",
        "Jonah" to "Jonah", "Jona" to "Jonah",
        "Mikah" to "Micah", "Mika" to "Micah",
        "Nahum" to "Nahum",
        "Habakkuk" to "Habakkuk", "Habakuk" to "Habakkuk",
        "Zephaniah" to "Zephaniah", "Zefania" to "Zephaniah",
        "Haggai" to "Haggai", "Hagai" to "Haggai",
        "Zekariah" to "Zechariah", "Zekharia" to "Zechariah",
        "Malaki" to "Malachi", "Malakhi" to "Malachi",
        "Matthai" to "Matthew", "Mathai" to "Matthew",
        "Marka" to "Mark",
        "Luka" to "Luke",
        "Johan" to "John",
        "Nasepte" to "Acts",
        "Romete" to "Romans",
        "1 Korinthte" to "1 Corinthians",
        "2 Korinthte" to "2 Corinthians",
        "Ephesate" to "Ephesians", "Efesate" to "Ephesians",
        "Philippite" to "Philippians", "Filipite" to "Philippians",
        "Kolosate" to "Colossians",
        "1 Thessalonikate" to "1 Thessalonians", "1 Thesalonikate" to "1 Thessalonians",
        "2 Thessalonikate" to "2 Thessalonians", "2 Thesalonikate" to "2 Thessalonians",
        "1 Timothi" to "1 Timothy",
        "2 Timothi" to "2 Timothy",
        "Titas" to "Titus", "Tita" to "Titus",
        "Philemon" to "Philemon", "Filemon" to "Philemon",
        "Hebrute" to "Hebrews",
        "Jakob" to "James",
        "1 Peter" to "1 Peter",
        "2 Peter" to "2 Peter",
        "1 Johan" to "1 John",
        "2 Johan" to "2 John",
        "3 Johan" to "3 John",
        "Juda" to "Jude",
        "Kilakna" to "Revelation"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouRepository = LaisiangthouRepository(applicationContext)

        val verseText = intent.getStringExtra("VERSE_TEXT") ?: ""
        val chapter = intent.getStringExtra("CHAPTER")?.toIntOrNull() ?: 1
        chapterNumber = intent.getIntExtra("CHAPTER_NUMBER", -1)
        bookId = intent.getIntExtra("BOOK_ID", -1)
        bookName = laisiangthouRepository.getBookNameById(bookId)

        // 🔥 Extract ALL verse numbers
        val verseNumbers = extractVerseNumbers(verseText)

        // 🔥 Setup toolbar properly
        setupToolbar(bookName, chapterNumber, verseNumbers)

        // Show selected verses (your version)
        binding.yourVerseText.text = getFullVerseText(verseText)

        binding.yourVersionLabel.text =
            getString(
                R.string.laisiangthou_version_label,
                getSelectedBibleSource()
            )

        val verseQuery = buildVerseQuery(verseNumbers)
        val englishBookName = bookNameMap[bookName] ?: bookName

        fetchVerse(englishBookName, chapter, verseQuery, "kjv") {
            binding.kjvVerseText.text = it ?: "KJV not available."
        }

        fetchVerse(englishBookName, chapter, verseQuery, "web") {
            binding.webVerseText.text = it ?: "WEB not available."
        }
    }

    // ----------------------------------------------------
    // 🔥 Extract multiple verse numbers
    // ----------------------------------------------------
    private fun extractVerseNumbers(text: String): List<Int> {
        return text.split("\n")
            .mapNotNull { line ->
                Regex("^\\d+").find(line.trim())?.value?.toIntOrNull()
            }
    }

    // ----------------------------------------------------
    // 🔥 Build API query (comma separated)
    // ----------------------------------------------------
    private fun buildVerseQuery(verseNumbers: List<Int>): String {
        return verseNumbers.sorted().joinToString(",")
    }

    // ----------------------------------------------------
    // 🔥 Format toolbar title
    // ----------------------------------------------------
    private fun formatVerseTitle(verses: List<Int>): String {
        val sorted = verses.sorted()

        return if (sorted.size > 1 &&
            sorted.zipWithNext().all { it.second == it.first + 1 }
        ) {
            "${sorted.first()}-${sorted.last()}"
        } else {
            sorted.joinToString(",")
        }
    }

    // ----------------------------------------------------
    // 🔥 Proper Toolbar Setup
    // ----------------------------------------------------
    private fun setupToolbar(
        bookName: String,
        chapterNumber: Int,
        verseNumbers: List<Int>
    ) {
        setSupportActionBar(binding.compareToolbar)

        binding.compareToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val verseTitle = formatVerseTitle(verseNumbers)

        binding.collapsingToolbar.title =
            "$bookName $chapterNumber:$verseTitle"

        binding.collapsingToolbar.setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
        binding.collapsingToolbar.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    // ----------------------------------------------------
    // 🔥 Unified fetch method (KJV + WEB)
    // ----------------------------------------------------
    private fun fetchVerse(
        bookName: String,
        chapter: Int,
        verseQuery: String,
        translation: String,
        callback: (String?) -> Unit
    ) {
        val api = retrofit.create(BibleApiService::class.java)

        val formattedQuery =
            "$bookName $chapter:$verseQuery".replace(" ", "%20")

        api.getVerses(formattedQuery, translation)
            .enqueue(object : Callback<BibleApiResponse> {

                override fun onResponse(
                    call: Call<BibleApiResponse>,
                    response: Response<BibleApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val text = response.body()?.verses
                            ?.joinToString("\n") { it.text.trim() }

                        runOnUiThread { callback(text) }
                    } else {
                        runOnUiThread { callback(null) }
                    }
                }

                override fun onFailure(call: Call<BibleApiResponse>, t: Throwable) {
                    runOnUiThread { callback(null) }
                }
            })
    }

    // ----------------------------------------------------
    // 🔥 Display your version
    // ----------------------------------------------------
    private fun getFullVerseText(verseText: String): SpannableStringBuilder {
        val fullText = SpannableStringBuilder()
        val cleanedText = cleanText(verseText)
        val verses = cleanedText.split("\n")

        verses.forEach { verseLine ->
            val verseNumber = Regex("^\\d+").find(verseLine)?.value ?: ""
            val verseContent = verseLine.removePrefix("$verseNumber ")
            fullText.append(verseContent)
            fullText.append("\n")
        }

        return fullText
    }

    private fun cleanText(verseText: String): String {
        val lines = verseText.split("\n")
        // Clean up each line by trimming spaces and replacing multiple spaces with a single one
        return lines.joinToString("\n") {
            it.trim().replace("\\s+".toRegex(), " ")  // Replace multiple spaces with a single space
                .replace("\n\n", "") // Replace double newlines with a single space
                .replace("\\n", "")   // Replace any remaining single newline with a space
                .replace("*", "") // Replace multiple spaces with a single space
        }
    }

    // ----------------------------------------------------
    // 🔥 Bible Source
    // ----------------------------------------------------
    fun Context.getSelectedBibleSource(): String {
        val prefs = getSharedPreferences(
            LaisiangthouDatabaseManager.PREF_NAME,
            MODE_PRIVATE
        )
        return prefs.getString(
            LaisiangthouDatabaseManager.KEY_SOURCE,
            LaisiangthouDatabaseManager.SOURCE_BSI
        ) ?: LaisiangthouDatabaseManager.SOURCE_BSI
    }
}