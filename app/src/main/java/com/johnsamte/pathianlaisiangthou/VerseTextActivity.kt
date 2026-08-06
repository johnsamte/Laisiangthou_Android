package com.johnsamte.pathianlaisiangthou

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.KEY_SOURCE
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.PREF_NAME
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BI
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BSI
import com.johnsamte.pathianlaisiangthou.databinding.ActivityVerseTextBinding
import com.johnsamte.pathianlaisiangthou.databinding.NavHeaderBinding
import com.johnsamte.pathianlaisiangthou.databinding.TextSulianlehneuBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VerseTextActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerseTextBinding
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var laisiangthouDatabase: LaisiangthouRepository
    private var verseNumber: Int = -1
    private lateinit var verseAdapter: VerseTextAdapter
    private var bookId: Int = -1
    private var chapterNumber: Int = -1
    private var chapterId: Int = -1
    private lateinit var bookName: String
    private lateinit var verses: List<Verses>
    private var isBottomViewVisible = false
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerseTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouDatabase = LaisiangthouRepository(applicationContext)

        bookId = intent.getIntExtra("BOOK_ID", -1)
        chapterId = intent.getIntExtra("CHAPTER_ID", -1)
        chapterNumber = intent.getIntExtra("CHAPTER_NUMBER", -1)
        verseNumber = intent.getIntExtra("VERSE_NUMBER", -1)

        if (bookId == -1 && chapterId != -1) {
            bookId = laisiangthouDatabase.getBookIdFromChapterId(chapterId)
        }

        bookName = laisiangthouDatabase.getBookNameById(bookId)

        setupToolbar(bookName, chapterNumber)

        verses = laisiangthouDatabase.getVersesForChapter(chapterId)

        layoutManager = LinearLayoutManager(this)
        binding.versesRecyclerview.layoutManager = layoutManager

        verseAdapter = VerseTextAdapter(verses,this, this)
        binding.versesRecyclerview.adapter = verseAdapter

        if (bookId != -1) {
            loadVersesForChapter(chapterId)
        }

        // Highlight the selected verse
       // if (verseNumber != -1) {
           // verseAdapter.setHighlightedVerse(verseNumber)
       // }

        val referenceKey = intent.getStringExtra("REFERENCE_KEY")

        if (!referenceKey.isNullOrBlank()) {
            val versesToHighlight = parseReferenceKey(referenceKey)
            verseAdapter.setHighlightedVerse(versesToHighlight)
        } else if (verseNumber != -1) {
            verseAdapter.setHighlightedVerse(listOf(verseNumber))
        }

        setupBottomView()
        updateBookAndChapterText(bookName)
        loadVersesForSelectedVersion(chapterId)
        setupDrawer()
        setupDrawerFooter()

        if (chapterNumber != -1 && bookId != -1) {
            // Retrieve the chapterId
            chapterId = laisiangthouDatabase.getChapterId(bookId, chapterNumber)

            // Load the verses for the chapter
            loadVersesForChapter(chapterId)
        }
    }

    // Handle swipes to the previous chapter
    private fun navigateToPreviousChapter() {
        verseAdapter.clearSelectedVerses()

        //val dbHelper = BibleDatabaseHelper(this)
        val dbHelper = LaisiangthouRepository(applicationContext)

        // Check if we're in the first chapter of the first book (Genesis)
        if (chapterNumber == 1 && dbHelper.isFirstBook(bookId)) {
            // If we're in Genesis 1, stop and show a message
            Toast.makeText(this, "Abu kipatna", Toast.LENGTH_SHORT).show()
            return
        }

        // If we are not in the first chapter, just decrement chapter number
        if (chapterNumber > 1) {
            chapterNumber -= 1
        } else {
            // Attempt to move to the previous book
            val previousBookId = dbHelper.getPreviousBookId(bookId)
            if (previousBookId != null) {
                // If a previous book exists, move to the last chapter of the previous book
                bookId = previousBookId
                chapterNumber = dbHelper.getLastChapterNumberForBook(bookId)
                bookName = dbHelper.getBookName(bookId)
            } else {
                // If no previous book exists, show a message and stop
                Toast.makeText(this, "Abu beina hita", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Apply the slide-out animation
        binding.versesRecyclerview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left))

        // After the animation ends, update the view
        binding.versesRecyclerview.postDelayed({
            chapterId = getChapterId(bookId, chapterNumber)
            setupToolbar(bookName, chapterNumber)
            loadVersesForChapter(chapterId)
            updateBookAndChapterText(bookName)
            setupDrawer()
            //updateBookAndChapterText(currentBookName, currentChapterNumber)

            // Manage verse highlights:
           // verseAdapter.clearTemporaryHighlights() // Clear temporary highlights
           // verseAdapter.loadSavedColorsForChapter(chapterId) // Load saved highlights for the new chapter

            // Scroll to verse 1 (first position in RecyclerView)
            binding.versesRecyclerview.scrollToPosition(0)


            // Apply the slide-in animation
            binding.versesRecyclerview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right))
        }, 300) // This should match the duration of your slide-out animation
    }

    private fun navigateToNextChapter() {
        verseAdapter.clearSelectedVerses()

        //val dbHelper = BibleDatabaseHelper(this)
        val dbHelper = LaisiangthouRepository(applicationContext)
        val totalChapters = dbHelper.getTotalChaptersForBook(bookId)
        if (chapterNumber < totalChapters) {
            chapterNumber += 1 // Go to next chapter
        } else {
            // Attempt to retrieve the next book
            val nextBookId = dbHelper.getNextBookId(bookId)

            if (nextBookId != null) {
                bookId = nextBookId
                chapterNumber = 1 // Reset to first chapter of next book
                bookName = dbHelper.getBookName(bookId)  // Update bookName
            } else {
                Toast.makeText(this, "Abu tawpna hita", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Apply the slide-out animation
        binding.versesRecyclerview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right))

        // After the animation ends, update the view
        binding.versesRecyclerview.postDelayed({
            chapterId = getChapterId(bookId, chapterNumber)
            setupToolbar(bookName, chapterNumber)
            loadVersesForChapter(chapterId)
            updateBookAndChapterText(bookName)
            setupDrawer()

            // Manage verse highlights:
           // verseAdapter.clearTemporaryHighlights() // Clear temporary highlights
           // verseAdapter.loadSavedColorsForChapter(chapterId) // Load saved highlights for the new chapter

            // Scroll to verse 1 (first position in RecyclerView)
            binding.versesRecyclerview.scrollToPosition(0)

            // Apply the slide-in animation
            binding.versesRecyclerview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
        }, 300) // This should match the duration of your slide-out animation
    }

    private fun getChapterId(bookId: Int, chapterNumber: Int): Int {
        //val dbHelper = BibleDatabaseHelper(this)
        val dbHelper = LaisiangthouRepository(applicationContext)
        return dbHelper.getChapterIdForBookAndChapter(bookId, chapterNumber)
    }

    private fun loadVersesForChapter(chapterId: Int) {
        val dbHelper = LaisiangthouRepository(applicationContext)

        val verses = dbHelper.getVersesForChapter(chapterId)
        // Update the VersesAdapter with the paragraph-formatted verses
        verseAdapter.updateVerses(verses)

        val notedVerses = dbHelper.getNotedVerseNumbers(chapterId)
        verseAdapter.setNotedVerses(notedVerses)

        val bookmarkedVerses = dbHelper.getBookmarkedVerseNumbers(bookId, chapterNumber)
        verseAdapter.setBookmarkedVerses(bookmarkedVerses)
    }

    private fun setupToolbar(bookName: String, chapterNumber: Int) {
        //FontManager.applySavedFont(this)
        setSupportActionBar(binding.versesToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)  // Let CollapsingToolbar handle the title
        }
        binding.collapsingToolbar.apply {
            title = "$bookName $chapterNumber"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.verses_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("chapterId", chapterId)
                intent.putExtra("bookId", bookId)
                startActivity(intent)
                 true
            }
            R.id.action_text_adjust -> {
                //theBottomSheet()
                textSukletna()
                 true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
    private fun setupDrawer() {
        drawerLayout = binding.drawerLayout
        val navigationView = binding.navigationView
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.versesToolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        toggle.isDrawerIndicatorEnabled = true   // ✅ IMPORTANT
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.versesToolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ham)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                R.id.bookmarkte -> {
                    startActivity(Intent(this, BookmarksActivity::class.java))
                }
                R.id.notete -> {
                    startActivity(Intent(this, NotesActivity::class.java))
                }

                R.id.bi -> {
                    setBibleSource(SOURCE_BI)
                    LaisiangthouDatabaseManager.closeAll()
                    recreate()
                }

                R.id.bsi -> {
                    setBibleSource(SOURCE_BSI)
                    LaisiangthouDatabaseManager.closeAll()
                    recreate()
                }

                R.id. theme -> {

                    val themes = arrayOf(
                        "Light Theme",
                        "Dark Theme",
                        "System Default"
                    )

                    val current = when (ThemeManager.getTheme(this)) {
                        ThemeManager.THEME_LIGHT -> 0
                        ThemeManager.THEME_DARK -> 1
                        else -> 2
                    }

                    MaterialAlertDialogBuilder(this)
                        .setTitle("Theme Telna")
                        .setSingleChoiceItems(themes, current) { dialog, which ->

                            val selectedTheme = when (which) {
                                0 -> ThemeManager.THEME_LIGHT
                                1 -> ThemeManager.THEME_DARK
                                else -> ThemeManager.THEME_SYSTEM
                            }

                            ThemeManager.saveTheme(this, selectedTheme)
                            ThemeManager.applyTheme(this)

                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                R.id.about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
                R.id.feedback -> {
                    startActivity(Intent(this, FeedbackActivity::class.java))
                }
                R.id.updateCheck -> {
                    UpdateManager.checkForUpdate(this, showUpToDateDialog = true)
                }
                R.id.changelog -> {
                    startActivity(Intent(this, ChangelogActivity::class.java))
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setBibleSource(source: String) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit {
                putString(KEY_SOURCE, source)
            }
    }

    private fun updateBookAndChapterText(bookName: String) {
        val text = getString(R.string.bookName, bookName)

        // Create a SpannableString to apply text styling
        val spannableString = SpannableString(text)

        // Apply underline to the entire text (you can adjust the range for partial underlining)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the updated spannable text with underline
        binding.bottomView.bkchapText.text = spannableString

        // Make the TextView clickable
        binding.bottomView.bkchapText.setOnClickListener {
            // Create an Intent to navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()

        }
    }

    private fun setupBottomView() {
        // RecyclerView Scroll Listener to control bottom view visibility
        binding.versesRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !isBottomViewVisible) {
                    showBottomView()
                } else if (dy < 0 && isBottomViewVisible) {
                    hideBottomView()
                }
            }
        })

        // Setup navigation buttons in the bottom view
        binding.bottomView.prevBtn.setOnClickListener {
            navigateToPreviousChapter()
        }

        binding.bottomView.nxtBtn.setOnClickListener {
            navigateToNextChapter()
        }
    }

    private fun showBottomView() {
        // Show the bottom view with animation
        binding.bottomView.root.visibility = View.VISIBLE
        binding.bottomView.root.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
        isBottomViewVisible = true
    }

    private fun hideBottomView() {
        // Hide the bottom view with animation
        binding.bottomView.root.animate()
            .translationY(binding.bottomView.root.height.toFloat())
            .setDuration(300)
            .withEndAction {
                binding.bottomView.root.visibility = View.GONE
            }
            .start()

        isBottomViewVisible = false
    }

    private fun loadVersesForSelectedVersion(chapterId: Int) {
        // Use the selected version to fetch the verses from the database
        val verses = laisiangthouDatabase.getVersesForChapter(chapterId)
        if (::verseAdapter.isInitialized) {
            verseAdapter.updateVerses(verses)


        }
    }

    fun showBottomSheet(
        selectedVerseTexts: List<String>,
        selectedVerseNumbers: List<Int>,
        bookName: String,
        chapter: String
    ) {

        val actionBar = binding.verseActionBarContainer

        // 🔥 Build combined text ONCE
        val combinedText = selectedVerseTexts.joinToString("\n")

        // Add to Notes
        actionBar.root
            .findViewById<View>(R.id.icon_add_to_notes)
            .setOnClickListener {
                openFullscreenNoteEditor(
                    selectedVerseTexts,
                    selectedVerseNumbers
                )

                hideBottomSheet()
                verseAdapter.clearSelectedVerses()

            }

        // Bookmark
        actionBar.root.findViewById<View>(R.id.icon_bookmark).setOnClickListener {
            addToBookmarks(combinedText)
            hideBottomSheet()
            verseAdapter.clearSelectedVerses()

        }

        actionBar.root.findViewById<View>(R.id.icon_share).setOnClickListener {
            val resolvedBookName = laisiangthouDatabase.getBookNameById(bookId)
            val selectedVerses = verseAdapter.getSelectedVerses()
            shareVerses(
                combinedText,
                selectedVerses,
                resolvedBookName,
                chapterNumber
            )
            hideBottomSheet()
            verseAdapter.clearSelectedVerses()
        }

        // Copy
        actionBar.root.findViewById<View>(R.id.icon_copy).setOnClickListener {
            hideBottomSheet()
            verseAdapter.clearSelectedVerses()
            val resolvedBookName = laisiangthouDatabase.getBookNameById(bookId)
            copyVerses(combinedText,
                selectedVerseNumbers,
                resolvedBookName,
                chapterNumber)

        }

        // Image Share
        actionBar.root.findViewById<View>(R.id.icon_image_share).setOnClickListener {
            hideBottomSheet()
            val resolvedBookName = laisiangthouDatabase.getBookNameById(bookId)

            // get selected verses as list of Pair(verseNumber, verseText)
            val selectedVerses = verseAdapter.getSelectedVersesWithText() // implement this

            if (selectedVerses.isEmpty()) return@setOnClickListener

            val fullText = getFullVerseText(
                selectedVerses = selectedVerses,
                bookName = resolvedBookName,
                chapter = chapterNumber
            )

            val intent = Intent(this, ImageShareActivity::class.java)
            intent.putExtra("EXTRA_FULL_VERSE_TEXT", fullText as CharSequence)
            startActivity(intent)
            verseAdapter.clearSelectedVerses()
        }

        // Compare
        actionBar.root.findViewById<View>(R.id.icon_compare).setOnClickListener {
            hideBottomSheet()
            val intent = Intent(this, CompareActivity::class.java)
            intent.putExtra("VERSE_TEXT", combinedText)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("CHAPTER", chapter)
            intent.putExtra("CHAPTER_NUMBER", chapterNumber)
            startActivity(intent)
            verseAdapter.clearSelectedVerses()
        }

        // Animate in from bottom
        val view = actionBar.root
        if (view.visibility != View.VISIBLE) {
            view.visibility = View.VISIBLE
            view.translationY = view.height.toFloat()
            view.animate()
                .translationY(0f)
                .setDuration(250)
                .start()
        }
    }

    fun hideBottomSheet() {
        val view = binding.verseActionBarContainer.root
        if (view.isVisible) {
            view.animate()
                .translationY(view.height.toFloat())
                .setDuration(250)
                .withEndAction {
                    view.visibility = View.GONE
                }
                .start()
        }
    }
    private val noteEditorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            val referenceKey =
                result.data?.getStringExtra("REFERENCE_KEY") ?: return@registerForActivityResult

            val noteText =
                result.data?.getStringExtra("NOTE_TEXT") ?: return@registerForActivityResult

            val noteColor =
                result.data?.getIntExtra("NOTE_COLOR", -1) ?: return@registerForActivityResult


            addOrUpdateNote(referenceKey, noteText, noteColor)

            // ✅ ADD THIS
            val verses = parseReferenceKey(referenceKey)
            verseAdapter.addNotedVerses(verses)

            hideBottomSheet()
        }

    private fun openFullscreenNoteEditor(
        selectedVerseTexts: List<String>,
        selectedVerseNumbers: List<Int>

    ) {
        val referenceKey = buildReferenceKey(selectedVerseNumbers)

        val intent = Intent(this, NoteEditorActivity::class.java).apply {
            putExtra("BOOK_NAME", bookName)                         // single String
            putExtra("CHAPTER_NUMBER", chapterNumber)
            putStringArrayListExtra("VERSE_TEXTS", ArrayList(selectedVerseTexts))
            putIntegerArrayListExtra("VERSE_NUMBERS", ArrayList(selectedVerseNumbers))
            putExtra("REFERENCE_KEY", referenceKey)
        }

        noteEditorLauncher.launch(intent)
    }

    private fun buildReferenceKey(verses: List<Int>): String {

        if (verses.isEmpty()) return ""

        val sorted = verses.sorted()
        val ranges = mutableListOf<String>()

        var start = sorted.first()
        var end = start

        for (i in 1 until sorted.size) {
            if (sorted[i] == end + 1) {
                end = sorted[i]
            } else {
                ranges.add(if (start == end) "$start" else "$start-$end")
                start = sorted[i]
                end = start
            }
        }

        ranges.add(if (start == end) "$start" else "$start-$end")

        return ranges.joinToString(",")
    }

    private fun parseReferenceKey(referenceKey: String?): List<Int> {
        if (referenceKey.isNullOrBlank()) return emptyList()

        val result = mutableListOf<Int>()

        referenceKey.split(",").forEach { part ->
            if (part.contains("-")) {
                val (start, end) = part.split("-").map { it.trim().toInt() }
                for (i in start..end) {
                    result.add(i)
                }
            } else {
                result.add(part.trim().toInt())
            }
        }

        return result
    }

    private fun textSukletna() {
        val anchorView = binding.versesToolbar
        val popupBinding = TextSulianlehneuBinding.inflate(layoutInflater)

        val popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(null)
            isOutsideTouchable = true
            elevation = 12f
        }

        // --- Text size slider ---
        binding.versesRecyclerview.post {

            // 1️⃣ Catch current/default text size (px → sp)
            val defaultTextSizeSp = run {
                val firstChild = binding.versesRecyclerview.getChildAt(0)
                val verseTextView = firstChild
                    ?.findViewById<TextView>(R.id.verse_text) // <-- your TextView id

                if (verseTextView != null) {
                    verseTextView.textSize / resources.displayMetrics.density
                } else {
                    16f // fallback
                }
            }

            popupBinding.textSlider.apply {

                valueFrom = 10f
                valueTo = 30f
                stepSize = 0f

                value = defaultTextSizeSp
                popupBinding.textSizeValue.text = "${defaultTextSizeSp.toInt()}"

                setLabelFormatter(null) // label hidden

                addOnChangeListener { _, value, _ ->

                    popupBinding.textSizeValue.text = "${value.toInt()}"

                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        value,
                        resources.displayMetrics
                    )

                    for (i in 0 until binding.versesRecyclerview.childCount) {
                        binding.versesRecyclerview.getChildAt(i)?.let {
                            updateTextViewSizes(
                                it,
                                px / resources.displayMetrics.density
                            )
                        }
                    }
                }
            }
        }

        // --- Line spacing slider ---
        // Default line spacing (keep this somewhere central if possible)
        val defaultLineSpacing = 1.3f

        popupBinding.textLineSlider.apply {

            valueFrom = 1.0f
            valueTo = 5.0f
            stepSize = 0f

            // 1️⃣ Set initial value
            value = defaultLineSpacing
            popupBinding.textLineValue.text = String.format("%.1f", defaultLineSpacing)

            setLabelFormatter(null) // label hidden

            // 2️⃣ Listen for changes
            addOnChangeListener { _, value, _ ->

                popupBinding.textLineValue.text =
                    String.format("%.1f", value)

                for (i in 0 until binding.versesRecyclerview.childCount) {
                    binding.versesRecyclerview.getChildAt(i)?.let {
                        updateTextViewLineSpacing(it, value)
                    }
                }
            }
        }


        popupBinding.root.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val popupWidth = popupBinding.root.measuredWidth
        val anchorWidth = anchorView.width
        val xOffset = anchorWidth - popupWidth

        popupWindow.showAsDropDown(anchorView, xOffset, (-45).dp)
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun updateTextViewSizes(view: View, newSizeInSp: Float) {
        if (view is TextView) {
            view.textSize = newSizeInSp
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateTextViewSizes(view.getChildAt(i), newSizeInSp)
            }
        }
    }
    private fun updateTextViewLineSpacing(view: View, lineSpacingMultiplier: Float) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateTextViewLineSpacing(view.getChildAt(i), lineSpacingMultiplier)
            }
        } else if (view is TextView) {
            view.setLineSpacing(0f, lineSpacingMultiplier) // Adjust spacing without extra padding
        }
    }

    private fun getFullVerseText(
        selectedVerses: List<Pair<Int, String>>, // verseNumber + verseText
        bookName: String,
        chapter: Int
    ): SpannableStringBuilder {

        val fullText = SpannableStringBuilder()

        if (selectedVerses.isEmpty()) return fullText

        // Build reference part (like 1-3,5)
        val verseNumbers = selectedVerses.map { it.first }
        val referencePart = buildReferenceKey(verseNumbers)

        // Header
        val header = "$bookName $chapter:$referencePart\n"
        fullText.append(header)

        fullText.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            header.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        fullText.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
            0,
            header.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Body: clean each verse individually, then join
        val paragraph = selectedVerses
            .sortedBy { it.first }
            .joinToString(" ") { (_, text) ->
                cleanText(text)
                    .replace("\\n", " ")
                    .replace("\n", " ")
                    .replace(Regex("(?m)^\\s*\\d+\\s+"), "") // remove verse numbers anywhere at line start
                    .trim()
            }

        fullText.append(paragraph)

        return fullText
    }

    private fun shareVerses(
        combinedText: String,
        selectedVerseNumbers: List<Int>,
        bookName: String,
        chapter: Int
    ) {

        if (selectedVerseNumbers.isEmpty()) return

        val referencePart = buildReferenceKey(selectedVerseNumbers)

        val fullText = """
        *$bookName $chapter:$referencePart*
        
        ${cleanText(combinedText)
            .replace(Regex("(?m)^\\s*\\d+\\s+"), "")
            .replace("\n", " ")
            .replace("\n\n", " ")
            .trim()}
    """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, fullText)
        }

        startActivity(Intent.createChooser(intent, "Share Verses"))
    }

    // Helper function to extract the verse number
    private fun extractVerseNumber(text: String): String {
        val parts = text.split(" ", limit = 2) // Split into two parts: number and text
        return if (parts.isNotEmpty() && parts[0].all { it.isDigit() }) parts[0] else "1"
    }

    private fun copyVerses(
        combinedText: String,
        selectedVerseNumbers: List<Int>,
        bookName: String,
        chapter: Int
    ) {

        if (selectedVerseNumbers.isEmpty()) return

        val referencePart = buildReferenceKey(selectedVerseNumbers)

        val fullText = """
        $bookName $chapter:$referencePart
        
        ${cleanText(combinedText)
            .replace(Regex("(?m)^\\s*\\d+\\s+"), "")
            .replace("\n", " ")
            .replace("\n\n", " ")
            .trim()}
    """.trimIndent()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Verse", fullText)
        clipboard.setPrimaryClip(clip)

        //Toast.makeText(this, "Verse copied", Toast.LENGTH_SHORT).show()
    }

    private fun addOrUpdateNote(
        referenceKey: String,
        userNote: String,
        noteColor: Int
    ) {
        val success = laisiangthouDatabase.saveNote(
            chapterId = chapterId,
            referenceKey = referenceKey,
            noteText = userNote,
            noteColor = noteColor
        )

        if (success) {
            val updatedSet = laisiangthouDatabase.getNotedVerseNumbers(chapterId)
            verseAdapter.setNotedVerses(updatedSet)
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val updatedNotes = laisiangthouDatabase.getNotedVerseNumbers(chapterId)
        verseAdapter.setNotedVerses(updatedNotes)

    }

    private fun addToBookmarks(verseText: String) {
        //val dbHelper = BibleDatabaseHelper(binding.root.context)
        val dbHelper = LaisiangthouRepository(applicationContext)
        val latestBookName = dbHelper.getBookName(bookId)
        val latestChapterNumber = chapterNumber
        val bookId = laisiangthouDatabase.getBookIdByName(latestBookName)
        val currentDate = getCurrentTimestamp()

        val cleanedText = cleanText(verseText)
        val verses = cleanedText.split("\n")

        val fullText = StringBuilder()
        val duplicateVerses = mutableListOf<Int>()
        val newBookmarks = mutableListOf<Int>()

        verses.forEach { verseLine ->
            val cleanedVerse = cleanText(verseLine)
            if (cleanedVerse.isNotEmpty()) {
                val verseNumber = extractVerseNumber(cleanedVerse).toIntOrNull() ?: return@forEach
                val verseContent = cleanedVerse.removePrefix("$verseNumber ")

                if (dbHelper.isBookmarkAlreadyExists(bookId, latestChapterNumber, verseNumber)) {
                    duplicateVerses.add(verseNumber)
                } else {
                    fullText.append("$verseNumber $verseContent\n")

                    val bookmark = Bookmark(
                        bookId = bookId,
                        bookName = latestBookName,
                        bookmarkText = verseContent,
                        chapterNumber = latestChapterNumber,
                        verseNumber = verseNumber,
                        createdAt = currentDate
                    )

                    val result = dbHelper.addBookmark(bookmark)
                    if (result > 0) {
                        newBookmarks.add(verseNumber)
                    }
                }
            }
        }

        if (newBookmarks.isNotEmpty()) {
            val bookmarkedVerses = dbHelper.getBookmarkedVerseNumbers(bookId, chapterNumber)
            verseAdapter.setBookmarkedVerses(bookmarkedVerses)
        }

        if (duplicateVerses.isNotEmpty()) {
            val message = "Tua abung-achang na bookmark dekpen ki-bookmark khinta!"

            AlertDialog.Builder(binding.root.context)
                .setTitle("Ki-Bookmark khin")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun cleanText(verseText: String): String {
        val lines = verseText.split("\n")
        // Clean up each line by trimming spaces and replacing multiple spaces with a single one
        return lines.joinToString("\n") {
            it.trim().replace("\\s+".toRegex(), " ")  // Replace multiple spaces with a single space
                .replace("\n\n", "") // Replace double newlines with a single space
                .replace("\\n", "\n")   // Replace any remaining single newline with a space
                .replace("*", "") // Replace multiple spaces with a single space
        }
    }

    private fun setupDrawerFooter() {
        val headerView = binding.navigationView.getHeaderView(0)
        val headerBinding = NavHeaderBinding.bind(headerView)

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0"
        }

        headerBinding.appVersion.text =
            getString(R.string.app_version, versionName)
    }
}