package az.sananhaji.quranoxu.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import az.sananhaji.quranoxu.data.model.Bookmark
import az.sananhaji.quranoxu.data.model.Surah
import az.sananhaji.quranoxu.data.model.UserNote
import az.sananhaji.quranoxu.domain.model.OverallProgressEntity
import org.json.JSONArray
import java.io.FileOutputStream

data class SupportedLanguage(
    val code: String,
    val displayName: String
)

class QuranDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, USER_DB_NAME, null, 2) {

    companion object {
        private const val QURAN_ASSET_NAME = "database/Quran"
        private const val QURAN_DB_NAME = "Quran.db"
        private const val USER_DB_NAME = "user_quran.db"

        private const val TABLE_BOOKMARKS = "bookmarks"
        private const val TABLE_NOTES = "notes"
        private const val TABLE_READ_VERSES = "read_verses"
        private const val TABLE_LAST_READ = "last_read"

        // Nüzul sırası (Chronological revelation order for Surahs 1 to 114)
        val REVELATION_ORDERS = intArrayOf(
            5, 87, 89, 92, 112, 55, 39, 88, 113, 51,
            52, 53, 96, 72, 54, 70, 50, 69, 44, 45,
            73, 103, 74, 102, 42, 47, 48, 49, 85, 84,
            57, 75, 90, 58, 43, 41, 56, 38, 59, 60,
            61, 62, 63, 64, 65, 66, 95, 111, 106, 34,
            67, 76, 23, 37, 97, 46, 94, 105, 101, 91,
            109, 110, 104, 108, 99, 107, 77, 2, 78, 79,
            71, 40, 3, 4, 31, 98, 33, 80, 81, 24,
            7, 82, 86, 83, 27, 36, 8, 68, 10, 35,
            26, 9, 11, 12, 28, 1, 25, 100, 93, 14,
            30, 16, 13, 32, 19, 29, 17, 15, 18, 114,
            6, 22, 20, 21
        )
    }

    init {
        copyQuranDatabaseIfNeeded()
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBookmarksTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_BOOKMARKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                surah_index INTEGER NOT NULL,
                verse_number INTEGER NOT NULL,
                surah_name TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                UNIQUE(surah_index, verse_number) ON CONFLICT REPLACE
            )
        """.trimIndent()

        val createNotesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_NOTES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                surah_index INTEGER NOT NULL,
                verse_number INTEGER NOT NULL,
                surah_name TEXT NOT NULL,
                note_text TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                UNIQUE(surah_index, verse_number) ON CONFLICT REPLACE
            )
        """.trimIndent()

