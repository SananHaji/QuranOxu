package az.sananhaji.quranoxu.domain.model

import kotlinx.serialization.Serializable

enum class SurahSortMode(val displayName: String) {
    QURAN_ORDER("Qurandakı sıra"),
    REVELATION_ORDER("Nüzul sırası"),
    ALPHABETICAL("Əlifba sırası"),
    VERSE_COUNT_DESC("Ayə sayı (Çoxdan aza)"),
    VERSE_COUNT_ASC("Ayə sayı (Azdan çoxa)")
}

data class SurahEntity(
    val index: Int,
    val verseCount: Int,
    val nameArabicLatin: String?,
    val versesLatin: List<String>,
    val versesAzerbaijani: List<String>,
    val versesArabic: List<String>,
    val nameAzeri: String,
    val nameAzeriTercume: String?,
    val placeAzeri: String?,
    val placeEng: String?,
    val nameArabicWithArabicLetter: String?,
    val revelationOrder: Int = index,
    val readVerseCount: Int = 0
)

data class VerseEntity(
    val surahIndex: Int,
    val verseNumber: Int,
    val arabicText: String,
    val azeriText: String,
    val latinText: String?,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val noteText: String? = null
)

@Serializable
data class BookmarkEntity(
    val id: Long = 0,
    val surahIndex: Int,
    val verseNumber: Int,
    val surahName: String,
    val timestamp: Long = az.sananhaji.quranoxu.util.getCurrentTimeMillis()
)

@Serializable
data class UserNoteEntity(
    val id: Long = 0,
    val surahIndex: Int,
    val verseNumber: Int,
    val surahName: String,
    val noteText: String,
    val timestamp: Long = az.sananhaji.quranoxu.util.getCurrentTimeMillis()
)

data class SupportedLanguageEntity(
    val code: String,
    val displayName: String
)

data class AudioStateEntity(
    val isPlaying: Boolean = false,
    val surahIndex: Int = 0,
    val surahName: String = "",
    val verseNumber: Int = 0,
    val totalVerses: Int = 0,
    val audioLanguage: String = "arabic",
    val remainingSleepTimerSeconds: Long = 0L,
    val isBuffering: Boolean = false,
    val isOfflineAvailable: Boolean = false,
    val playbackSpeed: Float = 1.0f
)

data class SurahDownloadStatus(
    val surahIndex: Int,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadedVerses: Int = 0,
    val totalVerses: Int = 0,
    val progress: Float = 0f,
    val sizeBytes: Long = 0L
)

data class AudioCacheInfo(
    val totalSizeBytes: Long = 0L,
    val formattedSize: String = "0 MB",
    val downloadedSurahsCount: Int = 0
)

data class OverallProgressEntity(
    val totalReadVerses: Int = 0,
    val totalVerses: Int = 6236,
    val lastReadSurahIndex: Int = 0,
    val lastReadVerseNumber: Int = 0,
    val lastReadSurahName: String = "",
    val completedSurahsCount: Int = 0,
    val recommendedSurah: SurahEntity? = null,
    val surahLastReadTimestamps: Map<Int, Long> = emptyMap()
)

data class VerseSearchResultEntity(
    val surahIndex: Int,
    val surahName: String,
    val verseNumber: Int,
    val text: String,
    val arabicText: String? = null,
    val revelationOrder: Int = surahIndex
)
