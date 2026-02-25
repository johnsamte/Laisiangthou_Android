package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.text.LineBreaker
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemVerseTextBinding

class VerseTextAdapter (
    private var verses: List<Verses> = emptyList(),  //List<Verses>,
    private val activity: VerseTextActivity,
    private val context: Context,
    private var textSize: Float = 18f,    // Default text size
    private var lineSpacing: Float = 1.0f, // Default line spacing

) : RecyclerView.Adapter<VerseTextAdapter.VerseViewHolder>(), Filterable {
    private var filteredVerses: List<Verses> = verses
    //private var highlightedVerse: Int = -1
    private var searchQuery: String? = null
    private val highlightedTextColor = Color.parseColor("#C6861D")
    private val searchHighlightColor = Color.parseColor("#ABC6861D")
    private val selectedVerseNumbers = SparseBooleanArray()
   // private val selectedVerseNumbersColors = SparseIntArray()
    private var typeface: Typeface? = null
    private val notedVerses = mutableSetOf<Int>()
    private val bookmarkedVerses = mutableSetOf<Int>()
    private val highlightedVerse = mutableSetOf<Int>()


    inner class VerseViewHolder(private val binding: ItemVerseTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            verses: List<Verses>,
            highlightedVerse: MutableSet<Int>
            //selectedVerseNumbersColors: SparseIntArray
        ) {
            val paragraphText = SpannableStringBuilder()
            var yPositionStart = -1
            val verseSpans = mutableListOf<Pair<Int, Int>>()

            // Apply justification for supported Android versions
            binding.verseText.breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
            binding.verseText.hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
            binding.verseText.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD


            verses.forEachIndexed { _, verse ->
                // Add title if present
                var titleAdded = false // Track if a title or title1 has been added

                if (!verse.maintitle.isNullOrEmpty()) {
                    val trimmedMaintitle = verse.maintitle!!.replace("\\n", "\n").trim()
                    if (trimmedMaintitle.isNotEmpty()) {
                        val maintitle = SpannableString(trimmedMaintitle)
                        maintitle.setSpan(StyleSpan(Typeface.BOLD), 0, trimmedMaintitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        maintitle.setSpan(RelativeSizeSpan(1.9f), 0, trimmedMaintitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        maintitle.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, trimmedMaintitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        if (titleAdded || paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from previous content
                        }
                        paragraphText.append(maintitle)
                        paragraphText.append("\n") // Only one newline after maintitle

                        titleAdded = true
                    }
                }

                if (!verse.minititle.isNullOrEmpty()) {
                    val trimmedMinititle = verse.minititle!!.replace("\\n", "\n").trim()
                    if (trimmedMinititle.isNotEmpty()) {
                        val minititle = SpannableString(trimmedMinititle)
                        minititle.setSpan(StyleSpan(Typeface.BOLD), 0, trimmedMinititle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        minititle.setSpan(RelativeSizeSpan(1.0f), 0, trimmedMinititle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        minititle.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, trimmedMinititle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        paragraphText.append(minititle)
                        paragraphText.append("\n") // Only one newline after minititle

                        // Add horizontal line
                        val line = SpannableString("─────────────────────────") // Straight line
                        line.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, line.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        paragraphText.append(line)
                        paragraphText.append("\n\n") // Newline after line
                    }
                }

                if (!verse.thupi.isNullOrEmpty()) {
                    val trimmedThupi = verse.thupi!!.replace("\\n", "\n").trim()
                    if (trimmedThupi.isNotEmpty()) {
                        val thupi = SpannableString(trimmedThupi)
                        thupi.setSpan(StyleSpan(Typeface.ITALIC), 0, trimmedThupi.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        thupi.setSpan(RelativeSizeSpan(1.0f), 0, trimmedThupi.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        thupi.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0, trimmedThupi.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        if (titleAdded || paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from previous content
                        }
                        paragraphText.append(thupi)
                        paragraphText.append("\n") // Only one newline after maintitle1

                        titleAdded = true
                    }
                }

                if (!verse.maintitle1.isNullOrEmpty()) {
                    val trimmedMaintitle1 = verse.maintitle1!!.replace("\\n", "\n").trim()
                    if (trimmedMaintitle1.isNotEmpty()) {
                        val maintitle1 = SpannableString(trimmedMaintitle1)
                        maintitle1.setSpan(StyleSpan(Typeface.BOLD), 0, trimmedMaintitle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        maintitle1.setSpan(RelativeSizeSpan(1.0f), 0, trimmedMaintitle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        maintitle1.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, trimmedMaintitle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        if (titleAdded || paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from previous content
                        }
                        paragraphText.append(maintitle1)
                        paragraphText.append("\n") // Only one newline after maintitle1

                        titleAdded = true
                    }
                }


                if (!verse.minititle1.isNullOrEmpty()) {
                    val trimmedMinititle1 = verse.minititle1!!.replace("\\n", "\n").trim()
                    if (trimmedMinititle1.isNotEmpty()) {
                        val minititle1 = SpannableString(trimmedMinititle1)
                        minititle1.setSpan(StyleSpan(Typeface.BOLD), 0, trimmedMinititle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        minititle1.setSpan(RelativeSizeSpan(1.9f), 0, trimmedMinititle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        minititle1.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, trimmedMinititle1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        paragraphText.append(minititle1)
                        paragraphText.append("\n") // Only one newline after minititle1

                        // Add horizontal line
                        val line = SpannableString("─────────────────────────") // Straight line
                        line.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, line.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        paragraphText.append(line)
                        paragraphText.append("\n\n") // Newline after line
                    }
                }

                verse.title?.let { titleText ->
                    if (titleText.isNotEmpty()) {
                        val formattedTitleText = verse.title.replace("\\n", "\n").trim() // Preserve internal newlines
                        val title = SpannableString(formattedTitleText) // Multi-line text
                        title.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            formattedTitleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title.setSpan(
                            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                            0,
                            formattedTitleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title.setSpan(
                            RelativeSizeSpan(1.1f),
                            0,
                            formattedTitleText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (paragraphText.isNotEmpty()) {
                            paragraphText.append("\n")
                        }
                        paragraphText.append(title)
                        titleAdded = true
                    }
                }

                // Add title1 if present, or manage newline if title1 is empty
                if (!verse.title1.isNullOrEmpty()) {
                    val trimmedTitle1Text =
                        verse.title1.trim() // Trim leading and trailing whitespace
                    if (trimmedTitle1Text.isNotEmpty()) {
                        val title1 = SpannableString(trimmedTitle1Text) // No newline here yet
                        title1.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            0,
                            trimmedTitle1Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title1.setSpan(
                            RelativeSizeSpan(0.9f), // Reduce font size
                            0,
                            trimmedTitle1Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title1.setSpan(
                            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Center the title1
                            0,
                            trimmedTitle1Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // If title was added, add only one newline to separate title and title1
                        if (titleAdded) {
                            paragraphText.append("\n")
                        } else if (paragraphText.isNotEmpty()) {
                            // If no title was added but paragraphText has content, add newline
                            paragraphText.append("\n")
                        }
                        paragraphText.append(title1)
                        //paragraphText.append("\n") // Add newline after title1
                        titleAdded = true // Mark that title1 is added
                    }
                }
                // Add title0 if present, or manage newline if title2 is empty
                if (!verse.title0.isNullOrEmpty()) {
                    val trimmedTitle0Text = verse.title0.replace("\\n", "\n").trim()
                    if (trimmedTitle0Text.isNotEmpty()) {
                        val title0 = SpannableString(trimmedTitle0Text) // Create SpannableString
                        title0.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            0,
                            trimmedTitle0Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title0.setSpan(
                            RelativeSizeSpan(1.0f), // Reduce font size
                            0,
                            trimmedTitle0Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title0.setSpan(
                            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Normal alignment for title2
                            0,
                            trimmedTitle0Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // Handle newlines to separate from other content
                        if (titleAdded) {
                            paragraphText.append("\n") // Separate from previous title
                        } else if (paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from existing paragraph text
                        }
                        paragraphText.append(title0) // Add title2
                        //paragraphText.append("\n") // Add newline after title2

                        titleAdded = true // Mark that title2 is added
                    }
                }

                // Add title2 if present, or manage newline if title2 is empty
                if (!verse.title2.isNullOrEmpty()) {
                    val trimmedTitle2Text = verse.title2.trim() // Trim leading and trailing whitespace
                    if (trimmedTitle2Text.isNotEmpty()) {
                        val title2 = SpannableString(trimmedTitle2Text) // Create SpannableString
                        title2.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            0,
                            trimmedTitle2Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title2.setSpan(
                            RelativeSizeSpan(1.0f), // Reduce font size
                            0,
                            trimmedTitle2Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title2.setSpan(
                            AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), // Normal alignment for title2
                            0,
                            trimmedTitle2Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // Handle newlines to separate from other content
                        if (titleAdded) {
                            paragraphText.append("\n") // Separate from previous title
                        } else if (paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from existing paragraph text
                        }
                        paragraphText.append(title2) // Add title2
                        //paragraphText.append("\n") // Add newline after title2

                        titleAdded = true // Mark that title2 is added
                    }
                }

                // Add title3 if present, or manage newline if title3 is empty
                if (!verse.title3.isNullOrEmpty()) {
                    val trimmedTitle3Text = verse.title3.replace("\\n", "\n").trim() // Trim leading and trailing whitespace
                    if (trimmedTitle3Text.isNotEmpty()) {
                        val title3 = SpannableString(trimmedTitle3Text) // Create SpannableString
                        title3.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            trimmedTitle3Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title3.setSpan(
                            RelativeSizeSpan(1.1f), // Reduce font size
                            0,
                            trimmedTitle3Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        title3.setSpan(
                            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Normal alignment for title2
                            0,
                            trimmedTitle3Text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // Handle newlines to separate from other content
                        if (titleAdded) {
                            paragraphText.append("\n") // Separate from previous title
                        } else if (paragraphText.isNotEmpty()) {
                            paragraphText.append("\n") // Separate from existing paragraph text
                        }
                        paragraphText.append(title3) // Add title2
                        paragraphText.append("\n") // Add newline after title2

                        titleAdded = true // Mark that title2 is added
                    }
                }
                else if (titleAdded) {
                    // Ensure a newline if a previous title was added
                    paragraphText.append("\n")
                }

                // Handle verse numbers and text
                val verseNumberText = "${verse.verseNumber} "
                val verseText = "${verse.text.replace("\\n", "\n")} "

                val start = paragraphText.length

                // Create a spannable for the verse number only
                val verseNumberSpannable = SpannableString(verseNumberText)

                // Set a smaller font size for the verse number
                verseNumberSpannable.setSpan(
                    RelativeSizeSpan(0.8f), // Makes the verse number 80% of the original size
                    0, verseNumberText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Make the verse number bold
                verseNumberSpannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, verseNumberText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Apply a custom span to adjust the baseline shift for a lower superscript
                verseNumberSpannable.setSpan(
                    object : SuperscriptSpan() {
                        override fun updateDrawState(textPaint: TextPaint) {
                            // Move the baseline slightly down for a "lower superscript" effect
                            textPaint.baselineShift += (textPaint.ascent() * 0.2).toInt() // Adjust as needed
                        }
                    },
                    0, verseNumberText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // Append the verse number and verse text to the paragraph
                paragraphText.append(verseNumberSpannable)


                // Icons to apply (in order)
                val iconsToApply = mutableListOf<Drawable>()

                if (bookmarkedVerses.contains(verse.verseNumber)) {
                    ContextCompat.getDrawable(context, R.drawable.icon_bookmarkverse)?.let { iconsToApply.add(it) }
                }
                if (notedVerses.contains(verse.verseNumber)) {
                    ContextCompat.getDrawable(context, R.drawable.icon_notes)?.let { iconsToApply.add(it) }
                }

                // Apply all icons before the verse text
                iconsToApply.forEach { drawable ->
                    val iconSize = (binding.verseText.textSize * 0.8).toInt()
                    drawable.setBounds(0, 0, iconSize, iconSize)

                    val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
                    val spannable = SpannableString(" ")
                    spannable.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    paragraphText.append(spannable)
                }


                paragraphText.append(verseText)

                // Append bottom text AFTER the main verse
                if (!verse.bottom_text.isNullOrEmpty()) {
                    val trimmedBottom = verse.bottom_text!!.replace("\\n", "\n").trim()
                    if (trimmedBottom.isNotEmpty()) {
                        val bottomSpannable = SpannableString(trimmedBottom)
                        bottomSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, trimmedBottom.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        bottomSpannable.setSpan(RelativeSizeSpan(0.9f), 0, trimmedBottom.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        bottomSpannable.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0, trimmedBottom.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        paragraphText.append("\n\n")
                        paragraphText.append(bottomSpannable)
                        paragraphText.append("\n\n\n")
                    }
                }

                val end = paragraphText.length

                verseSpans.add(Pair(start, end))

                // Apply styling for selected verses
                if (selectedVerseNumbers.get(verse.verseNumber)) {
                    paragraphText.setSpan(
                        StyleSpan(Typeface.BOLD_ITALIC),
                        start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // Highlight the specific verse if it's the one selected
                if (highlightedVerse.contains(verse.verseNumber)) {
                    paragraphText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    paragraphText.setSpan(
                        ForegroundColorSpan(highlightedTextColor),
                        start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    yPositionStart = start
                }

                // Search query highlight
                searchQuery?.takeIf { it.isNotEmpty() }?.let { query ->
                    val queryLowerCase = query.lowercase()
                    val verseTextLowerCase = verseText.lowercase()
                    var searchStart = verseTextLowerCase.indexOf(queryLowerCase)

                    while (searchStart >= 0) {
                        val searchEnd = searchStart + query.length
                        if (searchEnd <= verseText.length) {
                            paragraphText.setSpan(
                                BackgroundColorSpan(searchHighlightColor),
                                searchStart, searchEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        searchStart = verseTextLowerCase.indexOf(queryLowerCase, searchEnd)
                    }
                }
            }

            val italicWords = listOf("Selah", "Higgaion", "Pasal'", "Numei'", "Elihu in adawng hi:") // Add more words if needed
            val boldWords = listOf("Tunung Kumpipa Ding") // Words to bold
            val colorWords = listOf("*")

            val formattedText = applyStylesForKeywords(SpannableStringBuilder(paragraphText), italicWords, boldWords, colorWords)

            binding.verseText.text = formattedText
            binding.verseText.textSize = textSize
            binding.verseText.setLineSpacing(lineSpacing, 1.0f)


            // Handle verse touch to select/deselect and toggle bottom sheet
            binding.verseText.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val clickedVerse = getClickedVerse(event.x, event.y, verseSpans)
                    if (clickedVerse != null) {
                        toggleVerseSelection(clickedVerse)
                        view.performClick()  // Ensure accessibility services can trigger the click
                    }

                }
                return@setOnTouchListener true
            }

            if (yPositionStart != -1) {
                binding.verseText.post {
                    val layout = binding.verseText.layout
                    layout?.let {
                        val line = layout.getLineForOffset(yPositionStart)
                        val yOffset = layout.getLineTop(line) - binding.verseText.paddingTop

                        // Calculate adjustment to make the highlighted verse slightly above the middle
                        val recyclerView = binding.root.parent as? RecyclerView
                        val recyclerViewHeight = recyclerView?.height ?: 0
                        val middleOffset = recyclerViewHeight / 6 // Adjust to center or slightly above

                        recyclerView?.smoothScrollBy(0, yOffset - middleOffset)
                    }
                }
            }

        }


        private fun getClickedVerse(x: Float, y: Float, verseSpans: List<Pair<Int, Int>>): Int? {
            val layout = binding.verseText.layout ?: return null
            val line = layout.getLineForVertical(y.toInt())
            val offset = layout.getOffsetForHorizontal(line, x)

            verseSpans.forEachIndexed { index, span ->
                val verseText = verses[index].text.trimEnd()
                val adjustedSpan = Pair(span.first, span.first + verseText.length)
                if (offset in adjustedSpan.first until adjustedSpan.second) {
                    return verses[index].verseNumber
                }
            }
            return null
        }

        // Assuming chapterId is updated correctly when navigating between chapters.
        private fun toggleVerseSelection(verseNumber: Int) {
            // Find the verse with the specified verse number
            val selectedVerse = verses.find { it.verseNumber == verseNumber }

            // Check if the verse belongs to the current chapter, or any chapter the user navigates to (including next or previous)
            if (selectedVerse != null) {
                val isSelected = selectedVerseNumbers.get(verseNumber, false)

                // If the verse is not selected, select it
                if (!isSelected) {
                    selectedVerseNumbers.put(verseNumber, true)
                    showBottomSheetOptions()
                } else {
                    // If the verse is selected, unselect it
                    selectedVerseNumbers.delete(verseNumber)
                    if (selectedVerseNumbers.size() == 0) {
                        activity.hideBottomSheet()
                    }
                }
                // Refresh the current item to update the UI
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        //private fun showBottomSheetOptions() {
            // Filter selected verses to include only those from the current chapter
            //val selectedVersesText = verses
                //.filter { selectedVerseNumbers.get(it.verseNumber) }
                //.joinToString("\n") { "${it.verseNumber} ${it.text}" }

           // if (selectedVersesText.isNotEmpty()) {
               // val bookName = activity.intent.getStringExtra("BOOK_ID") ?: ""
               // val chapterNumber = activity.intent.getIntExtra("CHAPTER_NUMBER", -1)

                // Show the bottom sheet with the selected verses
               // activity.showBottomSheet(selectedVersesText, bookName, chapterNumber.toString())
           // }
        //}

        private fun showBottomSheetOptions() {

            // Get selected verse objects
            val selectedItems = verses
                .filter { selectedVerseNumbers.get(it.verseNumber) }

            if (selectedItems.isEmpty()) return

            // Build list of verse texts (e.g. "1 In the beginning...")
            val selectedVerseTexts = selectedItems.map {
                "${it.verseNumber} ${it.text}"
            }

            // Build list of verse numbers (e.g. [1,2,3])
            val selectedVerseNumbersList = selectedItems.map {
                it.verseNumber
            }

            val bookName = activity.intent.getStringExtra("BOOK_ID") ?: ""
            val chapterNumber = activity.intent.getIntExtra("CHAPTER_NUMBER", -1)

            // Send BOTH lists to activity
            activity.showBottomSheet(
                selectedVerseTexts,
                selectedVerseNumbersList,
                bookName,
                chapterNumber.toString()
            )
        }

        private fun applyStylesForKeywords(verseText: SpannableStringBuilder, italicWords: List<String>, boldWords: List<String>, colorWords: List<String>): SpannableStringBuilder {
            for (keyword in italicWords) {
                var start = verseText.indexOf(keyword)
                while (start >= 0) {
                    val end = start + keyword.length
                    verseText.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    start = verseText.indexOf(keyword, end) // Find next occurrence
                }
            }

            for (keyword in boldWords) {
                var start = verseText.indexOf(keyword)
                while (start >= 0) {
                    val end = start + keyword.length
                    verseText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    start = verseText.indexOf(keyword, end) // Find next occurrence
                }
            }
            for (keyword in colorWords) {
                var start = verseText.indexOf(keyword)
                while (start >= 0) {
                    val end = start + keyword.length
                    verseText.setSpan(
                        ForegroundColorSpan(Color.RED),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    start = verseText.indexOf(keyword, end)
                }
            }
            return verseText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val binding = ItemVerseTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.bind(filteredVerses, highlightedVerse)
    }

    override fun getItemCount(): Int = 1

    //fun setHighlightedVerse(verseNumber: Int) {
       // highlightedVerse = verseNumber
        //notifyDataSetChanged()  // Refresh the RecyclerView to show highlighted verse

        // Reset the highlighted verse after 3 seconds
        //Handler(Looper.getMainLooper()).postDelayed({
           // highlightedVerse = -1
           // notifyDataSetChanged()  // Refresh again to remove highlight
        //}, 5000)
    //}
        fun setHighlightedVerse(verseNumber: List<Int>) {

            highlightedVerse.clear()
            highlightedVerse.addAll(verseNumber)

            notifyDataSetChanged()

            // Auto remove highlight after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                highlightedVerse.clear()
                notifyDataSetChanged()
            }, 5000)
        }

    fun updateVerses(newVerses: List<Verses>) {
        val diffCallback = VersesDiffCallback(verses, newVerses)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        verses = newVerses
        filteredVerses = newVerses

        diffResult.dispatchUpdatesTo(this)
    }

    class VersesDiffCallback(private val oldList: List<Verses>, private val newList: List<Verses>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    // Filter logic with search highlighting
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchText = constraint.toString().lowercase()
                searchQuery = searchText

                filteredVerses = if (searchText.isEmpty()) {
                    verses
                } else {
                    verses.filter { verse ->
                        verse.text.lowercase().contains(searchText) ||
                                verse.title?.lowercase()?.contains(searchText) == true ||
                                verse.title1?.lowercase()?.contains(searchText) == true
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredVerses
                return filterResults
            }


            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val resultList = results?.values
                if (resultList is List<*>) {
                    filteredVerses = resultList as List<Verses>
                } else {
                    filteredVerses = emptyList()
                }
                notifyDataSetChanged()
            }

        }
    }

    fun clearSelectedVerses() {
        if (selectedVerseNumbers.size() > 0) {
            // Collect the affected verse numbers
            val affectedPositions = mutableListOf<Int>()
            for (i in 0 until selectedVerseNumbers.size()) {
                val verseNumber = selectedVerseNumbers.keyAt(i)
                val position = verses.indexOfFirst { it.verseNumber == verseNumber }
                if (position != -1) {
                    affectedPositions.add(position)
                }
            }
            // Clear the selected verses
            selectedVerseNumbers.clear()

            // Notify only the affected items to refresh their UI
            for (position in affectedPositions) {
                notifyItemChanged(position)
            }
        }
    }

    fun setNotedVerses(verseNumbers: Set<Int>) {
        notedVerses.clear()
        notedVerses.addAll(verseNumbers)
        notifyDataSetChanged()
    }
    fun addNotedVerses(verses: List<Int>) {
        notedVerses.addAll(verses)
        notifyDataSetChanged()
    }

    fun setBookmarkedVerses(verseNumbers: Set<Int>) {
        bookmarkedVerses.clear()
        bookmarkedVerses.addAll(verseNumbers)
        notifyDataSetChanged()
    }

    fun getSelectedVerses(): List<Int> {
        val selectedList = mutableListOf<Int>()

        for (i in 0 until selectedVerseNumbers.size()) {
            val verseNumber = selectedVerseNumbers.keyAt(i)
            selectedList.add(verseNumber)
        }

        return selectedList.sorted()
    }
    // Returns selected verses as Pair<verseNumber, verseText>
    fun getSelectedVersesWithText(): List<Pair<Int, String>> {
        val selectedList = mutableListOf<Pair<Int, String>>()
        for (i in 0 until selectedVerseNumbers.size()) {
            val verseNumber = selectedVerseNumbers.keyAt(i)
            val verse = verses.find { it.verseNumber == verseNumber }
            if (verse != null) {
                selectedList.add(verseNumber to verse.text)
            }
        }
        return selectedList.sortedBy { it.first }
    }

}