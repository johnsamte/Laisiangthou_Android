package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.KEY_SOURCE
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.PREF_NAME
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BI
import com.johnsamte.pathianlaisiangthou.LaisiangthouDatabaseManager.SOURCE_BSI
import com.johnsamte.pathianlaisiangthou.databinding.ActivityMainBinding
import com.johnsamte.pathianlaisiangthou.databinding.NavHeaderBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var laisiangthouDatabase: LaisiangthouRepository
    private lateinit var laisiangthouBuMinte: List<LaisiangthouBu>
    private lateinit var adapter: LaisiangthouBuAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        laisiangthouDatabase = LaisiangthouRepository(applicationContext)
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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.main_menu, menu)
        // Setup search functionality
        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Laibu minte zonbaihlamna"

        // Handle search query input
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBooks(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterBooks(query: String) {
        val currentBooks = laisiangthouBuMinte

        // Filter the list of books based on the search query
        val filteredList = if (query.isEmpty()) {
            currentBooks
        } else {
            currentBooks.filter { it.name.contains(query, ignoreCase = true) }
        }

        adapter.updateBooks(filteredList)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.mainToolbar)

        // Set the title (this will make it available for styling)
        //binding.collapsingToolbar.title = "Laisiangthou"
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
                R.id.bookmarkte -> {
                    startActivity(Intent(this, BookmarksActivity::class.java))
                }
                R.id.notete -> {
                    startActivity(Intent(this, NotesActivity::class.java))
                }
                R.id.search_view -> {
                    startActivity(Intent(this, MainSearchActivity::class.java))
                }

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
                   // UpdateManager.checkForUpdate(this, showUpToDateDialog = true)

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
}