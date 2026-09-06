package az.sananhaji.quranoxu.data.preferences

import az.sananhaji.quranoxu.domain.model.SurahSortMode
import az.sananhaji.quranoxu.ui.theme.ThemeMode
import com.russhwolf.settings.Settings

class SettingsPreferences(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_AUDIO_LANGUAGE = "audio_language"
        private const val KEY_SHOW_ARABIC_BY_DEFAULT = "show_arabic_by_default"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
    }

    var selectedLanguage: String
        get() = settings.getString(KEY_SELECTED_LANGUAGE, "azerbaijani")
        set(value) = settings.putString(KEY_SELECTED_LANGUAGE, value)

    var audioLanguage: String
        get() = settings.getString(KEY_AUDIO_LANGUAGE, "arabic")
        set(value) = settings.putString(KEY_AUDIO_LANGUAGE, value)

    var showArabicByDefault: Boolean
        get() = settings.getBoolean(KEY_SHOW_ARABIC_BY_DEFAULT, true)
        set(value) = settings.putBoolean(KEY_SHOW_ARABIC_BY_DEFAULT, value)

    var playbackSpeed: Float
        get() = settings.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = settings.putFloat(KEY_PLAYBACK_SPEED, value)

    var themeMode: ThemeMode
        get() {
            val name = settings.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            return try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
        set(value) = settings.putString(KEY_THEME_MODE, value.name)

    var surahSortMode: SurahSortMode
        get() {
            val name = settings.getString(KEY_SORT_MODE, SurahSortMode.QURAN_ORDER.name)
            return try {
                SurahSortMode.valueOf(name)
            } catch (e: Exception) {
                SurahSortMode.QURAN_ORDER
            }
        }
        set(value) = settings.putString(KEY_SORT_MODE, value.name)
}
