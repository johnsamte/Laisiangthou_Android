package com.johnsamte.pathianlaisiangthou

data class SearchResult(
    val id: Int,
    val bookId: Int,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)
