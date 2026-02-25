package com.johnsamte.pathianlaisiangthou

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.johnsamte.pathianlaisiangthou.databinding.ItemSearchBinding
import java.util.regex.Pattern

class SearchAdapter : ListAdapter<SearchResult, SearchAdapter.SearchViewHolder>(SearchResultDiffCallback()) {

    private var highlightedQuery: String = ""

    fun submitListWithQuery(newResults: List<SearchResult>, query: String) {
        highlightedQuery = query
        submitList(newResults)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val verse = getItem(position)
        holder.bind(verse, highlightedQuery)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VerseTextActivity::class.java).apply {
                putExtra("BOOK_ID", verse.bookId)
                putExtra("BOOK_NAME", verse.bookName)
                putExtra("CHAPTER_NUMBER", verse.chapterNumber)
                putExtra("VERSE_NUMBER", verse.verseNumber)
            }
            context.startActivity(intent)
        }
    }

    class SearchViewHolder( val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(verse: SearchResult, query: String) {
            val cleanedText = cleanText(verse.text)

            // Highlight the query in the verse text
            val highlightedText = if (query.isNotEmpty()) {
                highlightQuery(cleanedText, query)
            } else {
                SpannableString(cleanedText)
            }

            // Format the display to show Book Name, Chapter Number, and Verse Number
            val formattedText = "${verse.bookName} ${verse.chapterNumber}:${verse.verseNumber}"

            binding.bookNameTextView.text = formattedText
            binding.verseTextView.text = highlightedText


        }

         fun cleanText(verseText: String): String {
            return verseText
                .replace("\\n", " ")          // Replace literal \n strings
                .replace("\n\n", " ")         // Replace double newlines with a space
                .replace("\n", " ")           // Replace single newlines with a space
                .replace("\\s+".toRegex(), " ") // Replace multiple spaces with a single space
                .trim()                       // Trim leading and trailing spaces
        }

         fun highlightQuery(text: String, query: String): SpannableString {
            val spannable = SpannableString(text)
            val pattern = Pattern.compile("\\b${Pattern.quote(query)}\\b", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            val searchHighlightColor = Color.parseColor("#FFB400") // Custom highlight color

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                spannable.setSpan(
                    ForegroundColorSpan(searchHighlightColor), // Highlight color
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannable
        }

    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}



