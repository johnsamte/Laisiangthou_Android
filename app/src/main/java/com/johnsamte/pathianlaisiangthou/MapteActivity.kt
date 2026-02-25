package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.johnsamte.pathianlaisiangthou.databinding.ActivityMapteBinding
import java.io.File

class MapteActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMapteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        //val customTypeface = FontUtils.loadSavedFont(this) ?: Typeface.DEFAULT

        // List of image filenames (in assets folder) and their corresponding names
        val imageList = listOf(
            "image1.jpg" to "Abraham Khualzinna Gamte",
            "image2.jpg" to "Thukhun Lui Hun A Palestine",
            "image3.jpg" to "Thukhun Thak Hun A Palestine",
            "image4.jpg" to "Sawltak Pawl Khualzinna Gamte",
            "image5.jpg" to "Jerusalem Leh Akimvel",
            "image6.jpg" to "Jerusalem A Herod Tempul Kigeldan"
        )
        val columnWidthDp = 150f // Set this value as per your requirement
        val spanCount = calculateNoOfColumns(this, columnWidthDp)
        binding.mapteRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        // Set adapter with click handler
        // Adapter item click listener
        binding.mapteRecyclerView.adapter = MapteAdapter(this, imageList) { selectedImage ->
            // Load the image from assets or cache and open in the system photo viewer
            loadImageAndOpenInSystemViewer(selectedImage)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.mapteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the navigation click listener
        binding.mapteToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.collapsingToolbar.apply {
            title = "Mapte"
            setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        }
    }

    private fun loadImageAndOpenInSystemViewer(imageFileName: String) {
        // Copy the image from assets to cache directory
        val inputStream = assets.open(imageFileName)
        val file = File(cacheDir, imageFileName)
        file.outputStream().use { inputStream.copyTo(it) }

        // Create a URI for the file using FileProvider for secure sharing
        val imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)

        // Create an intent to view the image
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(imageUri, "image/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION // Grant permission to read the image URI
        }

        // Start the system image viewer
        startActivity(intent)
    }

    private fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return maxOf(1, (screenWidthDp / columnWidthDp).toInt()) // Ensure at least 1 column
    }


}