package az.sananhaji.quranoxu.data.repository

import az.sananhaji.quranoxu.data.db.QuranDatabaseHelper
import az.sananhaji.quranoxu.data.mapper.toEntity
import az.sananhaji.quranoxu.domain.model.BookmarkEntity
import az.sananhaji.quranoxu.domain.model.OverallProgressEntity
import az.sananhaji.quranoxu.domain.model.SupportedLanguageEntity
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.UserNoteEntity
import az.sananhaji.quranoxu.domain.model.VerseEntity
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import az.sananhaji.quranoxu.domain.model.VerseSearchResultEntity
import az.sananhaji.quranoxu.util.containsSmart

class QuranRepositoryImpl(private val dbHelper: QuranDatabaseHelper) : QuranRepositoryContract {

    override suspend fun getSupportedLanguages(): List<SupportedLanguageEntity> = withContext(Dispatchers.IO) {
        dbHelper.getSupportedLanguages().map { it.toEntity() }
    }

    override suspend fun getAllSurahs(): List<SurahEntity> = withContext(Dispatchers.IO) {
        val rawSurahs = dbHelper.getAllSurahs()
        rawSurahs.map { s ->
            val revOrder = QuranDatabaseHelper.REVELATION_ORDERS.getOrElse(s.index - 1) { s.index }
            val readCount = dbHelper.getReadVerseCountForSurah(s.index)
            s.toEntity(revelationOrder = revOrder, readVerseCount = readCount)
        }
    }

    override suspend fun getSurahWithVerses(surahIndex: Int): Pair<SurahEntity?, List<VerseEntity>> = withContext(Dispatchers.IO) {
        val surah = dbHelper.getSurahByIndex(surahIndex) ?: return@withContext Pair(null, emptyList())

        val verses = mutableListOf<VerseEntity>()
        val totalVerses = surah.verseCount
        val readVerseNumbers = dbHelper.getReadVerseNumbersForSurah(surahIndex)

        for (i in 0 until totalVerses) {
            val verseNumber = i + 1
            val arabic = surah.versesArabic.getOrElse(i) { "" }
            val azeri = surah.versesAzerbaijani.getOrElse(i) { "" }
            val latin = surah.versesLatin.getOrNull(i)
            val isBookmarked = dbHelper.isBookmarked(surahIndex, verseNumber)
            val isRead = readVerseNumbers.contains(verseNumber)
            val noteText = dbHelper.getNoteText(surahIndex, verseNumber)

            verses.add(
                VerseEntity(
                    surahIndex = surahIndex,
                    verseNumber = verseNumber,
                    arabicText = arabic,
                    azeriText = azeri,
                    latinText = latin,
                    isBookmarked = isBookmarked,
                    isRead = isRead,
                    noteText = noteText
                )
            )
        }

        val revOrder = QuranDatabaseHelper.REVELATION_ORDERS.getOrElse(surah.index - 1) { surah.index }
        val readCount = dbHelper.getReadVerseCountForSurah(surah.index)

        Pair(surah.toEntity(revelationOrder = revOrder, readVerseCount = readCount), verses)
    }

    override suspend fun toggleBookmark(surahIndex: Int, verseNumber: Int, surahName: String): Boolean = withContext(Dispatchers.IO) {
        dbHelper.toggleBookmark(surahIndex, verseNumber, surahName)
    }

    override suspend fun saveNote(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String) = withContext(Dispatchers.IO) {
        dbHelper.saveNote(surahIndex, verseNumber, surahName, noteText)
    }

    override suspend fun deleteNote(surahIndex: Int, verseNumber: Int) = withContext(Dispatchers.IO) {
        dbHelper.deleteNote(surahIndex, verseNumber)
    }

    override suspend fun getAllBookmarks(): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        dbHelper.getAllBookmarks().map { it.toEntity() }
    }

    override suspend fun getAllNotes(): List<UserNoteEntity> = withContext(Dispatchers.IO) {
        dbHelper.getAllNotes().map { it.toEntity() }
    }

    override suspend fun markVerseRead(surahIndex: Int, verseNumber: Int, surahName: String) = withContext(Dispatchers.IO) {
        dbHelper.markVerseRead(surahIndex, verseNumber, surahName)
    }

    override suspend fun resetSurahReadProgress(surahIndex: Int) = withContext(Dispatchers.IO) {
        dbHelper.resetSurahReadProgress(surahIndex)
    }

    override suspend fun getOverallProgress(): OverallProgressEntity = withContext(Dispatchers.IO) {
        val totalRead = dbHelper.getTotalReadVerseCount()
        val lastRead = dbHelper.getLastReadLocation()
        val allSurahs = getAllSurahs()

        val completedCount = allSurahs.count { it.readVerseCount >= it.verseCount }

        // Smart Next Surah Recommendation: find first surah that is not fully completed
        val recommended = allSurahs.firstOrNull { it.readVerseCount < it.verseCount } ?: allSurahs.firstOrNull()

        val timestamps = dbHelper.getSurahLastReadTimestamps()

        OverallProgressEntity(
            totalReadVerses = totalRead,
            totalVerses = 6236,
            lastReadSurahIndex = lastRead?.first ?: 0,
            lastReadVerseNumber = lastRead?.second ?: 0,
            lastReadSurahName = lastRead?.third ?: "",
            completedSurahsCount = completedCount,
            recommendedSurah = recommended,
            surahLastReadTimestamps = timestamps
        )
    }

    override suspend fun searchVerses(query: String): List<VerseSearchResultEntity> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val rawSurahs = dbHelper.getAllSurahs()
        val results = mutableListOf<VerseSearchResultEntity>()

        for (surah in rawSurahs) {
            val surahName = surah.nameAzeri
            val total = surah.verseCount
            val revOrder = QuranDatabaseHelper.REVELATION_ORDERS.getOrElse(surah.index - 1) { surah.index }

            for (i in 0 until total) {
                val verseNum = i + 1
                val azeriText = surah.versesAzerbaijani.getOrElse(i) { "" }
                val latinText = surah.versesLatin.getOrNull(i)
                val arabicText = surah.versesArabic.getOrNull(i)

                if (azeriText.containsSmart(trimmedQuery) || (latinText != null && latinText.containsSmart(trimmedQuery))) {
                    results.add(
                        VerseSearchResultEntity(
                            surahIndex = surah.index,
                            surahName = surahName,
                            verseNumber = verseNum,
                            text = azeriText.ifBlank { latinText ?: "" },
                            arabicText = arabicText,
                            revelationOrder = revOrder
                        )
                    )
                }
            }
        }

        results
    }
}
