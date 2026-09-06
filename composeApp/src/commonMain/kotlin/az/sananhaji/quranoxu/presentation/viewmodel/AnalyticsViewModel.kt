package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import az.sananhaji.quranoxu.presentation.mvi.AnalyticsIntent
import az.sananhaji.quranoxu.presentation.mvi.AnalyticsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val quranRepository: QuranRepositoryContract
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        processIntent(AnalyticsIntent.LoadAnalytics)
    }

    fun processIntent(intent: AnalyticsIntent) {
        when (intent) {
            is AnalyticsIntent.LoadAnalytics -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isLoading = true)
                    val progress = quranRepository.getOverallProgress()
                    val surahs = quranRepository.getAllSurahs()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        overallProgress = progress,
                        surahs = surahs
                    )
                }
            }
            is AnalyticsIntent.ResetSurahReadProgress -> {
                viewModelScope.launch {
                    quranRepository.resetSurahReadProgress(intent.surahIndex)
                    processIntent(AnalyticsIntent.LoadAnalytics)
                }
            }
        }
    }
}
