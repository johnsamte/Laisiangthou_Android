package com.johnsamte.pathianlaisiangthou

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.edit
import java.io.FileOutputStream

class LaisiangthouDatabaseHelper (
        private val context: Context,
        private val dbName: String,
    ) : SQLiteOpenHelper(context, dbName, null, 1) {

        companion object {
            private const val PREFS_NAME = "db_prefs"
        }

        private val dbPath = context.getDatabasePath(dbName).absolutePath
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        init {
            if (!prefs.getBoolean(dbName, false)) {
                copyDatabase()
                prefs.edit { putBoolean(dbName, true) }
            }
        }

        override fun onCreate(db: SQLiteDatabase) { }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

        private fun copyDatabase() {
            context.assets.open(dbName).use { input ->
                context.getDatabasePath(dbName).parentFile?.mkdirs()
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }

        fun getReadableDb(): SQLiteDatabase {
            val db = SQLiteDatabase.openDatabase(
                dbPath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            db.enableWriteAheadLogging()
            return db
        }
    }