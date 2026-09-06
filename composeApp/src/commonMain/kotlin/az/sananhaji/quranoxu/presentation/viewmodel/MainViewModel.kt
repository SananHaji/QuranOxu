package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import az.sananhaji.quranoxu.data.preferences.SettingsPreferences
import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import az.sananhaji.quranoxu.domain.usecase.ControlAudioUseCase
import az.sananhaji.quranoxu.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val controlAudioUseCase: ControlAudioUseCase,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _activeTab = MutableStateFlow(0) // 0 = Surahs, 1 = Bookmarks/Notes, 2 = Analytics, 3 = Settings
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Holds Pair(surahIndex, initialVerseNumber?) when a Surah detail is open, or null when on list/tabs
    private val _selectedSurahNav = MutableStateFlow<Pair<Int, Int?>?>(null)
    val selectedSurahNav: StateFlow<Pair<Int, Int?>?> = _selectedSurahNav.asStateFlow()

    val audioState: StateFlow<AudioStateEntity> = controlAudioUseCase.audioState

    val themeMode: ThemeMode
        get() = settingsPreferences.themeMode

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun openSurahDetail(surahIndex: Int, initialVerseNumber: Int? = null) {
        _selectedSurahNav.value = Pair(surahIndex, initialVerseNumber)
    }

    fun clearSelectedSurah() {
        _selectedSurahNav.value = null
    }

    fun toggleThemeMode() {
        val nextMode = when (settingsPreferences.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
        }
        settingsPreferences.themeMode = nextMode
    }

    fun pauseAudio() = controlAudioUseCase.pause()
    fun resumeAudio() = controlAudioUseCase.resume()
    fun stopAudio() = controlAudioUseCase.stop()
    fun nextAudio() = controlAudioUseCase.nextVerse()
    fun previousAudio() = controlAudioUseCase.previousVerse()
    fun setSleepTimer(minutes: Int) = controlAudioUseCase.setSleepTimer(minutes)
    fun cancelSleepTimer() = controlAudioUseCase.cancelSleepTimer()
    fun setPlaybackSpeed(speed: Float) = controlAudioUseCase.setPlaybackSpeed(speed)
}
