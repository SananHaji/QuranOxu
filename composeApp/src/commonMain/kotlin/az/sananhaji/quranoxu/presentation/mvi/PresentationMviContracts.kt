package az.sananhaji.quranoxu.presentation.mvi

import az.sananhaji.quranoxu.domain.model.AudioCacheInfo
import az.sananhaji.quranoxu.domain.model.BookmarkEntity
import az.sananhaji.quranoxu.domain.model.OverallProgressEntity
import az.sananhaji.quranoxu.domain.model.SupportedLanguageEntity
import az.sananhaji.quranoxu.domain.model.SurahDownloadStatus
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.SurahSortMode
import az.sananhaji.quranoxu.domain.model.UserNoteEntity
import az.sananhaji.quranoxu.domain.model.VerseEntity
import az.sananhaji.quranoxu.ui.theme.ThemeMode
import az.sananhaji.quranoxu.domain.model.VerseSearchResultEntity

// --- Surah List MVI ---
sealed interface SurahListIntent {
    object LoadSurahs : SurahListIntent
    data class SearchSurahs(val query: String) : SurahListIntent
    data class SetSortMode(val sortMode: SurahSortMode) : SurahListIntent
    data class SetSearchMode(val isVerseSearch: Boolean) : SurahListIntent
    data class SearchVerses(val query: String) : SurahListIntent
    data class ResetSurahReadProgress(val surahIndex: Int) : SurahListIntent
    data class DownloadSurah(val surahIndex: Int, val totalVerses: Int) : SurahListIntent
    data class CancelDownloadSurah(val surahIndex: Int, val totalVerses: Int) : SurahListIntent
    data class DeleteSurahAudio(val surahIndex: Int) : SurahListIntent
}

data class SurahListState(
    val isLoading: Boolean = false,
    val surahs: List<SurahEntity> = emptyList(),
    val filteredSurahs: List<SurahEntity> = emptyList(),
    val searchQuery: String = "",
    val sortMode: SurahSortMode = SurahSortMode.QURAN_ORDER,
    val overallProgress: OverallProgressEntity = OverallProgressEntity(),
    val isVerseSearch: Boolean = false,
    val verseSearchQuery: String = "",
    val verseSearchResults: List<VerseSearchResultEntity> = emptyList(),
    val isVerseSearchLoading: Boolean = false,
    val downloadStatuses: Map<Int, SurahDownloadStatus> = emptyMap()
)

// --- Surah Detail MVI ---
sealed interface SurahDetailIntent {
    data class LoadSurahDetail(val surahIndex: Int, val initialVerseNumber: Int? = null) : SurahDetailIntent
    data class ScrollToVerse(val verseNumber: Int) : SurahDetailIntent
    data class ToggleBookmark(val verse: VerseEntity) : SurahDetailIntent
    data class OpenNoteDialog(val verse: VerseEntity) : SurahDetailIntent
    object CloseNoteDialog : SurahDetailIntent
    data class SaveNote(val verse: VerseEntity, val noteText: String) : SurahDetailIntent
    data class DeleteNote(val verse: VerseEntity) : SurahDetailIntent
    data class ToggleArabic(val verseNumber: Int) : SurahDetailIntent
    data class PlayVerse(val verseNumber: Int) : SurahDetailIntent
    object PlayFullSurah : SurahDetailIntent
    data class MarkVerseRead(val verseNumber: Int) : SurahDetailIntent
    object DownloadCurrentSurah : SurahDetailIntent
    object CancelCurrentSurahDownload : SurahDetailIntent
    object DeleteCurrentSurahAudio : SurahDetailIntent
}

data class SurahDetailState(
    val isLoading: Boolean = false,
    val surah: SurahEntity? = null,
    val verses: List<VerseEntity> = emptyList(),
    val editingVerse: VerseEntity? = null,
    val perVerseArabicOverrides: Map<Int, Boolean> = emptyMap(),
    val targetScrollVerseNumber: Int? = null,
    val scrollTrigger: Long = 0L,
    val downloadStatus: SurahDownloadStatus = SurahDownloadStatus(0)
)

// --- Bookmarks Notes MVI ---
sealed interface BookmarksNotesIntent {
    object LoadData : BookmarksNotesIntent
    data class SelectTab(val tabIndex: Int) : BookmarksNotesIntent
    data class DeleteBookmark(val bookmark: BookmarkEntity) : BookmarksNotesIntent
    data class OpenEditNoteDialog(val note: UserNoteEntity) : BookmarksNotesIntent
    object CloseEditNoteDialog : BookmarksNotesIntent
    data class SaveEditedNote(val note: UserNoteEntity, val newText: String) : BookmarksNotesIntent
    data class DeleteNote(val note: UserNoteEntity) : BookmarksNotesIntent
}

data class BookmarksNotesState(
    val selectedTab: Int = 0,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val notes: List<UserNoteEntity> = emptyList(),
    val editingNote: UserNoteEntity? = null
)

// --- Analytics MVI ---
sealed interface AnalyticsIntent {
    object LoadAnalytics : AnalyticsIntent
    data class ResetSurahReadProgress(val surahIndex: Int) : AnalyticsIntent
}

data class AnalyticsState(
    val isLoading: Boolean = false,
    val overallProgress: OverallProgressEntity = OverallProgressEntity(),
    val surahs: List<SurahEntity> = emptyList()
)

// --- Settings MVI ---
sealed interface SettingsIntent {
    object LoadLanguages : SettingsIntent
    data class SetLanguage(val code: String) : SettingsIntent
    data class SetAudioLanguage(val audioLang: String) : SettingsIntent
    data class SetShowArabicByDefault(val show: Boolean) : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    object LoadAudioCacheInfo : SettingsIntent
    object ClearAllAudioCache : SettingsIntent
}

data class SettingsState(
    val supportedLanguages: List<SupportedLanguageEntity> = emptyList(),
    val selectedLanguage: String = "azerbaijani",
    val audioLanguage: String = "arabic",
    val showArabicByDefault: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val audioCacheInfo: AudioCacheInfo = AudioCacheInfo()
)
