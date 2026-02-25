package com.johnsamte.pathianlaisiangthou

data class BibleApiResponse(
    val reference: String,
    val verses: List<Verse>
)

data class Verse(
    val book_id: String,
    val book_name: String,
    val chapter: Int,
    val verse: Int,
    val text: String

)
