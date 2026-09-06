package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.SurahSortMode
import az.sananhaji.quranoxu.domain.model.VerseSearchResultEntity
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import az.sananhaji.quranoxu.domain.usecase.ControlAudioUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSurahsUseCase
import az.sananhaji.quranoxu.presentation.mvi.SurahListIntent
import az.sananhaji.quranoxu.presentation.mvi.SurahListState
import az.sananhaji.quranoxu.util.containsSmart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import az.sananhaji.quranoxu.data.preferences.SettingsPreferences

class SurahListViewModel(
    private val getSurahsUseCase: GetSurahsUseCase,
    private val quranRepository: QuranRepositoryContract,
    private val controlAudioUseCase: ControlAudioUseCase,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SurahListState(sortMode = settingsPreferences.surahSortMode))
    val state: StateFlow<SurahListState> = _state.asStateFlow()

    init {
        processIntent(SurahListIntent.LoadSurahs)
        viewModelScope.launch {
            controlAudioUseCase.downloadStatusFlow.collect { statuses ->
                _state.value = _state.value.copy(
                    downloadStatuses = _state.value.downloadStatuses + statuses
                )
            }
        }
    }

    fun processIntent(intent: SurahListIntent) {
        when (intent) {
            is SurahListIntent.LoadSurahs -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isLoading = true)
                    val surahs = getSurahsUseCase()
                    val progress = quranRepository.getOverallProgress()
                    val filtered = filterAndSortSurahs(surahs, _state.value.searchQuery, _state.value.sortMode)
                    val lang = settingsPreferences.audioLanguage
                    val initialStatuses = withContext(Dispatchers.IO) {
                        surahs.associate { s ->
                            s.index to controlAudioUseCase.getSurahDownloadStatus(s.index, s.verseCount, lang)
                        }
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        surahs = surahs,
                        filteredSurahs = filtered,
                        overallProgress = progress,
                        downloadStatuses = initialStatuses
                    )
                }
            }
            is SurahListIntent.SearchSurahs -> {
                val query = intent.query
                val currentSurahs = _state.value.surahs
                val filtered = filterAndSortSurahs(currentSurahs, query, _state.value.sortMode)
                _state.value = _state.value.copy(
                    searchQuery = query,
                    filteredSurahs = filtered
                )
            }
            is SurahListIntent.SetSortMode -> {
                val mode = intent.sortMode
                settingsPreferences.surahSortMode = mode
                val currentSurahs = _state.value.surahs
                val filtered = filterAndSortSurahs(currentSurahs, _state.value.searchQuery, mode)
                val sortedVerseResults = sortVerseResults(_state.value.verseSearchResults, mode)
                _state.value = _state.value.copy(
                    sortMode = mode,
                    filteredSurahs = filtered,
                    verseSearchResults = sortedVerseResults
                )
            }
            is SurahListIntent.SetSearchMode -> {
                _state.value = _state.value.copy(isVerseSearch = intent.isVerseSearch)
                if (intent.isVerseSearch && _state.value.verseSearchQuery.isNotBlank()) {
                    processIntent(SurahListIntent.SearchVerses(_state.value.verseSearchQuery))
                }
            }
            is SurahListIntent.SearchVerses -> {
                val query = intent.query
                _state.value = _state.value.copy(verseSearchQuery = query)
                if (query.isBlank()) {
                    _state.value = _state.value.copy(verseSearchResults = emptyList(), isVerseSearchLoading = false)
                } else {
                    viewModelScope.launch {
                        _state.value = _state.value.copy(isVerseSearchLoading = true)
                        val rawResults = quranRepository.searchVerses(query)
                        val sortedResults = sortVerseResults(rawResults, _state.value.sortMode)
                        _state.value = _state.value.copy(
                            isVerseSearchLoading = false,
                            verseSearchResults = sortedResults
                        )
                    }
                }
            }
            is SurahListIntent.ResetSurahReadProgress -> {
                viewModelScope.launch {
                    quranRepository.resetSurahReadProgress(intent.surahIndex)
                    processIntent(SurahListIntent.LoadSurahs)
                }
            }
            is SurahListIntent.DownloadSurah -> {
                viewModelScope.launch {
                    val lang = settingsPreferences.audioLanguage
                    controlAudioUseCase.downloadSurah(
                        intent.surahIndex,
                        intent.totalVerses,
                        lang
                    )
                    val updated = _state.value.downloadStatuses.toMutableMap()
                    updated[intent.surahIndex] = controlAudioUseCase.getSurahDownloadStatus(intent.surahIndex, intent.totalVerses, lang)
                    _state.value = _state.value.copy(downloadStatuses = updated)
                }
            }
            is SurahListIntent.CancelDownloadSurah -> {
                val lang = settingsPreferences.audioLanguage
                controlAudioUseCase.cancelDownloadSurah(intent.surahIndex, intent.totalVerses, lang)
                val updated = _state.value.downloadStatuses.toMutableMap()
                updated[intent.surahIndex] = controlAudioUseCase.getSurahDownloadStatus(intent.surahIndex, intent.totalVerses, lang)
                _state.value = _state.value.copy(downloadStatuses = updated)
            }
            is SurahListIntent.DeleteSurahAudio -> {
                viewModelScope.launch {
                    val lang = settingsPreferences.audioLanguage
                    controlAudioUseCase.deleteSurahAudio(intent.surahIndex, lang)
                    val s = _state.value.surahs.find { it.index == intent.surahIndex }
                    val totalV = s?.verseCount ?: 0
                    val updated = _state.value.downloadStatuses.toMutableMap()
                    updated[intent.surahIndex] = controlAudioUseCase.getSurahDownloadStatus(intent.surahIndex, totalV, lang)
                    _state.value = _state.value.copy(downloadStatuses = updated)
                }
            }
        }
    }

    private fun sortVerseResults(results: List<VerseSearchResultEntity>, sortMode: SurahSortMode): List<VerseSearchResultEntity> {
        return when (sortMode) {
            SurahSortMode.QURAN_ORDER -> results.sortedWith(compareBy({ it.surahIndex }, { it.verseNumber }))
            SurahSortMode.REVELATION_ORDER -> results.sortedWith(compareBy({ it.revelationOrder }, { it.verseNumber }))
            SurahSortMode.ALPHABETICAL -> results.sortedWith(compareBy({ it.surahName }, { it.verseNumber }))
            SurahSortMode.VERSE_COUNT_DESC -> results.sortedWith(compareBy({ it.surahIndex }, { it.verseNumber }))
            SurahSortMode.VERSE_COUNT_ASC -> results.sortedWith(compareBy({ it.surahIndex }, { it.verseNumber }))
        }
    }

    private fun filterAndSortSurahs(surahs: List<SurahEntity>, query: String, sortMode: SurahSortMode): List<SurahEntity> {
        val filtered = if (query.isBlank()) {
            surahs
        } else {
            val q = query.trim()
            surahs.filter { surah ->
                surah.index.toString() == q ||
                surah.nameAzeri.containsSmart(q) ||
                (surah.nameAzeriTercume?.containsSmart(q) == true) ||
                (surah.nameArabicLatin?.containsSmart(q) == true) ||
                (surah.nameArabicWithArabicLetter?.contains(q, ignoreCase = true) == true)
            }
        }

        return when (sortMode) {
            SurahSortMode.QURAN_ORDER -> filtered.sortedBy { it.index }
            SurahSortMode.REVELATION_ORDER -> filtered.sortedBy { it.revelationOrder }
            SurahSortMode.ALPHABETICAL -> filtered.sortedBy { it.nameAzeri }
            SurahSortMode.VERSE_COUNT_DESC -> filtered.sortedByDescending { it.verseCount }
            SurahSortMode.VERSE_COUNT_ASC -> filtered.sortedBy { it.verseCount }
        }
    }

    class Factory(
        private val getSurahsUseCase: GetSurahsUseCase,
        private val quranRepository: QuranRepositoryContract,
        private val controlAudioUseCase: ControlAudioUseCase,
        private val settingsPreferences: SettingsPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SurahListViewModel::class.java)) {
                return SurahListViewModel(getSurahsUseCase, quranRepository, controlAudioUseCase, settingsPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
