package com.johnsamte.pathianlaisiangthou

data class Note(
    val id: Int,
    val chapterId: Int,
    val chapterNumber: Int,
    val referenceKey: String,
    val noteText: String,
    val createdAt: String,
    val updatedAt: String,
    val bookName: String
)

