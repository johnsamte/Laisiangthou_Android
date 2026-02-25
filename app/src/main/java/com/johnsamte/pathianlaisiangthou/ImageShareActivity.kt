package com.johnsamte.pathianlaisiangthou

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.johnsamte.pathianlaisiangthou.databinding.ActivityImageShareBinding
import com.johnsamte.pathianlaisiangthou.databinding.FragmentImagesBinding
import com.larswerkman.holocolorpicker.ColorPicker
import java.io.File
import java.io.FileOutputStream


class ImageShareActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageShareBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var dX = 0f
    private var dY = 0f

    private var isBold = false
    private var isItalic = false

    private var colorPickerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupVerseTextView()
        loadVerseTextFromIntent()
        initializeGalleryLauncher()
        setDefaultImage()
        showImageGrid()

        // Set up ViewPager2 and TabLayout
        val adapterFragment = ImageShareFragAdapter(this)
        binding.viewPager.adapter = adapterFragment

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.image_share) // Replace with your image icon resource
                1 -> tab.setIcon(R.drawable.fonts_icons)  // Replace with your fonts icon resource
                2 -> tab.setIcon(R.drawable.fill_color_new) // Replace with your color picker icon resource
                3 -> tab.setIcon(R.drawable.baseline_text_fields_24) // Replace with your text customization icon resource
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showImageGrid()
                    1 -> handleFontsIcon()
                    2 -> theColorPicker()
                    3 -> theTextCustomisation()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> hideImageGrid()
                    1 -> hideFontsIcon()
                    2 -> hideTheColorPicker()
                    3 -> hideTheTextCustomisation()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }
    private fun hideImageGrid() {
        binding.theImageRecycler.visibility = View.GONE
    }
    private fun hideFontsIcon() {
        binding.fontsFrame.visibility = View.GONE
    }
    private fun hideTheColorPicker() {
        binding.colorPickerContainer.visibility = View.GONE
    }
    private fun hideTheTextCustomisation() {
        binding.textCustomisation.visibility = View.GONE }

    // Setup toolbar with back button and custom font
    private fun setupToolbar() {
        setSupportActionBar(binding.imageshareToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.imageshareToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

       // val savedTypeface = FontUtils.loadSavedFont(this)
        binding.imageshareToolbar.title = "Image Share"

    }

    private fun loadVerseTextFromIntent() {
        val fullVerseText = intent.getCharSequenceExtra("EXTRA_FULL_VERSE_TEXT") // Retrieve as CharSequence
        if (fullVerseText != null) {
            binding.verseTextViewImageShare.text = fullVerseText // Set directly to TextView
        } else {
            //binding.verseTextViewImageShare.text = "Text not available"
        }
    }


    // Set default image in ImageView
    private fun setDefaultImage() {
        binding.selectedImage.setImageResource(R.drawable.images_8)
    }


    // Initialize gallery launcher for image picking
    private fun initializeGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { binding.selectedImage.setImageURI(it) }
            }
        }
    }

    // Enable dragging for verse text view
    @SuppressLint("ClickableViewAccessibility")
    private fun setupVerseTextView() {
        binding.verseTextViewImageShare.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            true
        }
    }

    private fun showImageGrid() {
        // Clear any existing views in `theImageRecycler` to ensure only one `RecyclerView` is added
        binding.theImageRecycler.removeAllViews()

        val images = listOf(
            R.drawable.add_photos,
            R.drawable.images_1,
            R.drawable.images_2,
            R.drawable.images_3,
            R.drawable.images_4,
            R.drawable.images_5,
            R.drawable.images_6,
            R.drawable.images_7,
            R.drawable.images_8,
            R.drawable.images_9,
            R.drawable.images_10,
            R.drawable.images_11,
            R.drawable.images_12,
            R.drawable.images_13
        )

        // Calculate the number of columns for the GridLayoutManager
        val spanCount = calculateNoOfColumns()

        // Inflate the nested ImageShareRecyclerviewBinding
        val imageShareBinding = FragmentImagesBinding.inflate(layoutInflater)

        // Add the inflated view to the FrameLayout
        binding.theImageRecycler.addView(imageShareBinding.root)

        // Set up the RecyclerView in the included layout
        imageShareBinding.imageRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        imageShareBinding.imageRecyclerView.adapter = ImageShareAdapter(images) { imageResId ->
            if (imageResId == R.drawable.add_photos) {
                openGallery()
            } else {
                binding.selectedImage.setImageResource(imageResId)
            }
        }
        //binding.theImageRecycler.visibility = if (binding.theImageRecycler.visibility == View.GONE) View.VISIBLE else View.GONE
        // Ensure the RecyclerView is visible
        binding.theImageRecycler.visibility = View.VISIBLE
    }



    // Calculate number of columns based on screen width
    private fun calculateNoOfColumns(): Int {
        return 4
    }

    // Launch gallery for image selection
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.image_icon_share -> { handleShare(); true }
            //R.id.image_icon_download -> { handleDownload(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handle sharing the current image with text
    private fun handleShare() {
        val bitmap = getBitmapFromView(binding.imageContainer)
        if (bitmap != null) {
            val uri = saveImageToCache(bitmap)
            if (uri != null) {

                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    //putExtra(Intent.EXTRA_TEXT, "Verse of the Day")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(this, "Share Image"))
                }
            } else {
                showToast("Failed to save image for sharing")
            }
        } else {
            showToast("Failed to generate image")
        }
    }

    // Utility to get Bitmap from a View
    //private fun getBitmapFromView(view: View): Bitmap? {
        //if (view.width <= 0 || view.height <= 0) return null
        //return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
            //Canvas(this).apply {
                //view.draw(this)
            //}
        //}
    //}

    private fun getBitmapFromView(view: View): Bitmap? {
        // Ensure the view has valid dimensions
        if (view.width <= 0 || view.height <= 0) return null

        return try {
            // Create a bitmap with the same dimensions as the view
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                // Use a canvas to draw the view into the bitmap
                val canvas = Canvas(this)
                view.draw(canvas)
            }
        } catch (e: Exception) {
            null
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Save image to cache directory and return the Uri for sharing
    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(cacheDir, "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs() // Create the cache directory if it doesn't exist
            }

            val file = File(cachePath, "shared_image.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            // Get URI from FileProvider (ensure you have a FileProvider in your manifest)
            FileProvider.getUriForFile(this, "$packageName.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Handle font selection and apply chosen font to the TextView
    private fun handleFontsIcon() {
        // Toggle visibility of fonts_layout
        binding.fontsFrame.visibility =
            if (binding.fontsFrame.visibility == View.VISIBLE) View.GONE else View.VISIBLE

        // Access each font option using findViewById directly from fonts_layout
        val robotoMono = binding.fontsFrame.findViewById<TextView>(R.id.fontOptionRobotoMono)
        val loraItalic = binding.fontsFrame.findViewById<TextView>(R.id.fontOptionLoraItalic)
        val openSans = binding.fontsFrame.findViewById<TextView>(R.id.fontOptionOpenSans)
        val greatVibes = binding.fontsFrame.findViewById<TextView>(R.id.fontOptionGreatVibes)
        val baskerv = binding.fontsFrame.findViewById<TextView>(R.id.fontOptionBasker)

        // Set up listeners for each font option
        robotoMono.setOnClickListener { applySelectedFont("Roboto Mono") }
        loraItalic.setOnClickListener { applySelectedFont("Lora Italic") }
        openSans.setOnClickListener { applySelectedFont("Open Sans") }
        greatVibes.setOnClickListener { applySelectedFont("Great Vibes") }
        baskerv.setOnClickListener { applySelectedFont("Baskerv Villesc") }
    }

    private fun applySelectedFont(fontName: String) {
        // Find tick icons relative to fontsFrame
        val tickRoboto = binding.fontsFrame.findViewById<ImageView>(R.id.tickRoboto)
        val tickLoraItalic = binding.fontsFrame.findViewById<ImageView>(R.id.tickLoraItalic)
        val tickOpenSans = binding.fontsFrame.findViewById<ImageView>(R.id.tickOpenSans)
        val tickGreatVibes = binding.fontsFrame.findViewById<ImageView>(R.id.tickGreatVibes)
        val tickBasker = binding.fontsFrame.findViewById<ImageView>(R.id.tickBasker)

        // Hide all tick marks
        tickRoboto.visibility = View.GONE
        tickLoraItalic.visibility = View.GONE
        tickOpenSans.visibility = View.GONE
        tickGreatVibes.visibility = View.GONE
        tickBasker.visibility = View.GONE

        // Show the tick mark for the selected font
        when (fontName) {
            "Roboto Mono" -> tickRoboto.visibility = View.VISIBLE
            "Lora Italic" -> tickLoraItalic.visibility = View.VISIBLE
            "Open Sans" -> tickOpenSans.visibility = View.VISIBLE
            "Great Vibes" -> tickGreatVibes.visibility = View.VISIBLE
            "Baskerv Villesc" -> tickBasker.visibility = View.VISIBLE
        }

        // Apply the selected font to the TextView
        val textView = binding.verseTextViewImageShare // Replace with your actual TextView
        val typeface = when (fontName) {
            "Roboto Mono" -> ResourcesCompat.getFont(this, R.font.robotomono)
            "Lora Italic" -> ResourcesCompat.getFont(this, R.font.lora_italic)
            "Open Sans" -> ResourcesCompat.getFont(this, R.font.opensans)
            "Great Vibes" -> ResourcesCompat.getFont(this, R.font.greatvibes)
            "Baskerv Villesc" -> ResourcesCompat.getFont(this, R.font.baskervvillesc_regular)
            else -> null
        }
        textView.typeface = typeface
    }

    // Toggle visibility of color picker
    private fun theColorPicker() {

        if (!colorPickerInitialized) {
            val colorPickerLayout = layoutInflater.inflate(R.layout.fragment_color_picker, binding.colorPickerContainer, false)
            binding.colorPickerContainer.addView(colorPickerLayout)
            setupColorPicker(colorPickerLayout)
            colorPickerInitialized = true
        }
        binding.colorPickerContainer.visibility = if (binding.colorPickerContainer.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    // Setup color picker to update text color
    private fun setupColorPicker(view: View) {
        val colorPicker = view.findViewById<ColorPicker>(R.id.color_picker)
        colorPicker.addSVBar(view.findViewById(R.id.sv_bar))
        colorPicker.addOpacityBar(view.findViewById(R.id.opacity_bar))
        colorPicker.addSaturationBar(view.findViewById(R.id.saturation_bar))
        colorPicker.addValueBar(view.findViewById(R.id.value_bar))

        colorPicker.setOnColorChangedListener { color ->
            binding.verseTextViewImageShare.setTextColor(color)
        }
    }


    // Toggle visibility of font size options
    private fun theTextCustomisation() {
        binding.textCustomisation.apply {
            if (childCount == 0) {
                val textCustomisation = layoutInflater.inflate(R.layout.fragment_text_customisation, binding.textCustomisation, false)
                addView(textCustomisation)
                setupShadowOpacitySeekBar(textCustomisation)
                setupImageBlurSeekBar(textCustomisation)
                applyIncreaseDecreaseText(textCustomisation)
                applyLineSpacingAdjustments(textCustomisation)
                setupBoldFaceToggle(textCustomisation)
                setupItalicsToggle(textCustomisation)
                setupImageBrightnessSeekBar(textCustomisation)
                setupTextPaddingAdjustment(textCustomisation)
            }
            visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    // Set up shadow opacity seek bar to adjust verse shadow opacity
    private fun setupShadowOpacitySeekBar(view: View) {
        val shadowOpacitySeekBar = view.findViewById<SeekBar>(R.id.shadowOpacitySeekBar)

        shadowOpacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Set shadow color with adjustable opacity based on SeekBar progress
                val shadowColor = Color.argb(progress, Color.red(Color.BLACK), Color.green(Color.BLACK), Color.blue(Color.BLACK))

                // Increase the shadow area by adjusting the radius, dx, and dy values
                val shadowRadius = 8f + (progress / 8f) // Adjust radius based on progress
                val dx = 20f
                val dy = 10f

                binding.verseTextViewImageShare.setShadowLayer(shadowRadius, dx, dy, shadowColor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        shadowOpacitySeekBar.progress = 0
    }

    private fun setupImageBlurSeekBar(view: View) {
        val imageBlurSeekBar = view.findViewById<SeekBar>(R.id.imageBlurSeekbar)
        val selectedImage = binding.imageContainer // Now this points to the ImageView inside FrameLayout

        imageBlurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Scale progress to a suitable blur radius
                val blurRadius = progress / 10f

                // Apply blur effect to the ImageView based on API level
                applyConditionalBlur(selectedImage, blurRadius)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        imageBlurSeekBar.progress = 0
    }

    private fun setupImageBrightnessSeekBar(view: View) {
        val imageBrightnessSeekBar = view.findViewById<SeekBar>(R.id.imageBrightnessSeekbar)
        val selectedImage = binding.imageContainer // ImageView inside FrameLayout

        // Initialize SeekBar with a default brightness value
        imageBrightnessSeekBar.progress = 50 // Default progress (neutral brightness: 1.0f)
        imageBrightnessSeekBar.max = 100 // Progress range: 0 to 100

        // Set up SeekBar change listener
        imageBrightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Map progress (0-100) to brightness scale (0.1 to 2.0)
                val brightnessScale = 0.1f + (progress / 100f) * 1.9f

                // Apply the brightness adjustment
                applyConditionalBrightness(selectedImage, brightnessScale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun setupBoldFaceToggle(fragmentTextCustomisation: View) {
        val boldFaceImageView = fragmentTextCustomisation.findViewById<ImageView>(R.id.boldFace_share)
        val verseTextView = binding.verseTextViewImageShare

        boldFaceImageView.setOnClickListener {
            isBold = !isBold
            applyCombinedTextStyle(verseTextView)

            // Optionally change the icon to show bold is selected
            boldFaceImageView.setImageResource(
                if (isBold) R.drawable.bold_face_selected else R.drawable.bold_face
            )
        }
    }

    private fun setupItalicsToggle(fragmentTextCustomisation: View) {
        val italicFaceImageView = fragmentTextCustomisation.findViewById<ImageView>(R.id.italicFace_share)
        val verseTextView = binding.verseTextViewImageShare

        italicFaceImageView.setOnClickListener {
            isItalic = !isItalic
            applyCombinedTextStyle(verseTextView)

            // Optionally change the icon to show italic is selected
            italicFaceImageView.setImageResource(
                if (isItalic) R.drawable.italics_selected else R.drawable.italics
            )
        }
    }

    private fun applyCombinedTextStyle(textView: TextView) {
        textView.setTypeface(null, when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        })
    }

    private fun applyIncreaseDecreaseText(fragmentTextCustomisation: View) {
        val fontSizeSeekBar = fragmentTextCustomisation.findViewById<SeekBar>(R.id.shareIncreaseDecreaseText)

        // Reference the TextView
        val verseTextView = binding.verseTextViewImageShare

        // Set the maximum and minimum font size range
        val minFontSize = 10f
        val maxFontSize = 30f
        fontSizeSeekBar.max = (maxFontSize - minFontSize).toInt() // Maps range to SeekBar progress

        // Retrieve the current font size of verseTextViewImageShare in SP
        val currentFontSize = verseTextView.textSize / verseTextView.context.resources.displayMetrics.density

        // Calculate the initial SeekBar progress based on the current font size
        fontSizeSeekBar.progress = (currentFontSize - minFontSize).toInt()

        // Set up a listener for the SeekBar
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate the new font size
                val newFontSize = minFontSize + progress
                verseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, newFontSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }



    private fun applyLineSpacingAdjustments(fragmentTextCustomisation: View) {
        val lineSpacingSeekBar = fragmentTextCustomisation.findViewById<SeekBar>(R.id.share_lineSpace)

        // Set the SeekBar max to a range you want, e.g., 20 to cover line spacing from 1.0x to 3.0x
        lineSpacingSeekBar?.max = 20

        lineSpacingSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Map progress to line spacing multiplier (e.g., 1.0 to 3.0 range)
                val lineSpacingMultiplier = 1.0f + (progress / 10f) // progress 0 -> 1.0, progress 20 -> 3.0
                binding.verseTextViewImageShare.setLineSpacing(0f, lineSpacingMultiplier)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupTextPaddingAdjustment(fragmentTextCustomisation: View) {
        // Reference the SeekBar for padding adjustment
        val paddingSeekBar = fragmentTextCustomisation.findViewById<SeekBar>(R.id.share_lineExpand)

        // Reference the TextView whose padding will be adjusted
        val verseTextView = binding.verseTextViewImageShare

        // Initialize SeekBar properties
        paddingSeekBar.max = 100 // Adjust the max padding value as needed
        paddingSeekBar.progress = 0 // Default padding

        // Set SeekBar listener to adjust padding dynamically
        paddingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Apply start and end padding dynamically
                val paddingValue = progress * 2 // Multiply for finer control if needed
                verseTextView.setPadding(paddingValue, verseTextView.paddingTop, paddingValue, verseTextView.paddingBottom)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }





    // Function to apply blur based on API level
    private fun applyConditionalBlur(frameLayout: FrameLayout, radius: Float) {
        val blurBackgroundImageView = frameLayout.findViewById<ImageView>(R.id.selected_image)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use RenderEffect on API 31+
            val renderEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            blurBackgroundImageView.setRenderEffect(renderEffect)
        } else {
            // Fallback for lower API levels
            blurBackgroundImageView.drawable?.let { drawable ->
                val bitmap = drawable.toBitmap()
                val blurredBitmap = applyBlurMaskFilter(bitmap, radius)
                blurBackgroundImageView.setImageBitmap(blurredBitmap)
            }
        }
    }

    // Helper function to apply BlurMaskFilter on lower API levels
    private fun applyBlurMaskFilter(input: Bitmap, radius: Float): Bitmap {
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        val outputBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(input, 0f, 0f, paint)
        return outputBitmap
    }

    private fun applyConditionalBrightness(frameLayout: FrameLayout, brightness: Float) {
        val imageView = frameLayout.findViewById<ImageView>(R.id.selected_image)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use RenderEffect on API 29+ for brightness adjustment
            val colorMatrix = ColorMatrix()
            colorMatrix.setScale(brightness, brightness, brightness, 1f)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            imageView.colorFilter = colorFilter
        } else {
            // Fallback for lower API levels
            imageView.drawable?.let { drawable ->
                val bitmap = drawable.toBitmap()
                val adjustedBitmap = adjustBrightness(bitmap, brightness)
                imageView.setImageBitmap(adjustedBitmap)
            }
        }
    }

    // Helper function to adjust brightness for lower API levels
    private fun adjustBrightness(input: Bitmap, brightness: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            setScale(brightness, brightness, brightness, 1f)
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        val outputBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(input, 0f, 0f, paint)
        return outputBitmap
    }

}

