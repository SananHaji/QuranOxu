package az.sananhaji.quranoxu.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Helper object for Firebase Analytics and Crashlytics event tracking.
 */
object AppAnalytics {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
        } catch (e: Exception) {
            // Safe fallback if Firebase is not yet initialized with google-services.json
        }
    }

    fun logScreenView(screenName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun logSurahRead(surahIndex: Int, surahName: String) {
        try {
            val bundle = Bundle().apply {
                putInt("surah_index", surahIndex)
                putString("surah_name", surahName)
            }
            firebaseAnalytics?.logEvent("read_surah", bundle)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun logAudioPlay(surahIndex: Int, verseNumber: Int) {
        try {
            val bundle = Bundle().apply {
                putInt("surah_index", surahIndex)
                putInt("verse_number", verseNumber)
            }
            firebaseAnalytics?.logEvent("play_audio", bundle)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun logNonFatalCrash(throwable: Throwable, message: String? = null) {
        try {
            message?.let { FirebaseCrashlytics.getInstance().log(it) }
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
