package com.johnsamte.pathianlaisiangthou

data class Bookmark(
    val id: Int = 0,
    val bookId: Int,
    val bookName: String, // Make sure this field exists
    val chapterNumber: Int,
    val verseNumber: Int,
    val bookmarkText: String,
    val createdAt: String,
    val updatedAt: String = "",
)
