package com.johnsamte.pathianlaisiangthou

data class Verses(
    val id: Int,
    val chapterId : Int,
    val verseNumber: Int,
    var maintitle : String?,
    var minititle : String?,
    var thupi : String?,
    var maintitle1 : String?,
    var minititle1 : String?,
    val title: String?,
    val title1 : String?,
    val title0 : String?,
    val title2 : String?,
    val title3 : String?,
    val text: String,
    var bottom_text: String?,
    var color: Int? = null, // Add this property to store color
    var isSelected: Boolean = false, // Add a selected flag
    var isBookmarked: Boolean = false // default to false

)
