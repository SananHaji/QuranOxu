package az.sananhaji.quranoxu.data.preferences

import android.content.Context
import az.sananhaji.quranoxu.domain.model.SurahSortMode
import az.sananhaji.quranoxu.ui.theme.ThemeMode

class SettingsPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("quranoxu_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_AUDIO_LANGUAGE = "audio_language"
        private const val KEY_SHOW_ARABIC_BY_DEFAULT = "show_arabic_by_default"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SORT_MODE = "sort_mode"
    }

    var selectedLanguage: String
        get() = prefs.getString(KEY_SELECTED_LANGUAGE, "azerbaijani") ?: "azerbaijani"
        set(value) = prefs.edit().putString(KEY_SELECTED_LANGUAGE, value).apply()

    var audioLanguage: String
        get() = prefs.getString(KEY_AUDIO_LANGUAGE, "arabic") ?: "arabic"
        set(value) = prefs.edit().putString(KEY_AUDIO_LANGUAGE, value).apply()

    var showArabicByDefault: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ARABIC_BY_DEFAULT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ARABIC_BY_DEFAULT, value).apply()

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            return try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    var surahSortMode: SurahSortMode
        get() {
            val name = prefs.getString(KEY_SORT_MODE, SurahSortMode.QURAN_ORDER.name) ?: SurahSortMode.QURAN_ORDER.name
            return try {
                SurahSortMode.valueOf(name)
            } catch (e: Exception) {
                SurahSortMode.QURAN_ORDER
            }
        }
        set(value) = prefs.edit().putString(KEY_SORT_MODE, value.name).apply()
}
