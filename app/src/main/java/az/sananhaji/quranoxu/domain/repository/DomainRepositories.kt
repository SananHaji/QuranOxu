package az.sananhaji.quranoxu.domain.repository

import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import az.sananhaji.quranoxu.domain.model.BookmarkEntity
import az.sananhaji.quranoxu.domain.model.SupportedLanguageEntity
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.UserNoteEntity
import az.sananhaji.quranoxu.domain.model.VerseEntity
import kotlinx.coroutines.flow.StateFlow

import az.sananhaji.quranoxu.domain.model.VerseSearchResultEntity

interface QuranRepositoryContract {
    suspend fun getSupportedLanguages(): List<SupportedLanguageEntity>
    suspend fun getAllSurahs(): List<SurahEntity>
    suspend fun getSurahWithVerses(surahIndex: Int): Pair<SurahEntity?, List<VerseEntity>>
    suspend fun toggleBookmark(surahIndex: Int, verseNumber: Int, surahName: String): Boolean
    suspend fun saveNote(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String)
    suspend fun deleteNote(surahIndex: Int, verseNumber: Int)
    suspend fun getAllBookmarks(): List<BookmarkEntity>
    suspend fun getAllNotes(): List<UserNoteEntity>
    suspend fun markVerseRead(surahIndex: Int, verseNumber: Int, surahName: String)
    suspend fun resetSurahReadProgress(surahIndex: Int)
    suspend fun getOverallProgress(): az.sananhaji.quranoxu.domain.model.OverallProgressEntity
    suspend fun searchVerses(query: String): List<VerseSearchResultEntity>
}

interface AudioRepositoryContract {
    val audioStateFlow: StateFlow<AudioStateEntity>
    val downloadStatusFlow: StateFlow<Map<Int, az.sananhaji.quranoxu.domain.model.SurahDownloadStatus>>
    fun playVerse(surahIndex: Int, verseNumber: Int, totalVerses: Int, surahName: String, audioLanguage: String = "arabic")
    fun playSurah(surahIndex: Int, totalVerses: Int, surahName: String, audioLanguage: String = "arabic")
    fun pauseAudio()
    fun resumeAudio()
    fun stopAudio()
    fun nextVerse()
    fun previousVerse()
    fun setSleepTimer(minutes: Int)
    fun cancelSleepTimer()
    suspend fun downloadSurah(surahIndex: Int, totalVerses: Int, language: String, onProgress: ((downloaded: Int, total: Int) -> Unit)? = null): Boolean
    fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): az.sananhaji.quranoxu.domain.model.SurahDownloadStatus
    fun isSurahFullyDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean
    fun deleteSurahAudio(surahIndex: Int, language: String): Boolean
    fun getCacheInfo(): az.sananhaji.quranoxu.domain.model.AudioCacheInfo
    fun clearAllAudioCache(): Boolean
}
