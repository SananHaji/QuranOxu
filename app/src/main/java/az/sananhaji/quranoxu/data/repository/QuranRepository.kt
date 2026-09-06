package az.sananhaji.quranoxu.data.repository

import az.sananhaji.quranoxu.data.db.QuranDatabaseHelper
import az.sananhaji.quranoxu.data.db.SupportedLanguage
import az.sananhaji.quranoxu.data.model.Bookmark
import az.sananhaji.quranoxu.data.model.Surah
import az.sananhaji.quranoxu.data.model.UserNote
import az.sananhaji.quranoxu.data.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(private val dbHelper: QuranDatabaseHelper) {

    suspend fun getSupportedLanguages(): List<SupportedLanguage> = withContext(Dispatchers.IO) {
        dbHelper.getSupportedLanguages()
    }

    suspend fun getAllSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        dbHelper.getAllSurahs()
    }

    suspend fun getSurahWithVerses(surahIndex: Int): Pair<Surah?, List<Verse>> = withContext(Dispatchers.IO) {
        val surah = dbHelper.getSurahByIndex(surahIndex) ?: return@withContext Pair(null, emptyList())
        
        val verses = mutableListOf<Verse>()
        val totalVerses = surah.verseCount

        for (i in 0 until totalVerses) {
            val verseNumber = i + 1
            val arabic = surah.versesArabic.getOrElse(i) { "" }
            val azeri = surah.versesAzerbaijani.getOrElse(i) { "" }
            val latin = surah.versesLatin.getOrNull(i)
            val isBookmarked = dbHelper.isBookmarked(surahIndex, verseNumber)
            val noteText = dbHelper.getNoteText(surahIndex, verseNumber)

            verses.add(
                Verse(
                    surahIndex = surahIndex,
                    verseNumber = verseNumber,
                    arabicText = arabic,
                    azeriText = azeri,
                    latinText = latin,
                    isBookmarked = isBookmarked,
                    noteText = noteText
                )
            )
        }
        Pair(surah, verses)
    }

    suspend fun toggleBookmark(surahIndex: Int, verseNumber: Int, surahName: String): Boolean = withContext(Dispatchers.IO) {
        dbHelper.toggleBookmark(surahIndex, verseNumber, surahName)
    }

    suspend fun saveNote(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String) = withContext(Dispatchers.IO) {
        dbHelper.saveNote(surahIndex, verseNumber, surahName, noteText)
    }

    suspend fun deleteNote(surahIndex: Int, verseNumber: Int) = withContext(Dispatchers.IO) {
        dbHelper.deleteNote(surahIndex, verseNumber)
    }

    suspend fun getAllBookmarks(): List<Bookmark> = withContext(Dispatchers.IO) {
        dbHelper.getAllBookmarks()
    }

    suspend fun getAllNotes(): List<UserNote> = withContext(Dispatchers.IO) {
        dbHelper.getAllNotes()
    }
}
