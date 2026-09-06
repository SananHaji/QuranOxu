package az.sananhaji.quranoxu.data.repository

import android.content.Context
import az.sananhaji.quranoxu.data.audio.AudioCacheManager
import az.sananhaji.quranoxu.domain.model.AudioCacheInfo
import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import az.sananhaji.quranoxu.domain.model.SurahDownloadStatus
import az.sananhaji.quranoxu.domain.repository.AudioRepositoryContract
import az.sananhaji.quranoxu.service.QuranAudioService
import kotlinx.coroutines.flow.StateFlow

class AudioRepositoryImpl(private val context: Context) : AudioRepositoryContract {

    private val audioCacheManager = AudioCacheManager(context)

    override val audioStateFlow: StateFlow<AudioStateEntity>
        get() = QuranAudioService.audioStateFlow

    override val downloadStatusFlow: StateFlow<Map<Int, SurahDownloadStatus>>
        get() = audioCacheManager.downloadStatusFlow

    override fun playVerse(surahIndex: Int, verseNumber: Int, totalVerses: Int, surahName: String, audioLanguage: String) {
        QuranAudioService.playVerse(context, surahIndex, verseNumber, totalVerses, surahName, audioLanguage)
    }

    override fun playSurah(surahIndex: Int, totalVerses: Int, surahName: String, audioLanguage: String) {
        QuranAudioService.playVerse(context, surahIndex, 1, totalVerses, surahName, audioLanguage)
    }

    override fun pauseAudio() {
        QuranAudioService.pause(context)
    }

    override fun resumeAudio() {
        QuranAudioService.resume(context)
    }

    override fun stopAudio() {
        QuranAudioService.stop(context)
    }

    override fun nextVerse() {
        QuranAudioService.nextVerse(context)
    }

    override fun previousVerse() {
        QuranAudioService.previousVerse(context)
    }

    override fun setSleepTimer(minutes: Int) {
        QuranAudioService.setSleepTimer(context, minutes)
    }

    override fun cancelSleepTimer() {
        QuranAudioService.cancelSleepTimer(context)
    }

    override fun setPlaybackSpeed(speed: Float) {
        QuranAudioService.setPlaybackSpeed(context, speed)
    }

    override suspend fun downloadSurah(
        surahIndex: Int,
        totalVerses: Int,
        language: String,
        onProgress: ((downloaded: Int, total: Int) -> Unit)?
    ): Boolean {
        return audioCacheManager.downloadSurah(surahIndex, totalVerses, language, onProgress)
    }

    override fun cancelDownloadSurah(surahIndex: Int, totalVerses: Int, language: String) {
        audioCacheManager.cancelDownload(surahIndex, totalVerses, language)
    }

    override fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): SurahDownloadStatus {
        return audioCacheManager.getSurahDownloadStatus(surahIndex, totalVerses, language)
    }

    override fun isSurahFullyDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean {
        return audioCacheManager.isSurahFullyDownloaded(surahIndex, totalVerses, language)
    }

    override fun deleteSurahAudio(surahIndex: Int, language: String): Boolean {
        return audioCacheManager.deleteSurahCache(surahIndex, language)
    }

    override fun getCacheInfo(): AudioCacheInfo {
        return audioCacheManager.getCacheInfo()
    }

    override fun clearAllAudioCache(): Boolean {
        return audioCacheManager.clearAllCache()
    }
}
