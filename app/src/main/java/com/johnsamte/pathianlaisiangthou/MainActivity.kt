package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LineHeightSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.KEY_SOURCE
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.PREF_NAME
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BI
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BSI
import com.johnsamte.pathianlaisiangthou.databinding.ActivityMainBinding
import com.johnsamte.pathianlaisiangthou.databinding.NavHeaderBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var laisiangthouDatabase: LaisiangthouRepository
    private lateinit var laisiangthouBuMinte: List<LaisiangthouBu>
    private lateinit var adapter: LaisiangthouBuAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouDatabase = LaisiangthouRepository(applicationContext)

        lifecycleScope.launch {
            delay(7000) // 3s delay before showing main UI
        }
        laisiangthouBuMinte = laisiangthouDatabase.getBooks()

        adapter = LaisiangthouBuAdapter( this)
        binding.recyclerViewBooks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBooks.adapter = adapter

        laisiangthouBuMinte.forEach { book ->
            book.chapters = laisiangthouDatabase.getChaptersForBook(book.id)
        }
        adapter.updateBooks(laisiangthouBuMinte)

        setupToolbar()
        setupDrawer()
        setupDrawerFooter()

        showDailyVerseIfNeeded()

        NotificationUtils().createNotificationChannel(this)
        NotificationSender(this).sendRandomVerseNotification()

        if (RootChecker.isDeviceRooted() && !BuildUtils.isEmulator()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Security Alert")
                .setMessage("Laisiangthou App cannot run on rooted devices.")
                .setCancelable(false)
                .setPositiveButton("Exit") { _, _ ->
                    finishAffinity()
                }
                .show()
            return
        }

        binding.searchEdit.doAfterTextChanged {
            filterBooks(it.toString())
        }

        binding.searchContainer.doOnLayout { card ->
            binding.recyclerViewBooks.updatePadding(
                bottom = card.height + 38.dpToPx()
            )
        }

        binding.searchEdit.doAfterTextChanged {

            val hasText = !it.isNullOrBlank()

            binding.clearCard.visibility =
                if (hasText) View.VISIBLE else View.GONE

            filterBooks(it.toString())
        }

        binding.clearButton.setOnClickListener {
            binding.searchEdit.text?.clear()
        }

    }
    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.main_menu, menu)
        // Setup search functionality
        //val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
       // val searchView = searchItem?.actionView as SearchView
       // searchView.queryHint = "Laibu minte zonbaihlamna"

        // Handle search query input
        //searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //override fun onQueryTextSubmit(query: String?): Boolean {
               // return false
           // }

           // override fun onQueryTextChange(newText: String?): Boolean {
               // filterBooks(newText.orEmpty())
                //return true
           // }
       // })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_search -> {
                val intent = Intent(this, MainSearchActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterBooks(query: String) {
        val filteredList = if (query.isBlank()) {
            laisiangthouBuMinte
        } else {
            laisiangthouBuMinte.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }

        adapter.clearExpansion()
        adapter.updateBooks(filteredList.toList())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.mainToolbar)
        binding.collapsingToolbar.apply {
            title = "Laisiangthou"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }
    }

    private fun setupDrawer() {
        drawerLayout = binding.drawerLayout
        val navigationView = binding.navigationView
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.mainToolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        toggle.isDrawerIndicatorEnabled = true   // ✅ IMPORTANT
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.mainToolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ham)

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
               // R.id.search_view -> {
                  //  startActivity(Intent(this, MainSearchActivity::class.java))
               // }

                R.id.bi -> {
                    laisiangthouSource(SOURCE_BI)
                    LaisiangthouDatabaseManager.closeAll()
                    recreate()
                }

                R.id.bsi -> {
                    laisiangthouSource(SOURCE_BSI)
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

                    //val dialog = MaterialAlertDialogBuilder(this)
                    MaterialAlertDialogBuilder(this)
                        //MaterialAlertDialogBuilder(this)
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

    private fun laisiangthouSource(source: String) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit {
                putString(KEY_SOURCE, source)
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

    private fun showDailyVerseIfNeeded() {
        val prefs = getSharedPreferences("daily_verse_prefs", MODE_PRIVATE)
        val lastShownDate = prefs.getString("last_date", "")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastShownDate != today) {
            // Show popup
            showDailyVerseDialog()

            // Save today's date so we don't show again until tomorrow
            prefs.edit { putString("last_date", today) }
        }
    }

    private fun showDailyVerseDialog() {
        val laisiangthouRepo = LaisiangthouRepository(this)
        val randomVerse = laisiangthouRepo.getRandomVerse()
        val chapterToBookMap = laisiangthouRepo.createChapterToBookMap()
        val bookName = chapterToBookMap[randomVerse.chapterId] ?: "Unknown Book"
        val chapterNumber = laisiangthouRepo.getChapterNumberById(randomVerse.chapterId)

        val verseInfo = "$bookName $chapterNumber:${randomVerse.verseNumber}"
        val verseText = cleanText(randomVerse.text)

        // We add only one \n and then insert a small vertical gap
        val combinedText = "$verseInfo\n$verseText"

        val spannable = SpannableString(combinedText).apply {
            setSpan(
                RelativeSizeSpan(1.2f), // 20% bigger
                0,
                verseInfo.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                verseInfo.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Add a smaller space (e.g., 8px) after verseInfo
            setSpan(
                object : LineHeightSpan {
                    override fun chooseHeight(
                        text: CharSequence?,
                        start: Int,
                        end: Int,
                        spanstartv: Int,
                        v: Int,
                        fm: Paint.FontMetricsInt,
                    ) {
                        fm.descent += 10 // small gap in px
                        fm.ascent -= 0
                    }
                },
                verseInfo.length,
                verseInfo.length + 1, // only the \n
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val titleView = TextView(this).apply {
            text = context.getString(R.string.tuni_thupi)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, Typeface.BOLD)
            // setTextColor(ContextCompat.getColor(context, android.R.color.black))
            // setShadowLayer(4f, 0f, 2f, Color.GRAY) // elevation shadow
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 16)
        }

        MaterialAlertDialogBuilder(this)
            .setCustomTitle(titleView)
            .setMessage(spannable)
            .setPositiveButton("OK", null)
            .show()
    }
    private fun cleanText(verseText: String): String {
        val lines = verseText.split("\n")

        // Clean up each line by trimming spaces and replacing multiple spaces with a single one
        return lines.joinToString("\n") {
            it.trim().replace("\\s+".toRegex(), " ")  // Replace multiple spaces with a single space
                .replace("\n\n", "") // Replace double newlines with a single space
                .replace("\\n", "\n")   // Replace any remaining single newline with a space
                .replace("\\s+".toRegex(), " ") // Replace multiple spaces with a single space
        }
    }
}