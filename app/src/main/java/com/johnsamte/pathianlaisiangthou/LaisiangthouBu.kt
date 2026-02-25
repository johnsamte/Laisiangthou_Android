package com.johnsamte.pathianlaisiangthou

data class LaisiangthouBu(
    val id: Int,
    val name: String,
    var chapters: List<Chapter> = emptyList(),  // Mutable list to hold chapters
    var isExpanded: Boolean = false  // Track if a book is expanded
)
