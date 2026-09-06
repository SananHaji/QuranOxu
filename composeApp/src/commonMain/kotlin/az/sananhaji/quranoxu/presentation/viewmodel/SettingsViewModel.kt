package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.data.preferences.SettingsPreferences
import az.sananhaji.quranoxu.domain.usecase.ControlAudioUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSupportedLanguagesUseCase
import az.sananhaji.quranoxu.presentation.mvi.SettingsIntent
import az.sananhaji.quranoxu.presentation.mvi.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSupportedLanguagesUseCase: GetSupportedLanguagesUseCase,
    private val controlAudioUseCase: ControlAudioUseCase,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            selectedLanguage = settingsPreferences.selectedLanguage,
            audioLanguage = settingsPreferences.audioLanguage,
            showArabicByDefault = settingsPreferences.showArabicByDefault,
            themeMode = settingsPreferences.themeMode,
            audioCacheInfo = controlAudioUseCase.getCacheInfo()
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        processIntent(SettingsIntent.LoadLanguages)
        processIntent(SettingsIntent.LoadAudioCacheInfo)
    }

    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadLanguages -> {
                viewModelScope.launch {
                    val langs = getSupportedLanguagesUseCase()
                    _state.value = _state.value.copy(supportedLanguages = langs)
                }
            }
            is SettingsIntent.SetLanguage -> {
                settingsPreferences.selectedLanguage = intent.code
                _state.value = _state.value.copy(selectedLanguage = intent.code)
            }
            is SettingsIntent.SetAudioLanguage -> {
                settingsPreferences.audioLanguage = intent.audioLang
                _state.value = _state.value.copy(
                    audioLanguage = intent.audioLang,
                    audioCacheInfo = controlAudioUseCase.getCacheInfo()
                )
            }
            is SettingsIntent.SetShowArabicByDefault -> {
                settingsPreferences.showArabicByDefault = intent.show
                _state.value = _state.value.copy(showArabicByDefault = intent.show)
            }
            is SettingsIntent.SetThemeMode -> {
                settingsPreferences.themeMode = intent.mode
                _state.value = _state.value.copy(themeMode = intent.mode)
            }
            is SettingsIntent.LoadAudioCacheInfo -> {
                _state.value = _state.value.copy(audioCacheInfo = controlAudioUseCase.getCacheInfo())
            }
            is SettingsIntent.ClearAllAudioCache -> {
                viewModelScope.launch {
                    controlAudioUseCase.clearAllAudioCache()
                    _state.value = _state.value.copy(audioCacheInfo = controlAudioUseCase.getCacheInfo())
                }
            }
        }
    }
}