        val createReadVersesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_READ_VERSES (
                surah_index INTEGER NOT NULL,
                verse_number INTEGER NOT NULL,
                timestamp INTEGER DEFAULT 0,
                PRIMARY KEY(surah_index, verse_number)
            )
        """.trimIndent()

        val createLastReadTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_LAST_READ (
                id INTEGER PRIMARY KEY DEFAULT 1,
                surah_index INTEGER NOT NULL,
                verse_number INTEGER NOT NULL,
                surah_name TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createBookmarksTable)
        db.execSQL(createNotesTable)
        db.execSQL(createReadVersesTable)
        db.execSQL(createLastReadTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    private fun copyQuranDatabaseIfNeeded() {
        val dbFile = context.getDatabasePath(QURAN_DB_NAME)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            try {
                context.assets.open(QURAN_ASSET_NAME).use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getQuranDatabase(): SQLiteDatabase {
        copyQuranDatabaseIfNeeded()
        val dbFile = context.getDatabasePath(QURAN_DB_NAME)
        return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun getSupportedLanguages(): List<SupportedLanguage> {
        val db = getQuranDatabase()
        val languages = mutableListOf<SupportedLanguage>()
        val cursor = db.rawQuery("PRAGMA table_info(Surah)", null)
        val columns = mutableSetOf<String>()
        cursor.use { c ->
            val nameCol = c.getColumnIndex("name")
            while (c.moveToNext()) {
                columns.add(c.getString(nameCol))
            }
        }
        db.close()

        if (columns.contains("verses_azerbaijani")) {
            languages.add(SupportedLanguage("azerbaijani", "Azərbaycanca"))
        }
        if (columns.contains("verses_latin")) {
            languages.add(SupportedLanguage("latin", "Latın (Transliterasiya)"))
        }
        return languages
    }

    private fun parseJsonArray(jsonString: String?): List<String> {
        if (jsonString.isNullOrEmpty()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i -> jsonArray.optString(i, "") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllSurahs(): List<Surah> {
        val surahs = mutableListOf<Surah>()
        val db = getQuranDatabase()

        val query = "SELECT * FROM Surah ORDER BY \"index\" ASC"
        val cursor = db.rawQuery(query, null)

        cursor.use { c ->
            val indexCol = c.getColumnIndex("index")
            val verseCountCol = c.getColumnIndex("verse_count")
            val nameArabicLatinCol = c.getColumnIndex("name_arabic_latin")
            val versesLatinCol = c.getColumnIndex("verses_latin")
            val versesAzerbaijaniCol = c.getColumnIndex("verses_azerbaijani")
            val versesArabicCol = c.getColumnIndex("verses_arabic")
            val nameAzeriCol = c.getColumnIndex("name_azeri")
            val nameAzeriTercumeCol = c.getColumnIndex("name_azeri_tercume")
            val placeAzeriCol = c.getColumnIndex("place_azeri")
            val placeEngCol = c.getColumnIndex("place_eng")
            val nameArabicCol = c.getColumnIndex("name_arabic_with_arabic_letter")

            while (c.moveToNext()) {
                val sIndex = c.getInt(indexCol)
                val surah = Surah(
                    index = sIndex,
                    verseCount = c.getInt(verseCountCol),
                    nameArabicLatin = c.getString(nameArabicLatinCol),
                    versesLatin = parseJsonArray(c.getString(versesLatinCol)),
                    versesAzerbaijani = parseJsonArray(c.getString(versesAzerbaijaniCol)),
                    versesArabic = parseJsonArray(c.getString(versesArabicCol)),
                    nameAzeri = c.getString(nameAzeriCol) ?: "",
                    nameAzeriTercume = c.getString(nameAzeriTercumeCol),
                    placeAzeri = c.getString(placeAzeriCol),
                    placeEng = c.getString(placeEngCol),
                    nameArabicWithArabicLetter = c.getString(nameArabicCol)
                )
                surahs.add(surah)
            }
        }
        db.close()
        return surahs
    }

    fun getSurahByIndex(index: Int): Surah? {
        val db = getQuranDatabase()
        var surah: Surah? = null
        val query = "SELECT * FROM Surah WHERE \"index\" = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(index.toString()))

        cursor.use { c ->
            if (c.moveToFirst()) {
                val indexCol = c.getColumnIndex("index")
                val verseCountCol = c.getColumnIndex("verse_count")
                val nameArabicLatinCol = c.getColumnIndex("name_arabic_latin")
                val versesLatinCol = c.getColumnIndex("verses_latin")
                val versesAzerbaijaniCol = c.getColumnIndex("verses_azerbaijani")
                val versesArabicCol = c.getColumnIndex("verses_arabic")
                val nameAzeriCol = c.getColumnIndex("name_azeri")
                val nameAzeriTercumeCol = c.getColumnIndex("name_azeri_tercume")
                val placeAzeriCol = c.getColumnIndex("place_azeri")
                val placeEngCol = c.getColumnIndex("place_eng")
                val nameArabicCol = c.getColumnIndex("name_arabic_with_arabic_letter")

                surah = Surah(
                    index = c.getInt(indexCol),
                    verseCount = c.getInt(verseCountCol),
                    nameArabicLatin = c.getString(nameArabicLatinCol),
                    versesLatin = parseJsonArray(c.getString(versesLatinCol)),
                    versesAzerbaijani = parseJsonArray(c.getString(versesAzerbaijaniCol)),
                    versesArabic = parseJsonArray(c.getString(versesArabicCol)),
                    nameAzeri = c.getString(nameAzeriCol) ?: "",
                    nameAzeriTercume = c.getString(nameAzeriTercumeCol),
                    placeAzeri = c.getString(placeAzeriCol),
                    placeEng = c.getString(placeEngCol),
                    nameArabicWithArabicLetter = c.getString(nameArabicCol)
                )
            }
        }
        db.close()
        return surah
    }

    // --- Bookmarks Management ---

    fun toggleBookmark(surahIndex: Int, verseNumber: Int, surahName: String): Boolean {
        val userDb = writableDatabase
        val isCurrentlyBookmarked = isBookmarked(surahIndex, verseNumber)

        if (isCurrentlyBookmarked) {
            userDb.delete(
                TABLE_BOOKMARKS,
                "surah_index = ? AND verse_number = ?",
                arrayOf(surahIndex.toString(), verseNumber.toString())
            )
            return false
        } else {
            val values = ContentValues().apply {
                put("surah_index", surahIndex)
                put("verse_number", verseNumber)
                put("surah_name", surahName)
                put("timestamp", System.currentTimeMillis())
            }
            userDb.insertWithOnConflict(TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            return true
        }
    }

    fun isBookmarked(surahIndex: Int, verseNumber: Int): Boolean {
        val userDb = readableDatabase
        val cursor = userDb.query(
            TABLE_BOOKMARKS,
            arrayOf("id"),
            "surah_index = ? AND verse_number = ?",
            arrayOf(surahIndex.toString(), verseNumber.toString()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllBookmarks(): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val userDb = readableDatabase
        val cursor = userDb.query(
            TABLE_BOOKMARKS,
            null, null, null, null, null,
            "timestamp DESC"
        )
        cursor.use { c ->
            val idCol = c.getColumnIndex("id")
            val surahIndexCol = c.getColumnIndex("surah_index")
            val verseNumberCol = c.getColumnIndex("verse_number")
            val surahNameCol = c.getColumnIndex("surah_name")
            val timestampCol = c.getColumnIndex("timestamp")

            while (c.moveToNext()) {
                bookmarks.add(
                    Bookmark(
                        id = c.getLong(idCol),
                        surahIndex = c.getInt(surahIndexCol),
                        verseNumber = c.getInt(verseNumberCol),
                        surahName = c.getString(surahNameCol),
                        timestamp = c.getLong(timestampCol)
                    )
                )
            }
        }
        return bookmarks
    }

    // --- Notes Management ---

    fun saveNote(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String) {
        val userDb = writableDatabase
        if (noteText.isBlank()) {
            deleteNote(surahIndex, verseNumber)
            return
        }
        val values = ContentValues().apply {
            put("surah_index", surahIndex)
            put("verse_number", verseNumber)
            put("surah_name", surahName)
            put("note_text", noteText.trim())
            put("timestamp", System.currentTimeMillis())
        }
        userDb.insertWithOnConflict(TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteNote(surahIndex: Int, verseNumber: Int) {
        val userDb = writableDatabase
        userDb.delete(
            TABLE_NOTES,
            "surah_index = ? AND verse_number = ?",
            arrayOf(surahIndex.toString(), verseNumber.toString())
        )
    }

    fun getNoteText(surahIndex: Int, verseNumber: Int): String? {
        val userDb = readableDatabase
        val cursor = userDb.query(
            TABLE_NOTES,
            arrayOf("note_text"),
            "surah_index = ? AND verse_number = ?",
            arrayOf(surahIndex.toString(), verseNumber.toString()),
            null, null, null
        )
        var note: String? = null
        cursor.use { c ->
            if (c.moveToFirst()) {
                note = c.getString(c.getColumnIndexOrThrow("note_text"))
            }
        }
        return note
    }

    fun getAllNotes(): List<UserNote> {
        val notes = mutableListOf<UserNote>()
        val userDb = readableDatabase
        val cursor = userDb.query(
            TABLE_NOTES,
            null, null, null, null, null,
            "timestamp DESC"
        )
        cursor.use { c ->
            val idCol = c.getColumnIndex("id")
            val surahIndexCol = c.getColumnIndex("surah_index")
            val verseNumberCol = c.getColumnIndex("verse_number")
            val surahNameCol = c.getColumnIndex("surah_name")
            val noteTextCol = c.getColumnIndex("note_text")
            val timestampCol = c.getColumnIndex("timestamp")

            while (c.moveToNext()) {
                notes.add(
                    UserNote(
                        id = c.getLong(idCol),
                        surahIndex = c.getInt(surahIndexCol),
                        verseNumber = c.getInt(verseNumberCol),
                        surahName = c.getString(surahNameCol),
                        noteText = c.getString(noteTextCol),
                        timestamp = c.getLong(timestampCol)
                    )
                )
            }
        }
        return notes
    }

    // --- Read Progress & Last Read Tracking ---

    fun markVerseRead(surahIndex: Int, verseNumber: Int, surahName: String) {
        val userDb = writableDatabase
        val now = System.currentTimeMillis()
        try {
            userDb.execSQL("ALTER TABLE $TABLE_READ_VERSES ADD COLUMN timestamp INTEGER DEFAULT 0")
        } catch (e: Exception) {
            // column already exists
        }

        val values = ContentValues().apply {
            put("surah_index", surahIndex)
            put("verse_number", verseNumber)
            put("timestamp", now)
        }
        userDb.insertWithOnConflict(TABLE_READ_VERSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)

        val lastReadValues = ContentValues().apply {
            put("id", 1)
            put("surah_index", surahIndex)
            put("verse_number", verseNumber)
            put("surah_name", surahName)
            put("timestamp", now)
        }
        userDb.insertWithOnConflict(TABLE_LAST_READ, null, lastReadValues, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSurahLastReadTimestamps(): Map<Int, Long> {
        val userDb = readableDatabase
        val map = mutableMapOf<Int, Long>()
        try {
            val cursor = userDb.rawQuery("SELECT surah_index, MAX(timestamp) FROM $TABLE_READ_VERSES GROUP BY surah_index", null)
            cursor.use { c ->
                while (c.moveToNext()) {
                    val sIndex = c.getInt(0)
                    val ts = c.getLong(1)
                    map[sIndex] = ts
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return map
    }

    fun resetSurahReadProgress(surahIndex: Int) {
        val userDb = writableDatabase
        userDb.delete(TABLE_READ_VERSES, "surah_index = ?", arrayOf(surahIndex.toString()))
    }

    fun getReadVerseCountForSurah(surahIndex: Int): Int {
        val userDb = readableDatabase
        val cursor = userDb.rawQuery("SELECT COUNT(*) FROM $TABLE_READ_VERSES WHERE surah_index = ?", arrayOf(surahIndex.toString()))
        var count = 0
        cursor.use { c ->
            if (c.moveToFirst()) count = c.getInt(0)
        }
        return count
    }

    fun getReadVerseNumbersForSurah(surahIndex: Int): Set<Int> {
        val userDb = readableDatabase
        val cursor = userDb.query(TABLE_READ_VERSES, arrayOf("verse_number"), "surah_index = ?", arrayOf(surahIndex.toString()), null, null, null)
        val set = mutableSetOf<Int>()
        cursor.use { c ->
            val colIndex = c.getColumnIndexOrThrow("verse_number")
            while (c.moveToNext()) {
                set.add(c.getInt(colIndex))
            }
        }
        return set
    }

    fun isVerseRead(surahIndex: Int, verseNumber: Int): Boolean {
        val userDb = readableDatabase
        val cursor = userDb.query(TABLE_READ_VERSES, arrayOf("verse_number"), "surah_index = ? AND verse_number = ?", arrayOf(surahIndex.toString(), verseNumber.toString()), null, null, null)
        var exists = false
        cursor.use { c ->
            exists = c.moveToFirst()
        }
        return exists
    }

    fun getTotalReadVerseCount(): Int {
        val userDb = readableDatabase
        val cursor = userDb.rawQuery("SELECT COUNT(*) FROM $TABLE_READ_VERSES", null)
        var count = 0
        cursor.use { c ->
            if (c.moveToFirst()) count = c.getInt(0)
        }
        return count
    }

    fun getLastReadLocation(): Triple<Int, Int, String>? {
        val userDb = readableDatabase
        val cursor = userDb.query(TABLE_LAST_READ, null, "id = 1", null, null, null, null)
        var result: Triple<Int, Int, String>? = null
        cursor.use { c ->
            if (c.moveToFirst()) {
                val sIndex = c.getInt(c.getColumnIndexOrThrow("surah_index"))
                val vNum = c.getInt(c.getColumnIndexOrThrow("verse_number"))
                val sName = c.getString(c.getColumnIndexOrThrow("surah_name"))
                result = Triple(sIndex, vNum, sName)
            }
        }
        return result
    }
}
