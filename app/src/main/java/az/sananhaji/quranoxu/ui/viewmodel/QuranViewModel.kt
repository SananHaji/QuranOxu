package az.sananhaji.quranoxu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.data.db.SupportedLanguage
import az.sananhaji.quranoxu.data.model.Bookmark
import az.sananhaji.quranoxu.data.model.Surah
import az.sananhaji.quranoxu.data.model.UserNote
import az.sananhaji.quranoxu.data.model.Verse
import az.sananhaji.quranoxu.data.repository.QuranRepository
import az.sananhaji.quranoxu.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _supportedLanguages = MutableStateFlow<List<SupportedLanguage>>(emptyList())
    val supportedLanguages: StateFlow<List<SupportedLanguage>> = _supportedLanguages.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("azerbaijani")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _showArabicByDefault = MutableStateFlow(true)
    val showArabicByDefault: StateFlow<Boolean> = _showArabicByDefault.asStateFlow()

    private val _perVerseArabicOverrides = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val perVerseArabicOverrides: StateFlow<Map<Int, Boolean>> = _perVerseArabicOverrides.asStateFlow()

    private val _allSurahs = MutableStateFlow<List<Surah>>(emptyList())
    val allSurahs: StateFlow<List<Surah>> = _allSurahs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSurahs: StateFlow<List<Surah>> = combine(_allSurahs, _searchQuery) { surahs, query ->
        if (query.isBlank()) {
            surahs
        } else {
            val q = query.trim().lowercase()
            surahs.filter { surah ->
                surah.index.toString() == q ||
                surah.nameAzeri.lowercase().contains(q) ||
                (surah.nameAzeriTercume?.lowercase()?.contains(q) == true) ||
                (surah.nameArabicLatin?.lowercase()?.contains(q) == true) ||
                (surah.nameArabicWithArabicLetter?.contains(q) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _versesList = MutableStateFlow<List<Verse>>(emptyList())
    val versesList: StateFlow<List<Verse>> = _versesList.asStateFlow()

    private val _bookmarksList = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarksList: StateFlow<List<Bookmark>> = _bookmarksList.asStateFlow()

    private val _notesList = MutableStateFlow<List<UserNote>>(emptyList())
    val notesList: StateFlow<List<UserNote>> = _notesList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0 = Surahs, 1 = Bookmarks/Notes, 2 = Settings
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _editingNoteVerse = MutableStateFlow<Verse?>(null)
    val editingNoteVerse: StateFlow<Verse?> = _editingNoteVerse.asStateFlow()

    init {
        loadSupportedLanguages()
        loadSurahs()
        loadBookmarksAndNotes()
    }

    private fun loadSupportedLanguages() {
        viewModelScope.launch {
            _supportedLanguages.value = repository.getSupportedLanguages()
        }
    }

    fun setSelectedLanguage(code: String) {
        _selectedLanguage.value = code
    }

    fun setShowArabicByDefault(show: Boolean) {
        _showArabicByDefault.value = show
    }

    fun togglePerVerseArabic(verseNumber: Int) {
        val currentMap = _perVerseArabicOverrides.value
        val isCurrentlyVisible = currentMap[verseNumber] ?: _showArabicByDefault.value
        _perVerseArabicOverrides.value = currentMap + (verseNumber to !isCurrentlyVisible)
    }

    fun toggleTheme() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
        if (tabIndex == 1) {
            loadBookmarksAndNotes()
        }
    }

    fun loadSurahs() {
        viewModelScope.launch {
            _isLoading.value = true
            _allSurahs.value = repository.getAllSurahs()
            _isLoading.value = false
        }
    }

    fun selectSurah(surahIndex: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val (surah, verses) = repository.getSurahWithVerses(surahIndex)
            _selectedSurah.value = surah
            _versesList.value = verses
            _isLoading.value = false
        }
    }

    fun clearSelectedSurah() {
        _selectedSurah.value = null
        _versesList.value = emptyList()
    }

    fun toggleBookmark(verse: Verse) {
        val surah = _selectedSurah.value ?: return
        viewModelScope.launch {
            val newStatus = repository.toggleBookmark(verse.surahIndex, verse.verseNumber, surah.nameAzeri)
            _versesList.value = _versesList.value.map { v ->
                if (v.verseNumber == verse.verseNumber) v.copy(isBookmarked = newStatus) else v
            }
            loadBookmarksAndNotes()
        }
    }

    fun openNoteDialog(verse: Verse) {
        _editingNoteVerse.value = verse
    }

    fun closeNoteDialog() {
        _editingNoteVerse.value = null
    }

    fun saveNote(verse: Verse, noteText: String) {
        val surahName = _selectedSurah.value?.nameAzeri ?: "Surə ${verse.surahIndex}"
        viewModelScope.launch {
            repository.saveNote(verse.surahIndex, verse.verseNumber, surahName, noteText)
            val updatedNoteText = if (noteText.isBlank()) null else noteText.trim()
            _versesList.value = _versesList.value.map { v ->
                if (v.verseNumber == verse.verseNumber) v.copy(noteText = updatedNoteText) else v
            }
            closeNoteDialog()
            loadBookmarksAndNotes()
        }
    }

    fun deleteNote(verse: Verse) {
        viewModelScope.launch {
            repository.deleteNote(verse.surahIndex, verse.verseNumber)
            _versesList.value = _versesList.value.map { v ->
                if (v.verseNumber == verse.verseNumber) v.copy(noteText = null) else v
            }
            closeNoteDialog()
            loadBookmarksAndNotes()
        }
    }

    fun loadBookmarksAndNotes() {
        viewModelScope.launch {
            _bookmarksList.value = repository.getAllBookmarks()
            _notesList.value = repository.getAllNotes()
        }
    }

    class Factory(private val repository: QuranRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
                return QuranViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
