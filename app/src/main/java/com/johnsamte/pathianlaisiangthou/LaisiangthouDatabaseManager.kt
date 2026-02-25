package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.database.sqlite.SQLiteDatabase

object LaisiangthouDatabaseManager {
    const val PREF_NAME = "laisiangthou_prefs"
    const val KEY_SOURCE = "bible_source"

    const val SOURCE_BSI = "BSI"
    const val SOURCE_BI = "BI"

    private val databaseMap = mutableMapOf<String, SQLiteDatabase>()

    fun getDatabase(context: Context): SQLiteDatabase {
        val source = getBibleSource(context) // BUTHAK / BULUI
        val dbName = getDatabaseNameForSource(source)

        val existingDb = databaseMap[dbName]
        if (existingDb != null && existingDb.isOpen) {
            return existingDb
        }

        val helper = LaisiangthouDatabaseHelper(context, dbName)
        val db = helper.getReadableDb()

        databaseMap[dbName] = db
        return db
    }

    fun closeAll() {
        databaseMap.values.forEach {
            if (it.isOpen) it.close()
        }
        databaseMap.clear()
    }

    fun getBibleSource(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SOURCE, SOURCE_BSI)!!
    }

    fun getDatabaseNameForSource(source: String): String {
        return when (source) {
            SOURCE_BI -> "bi-bible.db"
            SOURCE_BSI -> "bible.db"
            else -> "bible.db"
        }
    }
}