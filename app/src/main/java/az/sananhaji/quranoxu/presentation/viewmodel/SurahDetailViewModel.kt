package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.data.preferences.SettingsPreferences
import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import az.sananhaji.quranoxu.domain.usecase.ControlAudioUseCase
import az.sananhaji.quranoxu.domain.usecase.DeleteNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSurahDetailUseCase
import az.sananhaji.quranoxu.domain.usecase.SaveNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.ToggleBookmarkUseCase
import az.sananhaji.quranoxu.presentation.mvi.SettingsState
import az.sananhaji.quranoxu.presentation.mvi.SurahDetailIntent
import az.sananhaji.quranoxu.presentation.mvi.SurahDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SurahDetailViewModel(
    private val getSurahDetailUseCase: GetSurahDetailUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val controlAudioUseCase: ControlAudioUseCase,
    private val quranRepository: QuranRepositoryContract,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SurahDetailState())
    val state: StateFlow<SurahDetailState> = _state.asStateFlow()

    val audioState: StateFlow<AudioStateEntity> = controlAudioUseCase.audioState

    val settingsState = MutableStateFlow(
        SettingsState(
            selectedLanguage = settingsPreferences.selectedLanguage,
            audioLanguage = settingsPreferences.audioLanguage,
            showArabicByDefault = settingsPreferences.showArabicByDefault,
            themeMode = settingsPreferences.themeMode
        )
    )

    init {
        viewModelScope.launch {
            controlAudioUseCase.downloadStatusFlow.collect { statuses ->
                val sIndex = _state.value.surah?.index ?: return@collect
                val status = statuses[sIndex]
                if (status != null) {
                    _state.value = _state.value.copy(downloadStatus = status)
                }
            }
        }
    }

    fun loadSurah(surahIndex: Int, initialVerseNumber: Int? = null) {
        if (_state.value.surah?.index == surahIndex && _state.value.verses.isNotEmpty()) {
            if (initialVerseNumber != null) {
                _state.value = _state.value.copy(
                    targetScrollVerseNumber = initialVerseNumber,
                    scrollTrigger = System.currentTimeMillis()
                )
            }
            return
        }
        processIntent(SurahDetailIntent.LoadSurahDetail(surahIndex, initialVerseNumber))
    }

    fun processIntent(intent: SurahDetailIntent) {
        when (intent) {
            is SurahDetailIntent.LoadSurahDetail -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isLoading = true)
                    val (surah, verses) = getSurahDetailUseCase(intent.surahIndex)
                    val lang = settingsPreferences.audioLanguage
                    val dlStatus = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        surah?.let {
                            controlAudioUseCase.getSurahDownloadStatus(it.index, it.verseCount, lang)
                        } ?: az.sananhaji.quranoxu.domain.model.SurahDownloadStatus(intent.surahIndex)
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        surah = surah,
                        verses = verses,
                        targetScrollVerseNumber = intent.initialVerseNumber,
                        scrollTrigger = System.currentTimeMillis(),
                        downloadStatus = dlStatus
                    )
                    intent.initialVerseNumber?.let { vNum ->
                        surah?.let { s ->
                            quranRepository.markVerseRead(s.index, vNum, s.nameAzeri)
                        }
                    }
                }
            }
            is SurahDetailIntent.ScrollToVerse -> {
                _state.value = _state.value.copy(
                    targetScrollVerseNumber = intent.verseNumber,
                    scrollTrigger = System.currentTimeMillis()
                )
            }
            is SurahDetailIntent.MarkVerseRead -> {
                val surah = _state.value.surah ?: return
                val currentVerse = _state.value.verses.find { it.verseNumber == intent.verseNumber }
                if (currentVerse?.isRead == true) return
                viewModelScope.launch {
                    quranRepository.markVerseRead(surah.index, intent.verseNumber, surah.nameAzeri)
                    val updatedReadCount = quranRepository.getAllSurahs().find { it.index == surah.index }?.readVerseCount ?: (surah.readVerseCount + 1)
                    _state.value = _state.value.copy(
                        surah = surah.copy(readVerseCount = updatedReadCount),
                        verses = _state.value.verses.map { v ->
                            if (v.verseNumber == intent.verseNumber) v.copy(isRead = true) else v
                        }
                    )
                }
            }
            is SurahDetailIntent.ToggleBookmark -> {
                val surah = _state.value.surah ?: return
                viewModelScope.launch {
                    val newStatus = toggleBookmarkUseCase(intent.verse.surahIndex, intent.verse.verseNumber, surah.nameAzeri)
                    _state.value = _state.value.copy(
                        verses = _state.value.verses.map { v ->
                            if (v.verseNumber == intent.verse.verseNumber) v.copy(isBookmarked = newStatus) else v
                        }
                    )
                }
            }
            is SurahDetailIntent.OpenNoteDialog -> {
                _state.value = _state.value.copy(editingVerse = intent.verse)
            }
            is SurahDetailIntent.CloseNoteDialog -> {
                _state.value = _state.value.copy(editingVerse = null)
            }
            is SurahDetailIntent.SaveNote -> {
                val surahName = _state.value.surah?.nameAzeri ?: "Surə"
                viewModelScope.launch {
                    saveNoteUseCase(intent.verse.surahIndex, intent.verse.verseNumber, surahName, intent.noteText)
                    val updatedNote = if (intent.noteText.isBlank()) null else intent.noteText.trim()
                    _state.value = _state.value.copy(
                        editingVerse = null,
                        verses = _state.value.verses.map { v ->
                            if (v.verseNumber == intent.verse.verseNumber) v.copy(noteText = updatedNote) else v
                        }
                    )
                }
            }
            is SurahDetailIntent.DeleteNote -> {
                viewModelScope.launch {
                    deleteNoteUseCase(intent.verse.surahIndex, intent.verse.verseNumber)
                    _state.value = _state.value.copy(
                        editingVerse = null,
                        verses = _state.value.verses.map { v ->
                            if (v.verseNumber == intent.verse.verseNumber) v.copy(noteText = null) else v
                        }
                    )
                }
            }
            is SurahDetailIntent.ToggleArabic -> {
                val currentMap = _state.value.perVerseArabicOverrides
                val isVisible = currentMap[intent.verseNumber] ?: settingsPreferences.showArabicByDefault
                _state.value = _state.value.copy(
                    perVerseArabicOverrides = currentMap + (intent.verseNumber to !isVisible)
                )
            }
            is SurahDetailIntent.PlayVerse -> {
                val surah = _state.value.surah ?: return
                val audioLang = settingsPreferences.audioLanguage
                viewModelScope.launch {
                    quranRepository.markVerseRead(surah.index, intent.verseNumber, surah.nameAzeri)
                }
                controlAudioUseCase.playVerse(surah.index, intent.verseNumber, surah.verseCount, surah.nameAzeri, audioLang)
            }
            is SurahDetailIntent.PlayFullSurah -> {
                val surah = _state.value.surah ?: return
                val audioLang = settingsPreferences.audioLanguage
                viewModelScope.launch {
                    quranRepository.markVerseRead(surah.index, 1, surah.nameAzeri)
                }
                controlAudioUseCase.playSurah(surah.index, surah.verseCount, surah.nameAzeri, audioLang)
            }
            is SurahDetailIntent.DownloadCurrentSurah -> {
                val surah = _state.value.surah ?: return
                val audioLang = settingsPreferences.audioLanguage
                viewModelScope.launch {
                    controlAudioUseCase.downloadSurah(surah.index, surah.verseCount, audioLang)
                }
            }
            is SurahDetailIntent.DeleteCurrentSurahAudio -> {
                val surah = _state.value.surah ?: return
                val audioLang = settingsPreferences.audioLanguage
                viewModelScope.launch {
                    controlAudioUseCase.deleteSurahAudio(surah.index, audioLang)
                    val st = controlAudioUseCase.getSurahDownloadStatus(surah.index, surah.verseCount, audioLang)
                    _state.value = _state.value.copy(downloadStatus = st)
                }
            }
        }
    }

    class Factory(
        private val getSurahDetailUseCase: GetSurahDetailUseCase,
        private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
        private val saveNoteUseCase: SaveNoteUseCase,
        private val deleteNoteUseCase: DeleteNoteUseCase,
        private val controlAudioUseCase: ControlAudioUseCase,
        private val quranRepository: QuranRepositoryContract,
        private val settingsPreferences: SettingsPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SurahDetailViewModel::class.java)) {
                return SurahDetailViewModel(
                    getSurahDetailUseCase, toggleBookmarkUseCase, saveNoteUseCase,
                    deleteNoteUseCase, controlAudioUseCase, quranRepository, settingsPreferences
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
