package com.johnsamte.pathianlaisiangthou

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class LaisiangthouRepository (private val context: Context) {
        private fun db(): SQLiteDatabase {
            return LaisiangthouDatabaseManager.getDatabase(context)
        }

        fun getBooks(): List<LaisiangthouBu> {
            //val db = openDatabase()  // Ensure the database is opened
            val db = LaisiangthouDatabaseManager.getDatabase(context)  // Use optimized singleton DB
            val books = mutableListOf<LaisiangthouBu>()

            val cursor = db.rawQuery("SELECT * FROM Books", null)
            if (cursor.moveToFirst()) {
                do {
                    val bookId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val bookName = cursor.getString(cursor.getColumnIndexOrThrow("name"))


                    // Fetch chapters for the current book
                    val chapters = getChaptersForBook(bookId)

                    // Create the book object with its chapters
                    val book = LaisiangthouBu(
                        id = bookId,
                        name = bookName,
                        chapters = chapters
                    )
                    books.add(book)
                } while (cursor.moveToNext())
            }
            cursor.close()

            return books
        }

        // Function to create a map of chapter ID to book name
        fun createChapterToBookMap(): Map<Int, String> {
            val books = getBooks()
            val chapterToBookMap = mutableMapOf<Int, String>()

            for (book in books) {
                for (chapter in book.chapters) {
                    chapterToBookMap[chapter.id] = book.name // Assuming chapter.id corresponds to chapterId
                }
            }
            return chapterToBookMap
        }

        fun getChaptersForBook(bookId: Int): List<Chapter> {
            val chapters = mutableListOf<Chapter>()
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            //val db = openDatabase()  // Ensure the database is opened
            val cursor = db.rawQuery("SELECT * FROM Chapters WHERE book_id = ?", arrayOf(bookId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val chapterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val chapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_number"))
                    val chapter = Chapter(id = chapterId, bookId = bookId, chapterNumber = chapterNumber)
                    chapters.add(chapter)
                } while (cursor.moveToNext())
            }
            cursor.close()

            return chapters
        }

        fun getVerseNumbers(chapterId: Int): List<Int> {
            val verseNumbers = mutableListOf<Int>()
            val db = LaisiangthouDatabaseManager.getDatabase(context)

            // Query to fetch verse numbers for the given chapter
            val query = "SELECT verse_number FROM verses WHERE chapter_id = ?"
            val cursor = db.rawQuery(query, arrayOf(chapterId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    // Get the verse number
                    val verseNumber = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
                    verseNumbers.add(verseNumber)
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()

            return verseNumbers
        }

        fun getVersesForChapter(chapterId: Int): List<Verses> {
            val verses = mutableListOf<Verses>()
            val db = LaisiangthouDatabaseManager.getDatabase(context)

            // Step 2: Fetch verses for the chapter from the verses table
            val query = "SELECT * FROM verses WHERE chapter_id = ?"
            val cursor = db.rawQuery(query, arrayOf(chapterId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val verseNumber = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
                    val maintitle = cursor.getString(cursor.getColumnIndexOrThrow("maintitle"))
                    val minititle = cursor.getString(cursor.getColumnIndexOrThrow("minititle"))
                    val thupi = cursor.getString(cursor.getColumnIndexOrThrow("thupi"))
                    val maintitle1 = cursor.getString(cursor.getColumnIndexOrThrow("maintitle1"))
                    val minititle1 = cursor.getString(cursor.getColumnIndexOrThrow("minititle1"))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                    val title1 = cursor.getString(cursor.getColumnIndexOrThrow("title_1"))
                    val title0 = cursor.getString(cursor.getColumnIndexOrThrow("title_0"))
                    val title2 = cursor.getString(cursor.getColumnIndexOrThrow("title_2"))
                    val title3 = cursor.getString(cursor.getColumnIndexOrThrow("title_3"))
                    val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                    val bottomText = cursor.getString(cursor.getColumnIndexOrThrow("bottom_text"))


                    val verse = Verses(id, chapterId, verseNumber, maintitle, minititle, thupi, maintitle1, minititle1, title, title1,title0, title2, title3, text, bottomText)
                    verses.add(verse)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()

            // Return the list of verses along with the chapterNumber
            return verses
        }

        fun getRandomVerse(): Verses {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT * FROM verses ORDER BY RANDOM() LIMIT 1"
            val cursor = db.rawQuery(query, null)

            var verse: Verses? = null
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val chapterId = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_id"))
                val verseNumber = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
                val maintitle = cursor.getString(cursor.getColumnIndexOrThrow("maintitle"))
                val minititle = cursor.getString(cursor.getColumnIndexOrThrow("minititle"))
                val thupi = cursor.getString(cursor.getColumnIndexOrThrow("thupi"))
                val maintitle1 = cursor.getString(cursor.getColumnIndexOrThrow("maintitle1"))
                val minititle1 = cursor.getString(cursor.getColumnIndexOrThrow("minititle1"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val title1 = cursor.getString(cursor.getColumnIndexOrThrow("title_1"))
                val title0 = cursor.getString(cursor.getColumnIndexOrThrow("title_0"))
                val title2 = cursor.getString(cursor.getColumnIndexOrThrow("title_2"))
                val title3 = cursor.getString(cursor.getColumnIndexOrThrow("title_3"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                val bottomText = cursor.getString(cursor.getColumnIndexOrThrow("bottom_text"))



                verse = Verses(id, chapterId, verseNumber,maintitle, minititle, thupi, maintitle1, minititle1, title, title1, title0, title2, title3, text, bottomText)
            }
            cursor.close()

            return verse ?: throw IllegalStateException("No verses available")
        }

        fun getBookIdFromChapterId(chapterId: Int): Int {

            val query = "SELECT book_id FROM Chapters WHERE id = ?"
            val cursor = db().rawQuery(query, arrayOf(chapterId.toString()))

            var bookId = -1  // Default value if not found
            if (cursor.moveToFirst()) {
                bookId = cursor.getInt(cursor.getColumnIndexOrThrow("book_id"))
            }
            cursor.close()

            return bookId
        }

    fun saveNote(
        chapterId: Int,
        referenceKey: String,
        noteText: String,
        noteColor: Int
    ): Boolean {

        val db = LaisiangthouDatabaseManager.getDatabase(context)
        val currentTime = System.currentTimeMillis()

        val cleanKey = referenceKey.trim()

        // 1️⃣ Try UPDATE first
        val updateValues = ContentValues().apply {
            put("note_text", noteText)
            put("updated_at", currentTime)
            put("note_color", noteColor)
        }

        val rowsUpdated = db.update(
            "Notes",
            updateValues,
            "chapter_id = ? AND reference_key = ?",
            arrayOf(chapterId.toString(), cleanKey)
        )

        if (rowsUpdated > 0) {
            return true // Updated existing note
        }

        // 2️⃣ If nothing updated, try INSERT
        return try {
            val insertValues = ContentValues().apply {
                put("chapter_id", chapterId)
                put("reference_key", cleanKey)
                put("note_text", noteText)
                put("created_at", currentTime)
                put("updated_at", currentTime)
                put("note_color", noteColor)
            }

            db.insertOrThrow("Notes", null, insertValues)
            true
        } catch (e: SQLiteConstraintException) {
            // Another thread inserted at same time
            false
        }
    }
    fun saveBookmark(
        chapterId: Int,
        referenceKey: String,
        noteText: String
    ): Boolean {

        val db = LaisiangthouDatabaseManager.getDatabase(context)
        val currentTime = System.currentTimeMillis()

        val cleanKey = referenceKey.trim()

        // 1️⃣ Try UPDATE first
        val updateValues = ContentValues().apply {
            put("bookmark_text", noteText)
            put("updated_at", currentTime)
        }

        val rowsUpdated = db.update(
            "Bookmarks",
            updateValues,
            "chapter_id = ? AND reference_key = ?",
            arrayOf(chapterId.toString(), cleanKey)
        )

        if (rowsUpdated > 0) {
            return true // Updated existing note
        }

        // 2️⃣ If nothing updated, try INSERT
        return try {
            val insertValues = ContentValues().apply {
                put("chapter_id", chapterId)
                put("reference_key", cleanKey)
                put("bookmark_text", noteText)
                put("created_at", currentTime)
                put("updated_at", currentTime)

            }

            db.insertOrThrow("Bookmarks", null, insertValues)
            true
        } catch (e: SQLiteConstraintException) {
            // Another thread inserted at same time
            false
        }
    }


   // fun addNote(note: Note): Long {
       // val db = LaisiangthouDatabaseManager.getDatabase(context)
       // val currentTimestamp = System.currentTimeMillis().toString()

        //val contentValues = ContentValues().apply {
          //  put("chapter_id", note.chapterId)
          //  put("reference_key", note.referenceKey)
           // put("note_text", note.noteText)
           // put("created_at", currentTimestamp)
           // put("updated_at", currentTimestamp)
       // }

       // return db.insert("Notes", null, contentValues)
   // }

    fun getAllNotes(): List<Note> {
        val db = LaisiangthouDatabaseManager.getDatabase(context)
        val notes = mutableListOf<Note>()

        val query = """
        SELECT 
            Notes.id,
            Notes.chapter_id,
            Notes.reference_key,
            Notes.note_text,
            Notes.created_at,
            Notes.updated_at,
            Notes.note_color,
            
            Chapters.chapter_number,
            Books.name AS book_name

        FROM Notes
        INNER JOIN Chapters ON Notes.chapter_id = Chapters.id
        INNER JOIN Books ON Chapters.book_id = Books.id
        
        
        ORDER BY Notes.created_at DESC
    """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val note = Note(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    chapterId = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_id")),
                    chapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_number")),
                    referenceKey = cursor.getString(cursor.getColumnIndexOrThrow("reference_key")),
                    noteText = cursor.getString(cursor.getColumnIndexOrThrow("note_text")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                    bookName = cursor.getString(cursor.getColumnIndexOrThrow("book_name")),
                    noteColor = cursor.getInt(cursor.getColumnIndexOrThrow("note_color"))

                )

                notes.add(note)

            } while (cursor.moveToNext())

        }

        cursor.close()
        return notes
    }

       // fun updateNote(note: Note): Int {
           // val db = LaisiangthouDatabaseManager.getDatabase(context)
           // val currentTimestamp = System.currentTimeMillis().toString() // or formatted date
           // val contentValues = ContentValues().apply {
              //  put("note_text", note.noteText)
              //  put("updated_at", currentTimestamp)
           // }

           // return db.update(
              //  "Notes",
              //  contentValues,
               // "id = ?",
               // arrayOf(note.id.toString())
            //)
       // }

    fun updateNote(
        bookId: Int,
        chapterNumber: Int,
        verseNumber: Int,
        noteText: String,
        updatedAt: String
    ): Int {

        val db = LaisiangthouDatabaseManager.getDatabase(context)
        val values = ContentValues().apply {
            put("noteText", noteText)
            put("updatedAt", updatedAt)
        }

        return db.update(
            "notes",
            values,
            "bookId=? AND chapterNumber=? AND verseNumber=?",
            arrayOf(
                bookId.toString(),
                chapterNumber.toString(),
                verseNumber.toString()
            )
        )
    }

        fun deleteNote(noteId: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            return db.delete(
                "Notes",
                "id = ?",
                arrayOf(noteId.toString())
            )
        }

        fun isNoteAlreadyExists(bookId: Int, chapter: Int, verseNumber: Int): Boolean {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            db.rawQuery(
                "SELECT COUNT(*) FROM Notes WHERE book_id = ? AND chapter_number = ? AND verse_number = ?",
                arrayOf(bookId.toString(), chapter.toString(), verseNumber.toString())
            ).use { cursor ->
                return cursor.moveToFirst() && cursor.getInt(0) > 0
            }
        }
    fun getNotedVerseNumbers(chapterId: Int): Set<Int> {
        val db = LaisiangthouDatabaseManager.getDatabase(context)
        val notedSet = mutableSetOf<Int>()

        val cursor = db.rawQuery(
            "SELECT reference_key FROM Notes WHERE chapter_id = ?",
            arrayOf(chapterId.toString())
        )

        while (cursor.moveToNext()) {
            val referenceKey =
                cursor.getString(cursor.getColumnIndexOrThrow("reference_key"))

            notedSet.addAll(parseReferenceKey(referenceKey))
        }

        cursor.close()
        return notedSet
    }

    private fun parseReferenceKey(referenceKey: String): Set<Int> {
        val result = mutableSetOf<Int>()

        val parts = referenceKey.split(",")

        for (part in parts) {
            if (part.contains("-")) {
                val range = part.split("-")
                val start = range[0].toIntOrNull()
                val end = range[1].toIntOrNull()

                if (start != null && end != null) {
                    for (i in start..end) {
                        result.add(i)
                    }
                }
            } else {
                part.toIntOrNull()?.let { result.add(it) }
            }
        }

        return result
    }

       // fun getNotedVerseNumbers(bookId: Int, chapter: Int): Set<Int> {
           // val db = LaisiangthouDatabaseManager.getDatabase(context)
           // val notedSet = mutableSetOf<Int>()

            //val cursor = db.rawQuery(
              //  "SELECT verse_number FROM Notes WHERE book_id = ? AND chapter_number = ?",
               // arrayOf(bookId.toString(), chapter.toString())
            //)

            //while (cursor.moveToNext()) {
             //   val verseNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
              //  if (verseNum > 0) notedSet.add(verseNum)
           // }

           // cursor.close()
           // return notedSet
       // }

        fun addBookmark(bookmark: Bookmark): Long {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val currentTimestamp = System.currentTimeMillis().toString() // Or use formatted date
            val contentValues = ContentValues().apply {
                put("book_id", bookmark.bookId)
                put("chapter_number", bookmark.chapterNumber)
                put("verse_number", bookmark.verseNumber)
                put("bookmark_text", bookmark.bookmarkText)
                put("created_at", currentTimestamp)
            }
            return db.insert("Bookmarks", null, contentValues)
        }

        fun getAllBookmarks(): List<Bookmark> {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val bookmarks = mutableListOf<Bookmark>()

            val query = """
        SELECT Bookmarks.id, Bookmarks.book_id, Books.name as book_name, 
       Bookmarks.chapter_number, Bookmarks.verse_number, 
       Bookmarks.bookmark_text, Bookmarks.created_at 
        FROM Bookmarks 
        INNER JOIN Books ON Bookmarks.book_id = Books.id 
        ORDER BY Bookmarks.created_at DESC

    """

            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                do {
                    val bookmark = Bookmark(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        bookId = cursor.getInt(cursor.getColumnIndexOrThrow("book_id")),
                        chapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_number")),
                        verseNumber = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number")),
                        bookmarkText = cursor.getString(cursor.getColumnIndexOrThrow("bookmark_text"))?:"",
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        bookName = cursor.getString(cursor.getColumnIndexOrThrow("book_name"))
                    )
                    bookmarks.add(bookmark)
                } while (cursor.moveToNext())
            }

            cursor.close()
            return bookmarks
        }

        fun deleteBookmark(bookmarkId: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            return db.delete(
                "Bookmarks",
                "id = ?",
                arrayOf(bookmarkId.toString())
            )
        }

        fun isBookmarkAlreadyExists(bookId: Int, chapter: Int, verseNumber: Int): Boolean {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            db.rawQuery(
                "SELECT COUNT(*) FROM Bookmarks WHERE book_id = ? AND chapter_number = ? AND verse_number = ?",
                arrayOf(bookId.toString(), chapter.toString(), verseNumber.toString())
            ).use { cursor ->
                return cursor.moveToFirst() && cursor.getInt(0) > 0
            }
        }

        fun getBookmarkedVerseNumbers(bookId: Int, chapter: Int): Set<Int> {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val bookmarkedSet = mutableSetOf<Int>()

            val cursor = db.rawQuery(
                "SELECT verse_number FROM Bookmarks WHERE book_id = ? AND chapter_number = ?",
                arrayOf(bookId.toString(), chapter.toString())
            )

            while (cursor.moveToNext()) {
                val verseNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
                if (verseNum > 0) bookmarkedSet.add(verseNum)
            }

            cursor.close()
            return bookmarkedSet
        }

        fun getChapterIdForBookAndChapter(bookId: Int, chapterNumber: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = """
        SELECT v.chapter_id 
        FROM verses v 
        JOIN chapters c ON v.chapter_id = c.id 
        WHERE c.book_id = ? AND c.chapter_number = ?
    """
            val result = db.rawQuery(query, arrayOf(bookId.toString(), chapterNumber.toString()))

            if (result.moveToFirst()) {
                val chapterId = result.getInt(0)
                result.close()
                return chapterId
            }
            result.close()
            return -1
        }

        fun getChapterId(bookId: Int, chapterNumber: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            var chapterId = -1

            // Correct column names: book_id and chapter_number
            val query = """
        SELECT id FROM Chapters
        WHERE book_id = ? AND chapter_number = ?
    """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(bookId.toString(), chapterNumber.toString()))

            if (cursor.moveToFirst()) {
                chapterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            }

            cursor.close()
            db.close()  // Don't forget to close the database
            return chapterId
        }

        fun getPreviousBookId(currentBookId: Int): Int? {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT id FROM Books WHERE id < ? ORDER BY id DESC LIMIT 1"
            val cursor = db.rawQuery(query, arrayOf(currentBookId.toString()))

            return if (cursor.moveToFirst()) {
                cursor.getInt(0) // Return the previous book ID
            } else {
                null // No previous book found
            }.also {
                cursor.close() // Always close the cursor after use
            }
        }

        fun getNextBookId(currentBookId: Int): Int? {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT id FROM Books WHERE id > ? AND name != 'Mapte' ORDER BY id ASC LIMIT 1"
            val cursor = db.rawQuery(query, arrayOf(currentBookId.toString()))

            return if (cursor.moveToFirst()) {
                val nextBookId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                cursor.close()
                nextBookId
            } else {
                cursor.close()
                null // No next book found
            }
        }

        fun getLastChapterNumberForBook(bookId: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT MAX(chapter_number) AS lastChapter FROM Chapters WHERE book_id = ?"
            val cursor = db.rawQuery(query, arrayOf(bookId.toString()))

            return if (cursor.moveToFirst()) {
                val lastChapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow("lastChapter"))
                cursor.close()
                lastChapterNumber
            } else {
                cursor.close()
                1 // Default to 1 if no chapters are found
            }
        }

        fun getBookName(bookId: Int): String {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT name FROM Books WHERE id = ?"
            val cursor = db.rawQuery(query, arrayOf(bookId.toString()))

            return if (cursor.moveToFirst()) {
                val bookName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                cursor.close()
                bookName
            } else {
                cursor.close()
                "Unknown Book" // Default value if the book name isn't found
            }
        }

        fun getBookNameById(bookId: Int): String {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val cursor = db.rawQuery(
                "SELECT name FROM Books WHERE id = ?",
                arrayOf(bookId.toString())
            )

            return cursor.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else {
                    ""
                }
            }
        }

        fun getTotalChaptersForBook(bookId: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT COUNT(*) AS totalChapters FROM chapters WHERE book_id = ?"
            val cursor = db.rawQuery(query, arrayOf(bookId.toString()))

            return if (cursor.moveToFirst()) {
                val totalChapters = cursor.getInt(cursor.getColumnIndexOrThrow("totalChapters"))
                cursor.close()
                totalChapters
            } else {
                cursor.close()
                0 // Return 0 if no chapters found for the book
            }
        }
        fun isFirstBook(bookId: Int): Boolean {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val query = "SELECT MIN(id) FROM books"
            val result = db.rawQuery(query, null)

            if (result.moveToFirst()) {
                val firstBookId = result.getInt(0)
                result.close()
                return bookId == firstBookId
            }
            result.close()
            return false
        }

        fun getChapterNumberById(chapterId: Int): Int {
            val db = LaisiangthouDatabaseManager.getDatabase(context)
            val cursor = db.rawQuery("SELECT chapter_number FROM Chapters WHERE id = ?", arrayOf(chapterId.toString()))

            var chapterNumber = -1
            if (cursor.moveToFirst()) {
                chapterNumber = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_number"))
            }
            cursor.close()
            return chapterNumber
        }

        fun searchInWholeBook(query: String, bookId: Int): List<SearchResult> {
            val verses = mutableListOf<SearchResult>()
            val db = LaisiangthouDatabaseManager.getDatabase(context)

            val cursor = db.rawQuery(
                """
        SELECT b.id AS book_id, b.name, c.chapter_number, v.verse_number, v.text, v.id
        FROM Verses v
        JOIN Chapters c ON v.chapter_id = c.id
        JOIN Books b ON c.book_id = b.id
        WHERE b.id = ? AND v.text LIKE ?
        """,
                arrayOf(bookId.toString(), "%$query%")
            )

            if (cursor.moveToFirst()) {
                do {
                    val bookId0 = cursor.getInt(cursor.getColumnIndexOrThrow("book_id"))
                    val bookName1 = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val chapterNumber2 = cursor.getInt(cursor.getColumnIndexOrThrow("chapter_number"))
                    val verseNumber3 = cursor.getInt(cursor.getColumnIndexOrThrow("verse_number"))
                    val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                    val id5 = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

                    verses.add(SearchResult(id5, bookId0, bookName1, chapterNumber2, verseNumber3, text))
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            return verses
        }

        fun getBookIdByName(bookName: String): Int {
            // val dbHelper = BibleDatabaseHelper(binding.root.context)
            val db = LaisiangthouDatabaseManager.getDatabase(context)

            // Query to get bookId from the Books table based on the bookName
            val cursor = db.query(
                "Books", // The table to query
                arrayOf("id"), // The columns to return
                "name = ?", // The WHERE clause
                arrayOf(bookName), // The argument for the WHERE clause
                null, // Grouping
                null, // Having
                null // Order
            )

            var bookId = -1 // Default to -1 if not found
            if (cursor.moveToFirst()) {
                bookId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            }

            cursor.close()
            db.close()
            return bookId
        }

        fun searchBiblePumpi(query: String): List<SearchResult> {
            if (query.isBlank()) return emptyList()

            val verses = mutableListOf<SearchResult>()
            val db = LaisiangthouDatabaseManager.getDatabase(context)

            val referenceRegex = Regex("""^([a-zA-Z]+)\s+(\d+):(\d+)$""")
            val match = referenceRegex.find(query.trim())

            if (match != null) {
                // --- Reference search ---
                val bookName = match.groupValues[1]
                val chapter = match.groupValues[2].toInt()
                val verse = match.groupValues[3].toInt()

                val cursor = db.rawQuery(
                    """
            SELECT b.id AS book_id, b.name, c.chapter_number, v.verse_number, v.text, v.id
            FROM Verses v
            JOIN Chapters c ON v.chapter_id = c.id
            JOIN Books b ON c.book_id = b.id
            WHERE b.name LIKE ? COLLATE NOCASE
              AND c.chapter_number = ?
              AND v.verse_number = ?
            """,
                    arrayOf("%$bookName%", chapter.toString(), verse.toString())
                )

                if (cursor.moveToFirst()) {
                    do {
                        val bookId = cursor.getInt(0)
                        val book = cursor.getString(1)
                        val chap = cursor.getInt(2)
                        val ver = cursor.getInt(3)
                        val text = cursor.getString(4)
                        val id = cursor.getInt(5)

                        verses.add(SearchResult(id, bookId, book, chap, ver, text))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            } else {
                // --- Normal text search ---
                val cursor = db.rawQuery(
                    """
            SELECT b.id AS book_id, b.name, c.chapter_number, v.verse_number, v.text, v.id
            FROM Verses v
            JOIN Chapters c ON v.chapter_id = c.id
            JOIN Books b ON c.book_id = b.id
            WHERE v.text LIKE ? COLLATE NOCASE
            """,
                    arrayOf("%$query%")
                )

                if (cursor.moveToFirst()) {
                    do {
                        val bookId = cursor.getInt(0)
                        val book = cursor.getString(1)
                        val chap = cursor.getInt(2)
                        val ver = cursor.getInt(3)
                        val text = cursor.getString(4)
                        val id = cursor.getInt(5)

                        verses.add(SearchResult(id, bookId, book, chap, ver, text))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return verses
        }
}