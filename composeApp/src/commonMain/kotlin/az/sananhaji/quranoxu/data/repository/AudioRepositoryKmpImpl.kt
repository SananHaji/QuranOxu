package az.sananhaji.quranoxu.data.repository

import az.sananhaji.quranoxu.domain.model.*
import az.sananhaji.quranoxu.domain.repository.AudioRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

expect class PlatformAudioEngine() {
    fun playUrl(url: String, onCompleted: () -> Unit)
    fun pause()
    fun resume()
    fun stop()
    fun setRate(speed: Float)
    fun downloadSurah(
        surahIndex: Int,
        totalVerses: Int,
        language: String,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Boolean
    fun cancelDownload(surahIndex: Int)
    fun isSurahDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean
    fun deleteSurah(surahIndex: Int, language: String): Boolean
    fun getLocalAudioUrlOrRemote(surahIndex: Int, verseNumber: Int, language: String, remoteUrl: String): String
}

class AudioRepositoryKmpImpl(
    private val audioEngine: PlatformAudioEngine = PlatformAudioEngine()
) : AudioRepositoryContract {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val _audioStateFlow = MutableStateFlow(AudioStateEntity())
    override val audioStateFlow: StateFlow<AudioStateEntity> = _audioStateFlow.asStateFlow()

    private val _downloadStatusFlow = MutableStateFlow<Map<Int, SurahDownloadStatus>>(emptyMap())
    override val downloadStatusFlow: StateFlow<Map<Int, SurahDownloadStatus>> = _downloadStatusFlow.asStateFlow()

    private var sleepTimerJob: Job? = null

    private fun getAudioUrl(surahIndex: Int, verseNumber: Int, language: String): String {
        val sIdx = surahIndex.toString().padStart(3, '0')
        val vNum = verseNumber.toString().padStart(3, '0')
        return if (language == "azerbaijani") {
            "https://everyayah.com/data/translations/azerbaijani/balayev/$sIdx$vNum.mp3"
        } else {
            "https://everyayah.com/data/Alafasy_128kbps/$sIdx$vNum.mp3"
        }
    }

    override fun playVerse(surahIndex: Int, verseNumber: Int, totalVerses: Int, surahName: String, audioLanguage: String) {
        val remoteUrl = getAudioUrl(surahIndex, verseNumber, audioLanguage)
        val effectiveUrl = audioEngine.getLocalAudioUrlOrRemote(surahIndex, verseNumber, audioLanguage, remoteUrl)
        val isOffline = effectiveUrl != remoteUrl

        _audioStateFlow.value = AudioStateEntity(
            isPlaying = true,
            isBuffering = false,
            surahIndex = surahIndex,
            surahName = surahName,
            verseNumber = verseNumber,
            totalVerses = totalVerses,
            audioLanguage = audioLanguage,
            isOfflineAvailable = isOffline,
            playbackSpeed = _audioStateFlow.value.playbackSpeed,
            remainingSleepTimerSeconds = _audioStateFlow.value.remainingSleepTimerSeconds
        )

        audioEngine.playUrl(effectiveUrl) {
            scope.launch {
                println("[AudioDebug] audioEngine onCompleted: surah=$surahIndex, verse=$verseNumber, total=$totalVerses")
                val current = _audioStateFlow.value
                if (current.isPlaying && current.surahIndex == surahIndex && current.verseNumber == verseNumber) {
                    if (verseNumber < totalVerses) {
                        println("[AudioDebug] Auto-advancing to verse ${verseNumber + 1}")
                        playVerse(surahIndex, verseNumber + 1, totalVerses, surahName, audioLanguage)
                    } else {
                        println("[AudioDebug] End of surah reached, stopping")
                        stopAudio()
                    }
                } else {
                    println("[AudioDebug] Skipped advancing: isPlaying=${current.isPlaying}, currentSurah=${current.surahIndex}, currentVerse=${current.verseNumber}")
                }
            }
        }
    }

    override fun playSurah(surahIndex: Int, totalVerses: Int, surahName: String, audioLanguage: String) {
        playVerse(surahIndex, 1, totalVerses, surahName, audioLanguage)
    }

    override fun pauseAudio() {
        audioEngine.pause()
        _audioStateFlow.value = _audioStateFlow.value.copy(isPlaying = false)
    }

    override fun resumeAudio() {
        audioEngine.resume()
        _audioStateFlow.value = _audioStateFlow.value.copy(isPlaying = true)
    }

    override fun stopAudio() {
        audioEngine.stop()
        _audioStateFlow.value = AudioStateEntity(
            playbackSpeed = _audioStateFlow.value.playbackSpeed
        )
    }

    override fun nextVerse() {
        val s = _audioStateFlow.value
        if (s.verseNumber < s.totalVerses) {
            playVerse(s.surahIndex, s.verseNumber + 1, s.totalVerses, s.surahName, s.audioLanguage)
        }
    }

    override fun previousVerse() {
        val s = _audioStateFlow.value
        if (s.verseNumber > 1) {
            playVerse(s.surahIndex, s.verseNumber - 1, s.totalVerses, s.surahName, s.audioLanguage)
        }
    }

    override fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = minutes * 60L)
        sleepTimerJob = scope.launch {
            var rem = minutes * 60L
            while (rem > 0) {
                delay(1000)
                rem--
                _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = rem)
            }
            stopAudio()
            _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = 0L)
        }
    }

    override fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = 0L)
    }

    override fun setPlaybackSpeed(speed: Float) {
        audioEngine.setRate(speed)
        _audioStateFlow.value = _audioStateFlow.value.copy(playbackSpeed = speed)
    }

    override suspend fun downloadSurah(
        surahIndex: Int,
        totalVerses: Int,
        language: String,
        onProgress: ((downloaded: Int, total: Int) -> Unit)?
    ): Boolean {
        val current = _downloadStatusFlow.value.toMutableMap()
        current[surahIndex] = SurahDownloadStatus(
            surahIndex = surahIndex,
            isDownloading = true,
            downloadedVerses = 0,
            totalVerses = totalVerses,
            progress = 0f
        )
        _downloadStatusFlow.value = current

        return withContext(Dispatchers.Default) {
            val success = audioEngine.downloadSurah(surahIndex, totalVerses, language) { downloaded, total ->
                if (downloaded < total) {
                    scope.launch {
                        val updated = _downloadStatusFlow.value.toMutableMap()
                        updated[surahIndex] = SurahDownloadStatus(
                            surahIndex = surahIndex,
                            isDownloading = true,
                            isDownloaded = false,
                            downloadedVerses = downloaded,
                            totalVerses = total,
                            progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                        )
                        _downloadStatusFlow.value = updated
                        onProgress?.invoke(downloaded, total)
                    }
                }
            }

            scope.launch {
                val finalMap = _downloadStatusFlow.value.toMutableMap()
                if (success) {
                    finalMap[surahIndex] = SurahDownloadStatus(
                        surahIndex = surahIndex,
                        isDownloaded = true,
                        isDownloading = false,
                        downloadedVerses = totalVerses,
                        totalVerses = totalVerses,
                        progress = 1.0f
                    )
                } else {
                    finalMap[surahIndex] = SurahDownloadStatus(
                        surahIndex = surahIndex,
                        isDownloaded = false,
                        isDownloading = false,
                        totalVerses = totalVerses
                    )
                }
                _downloadStatusFlow.value = finalMap
            }
            success
        }
    }

    override fun cancelDownloadSurah(surahIndex: Int, totalVerses: Int, language: String) {
        audioEngine.cancelDownload(surahIndex)
        val map = _downloadStatusFlow.value.toMutableMap()
        map[surahIndex] = SurahDownloadStatus(surahIndex = surahIndex, totalVerses = totalVerses)
        _downloadStatusFlow.value = map
    }

    override fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): SurahDownloadStatus {
        val cached = _downloadStatusFlow.value[surahIndex]
        if (cached != null && (cached.isDownloading || cached.isDownloaded)) {
            return cached
        }
        val isDownloaded = audioEngine.isSurahDownloaded(surahIndex, totalVerses, language)
        return SurahDownloadStatus(
            surahIndex = surahIndex,
            isDownloaded = isDownloaded,
            isDownloading = false,
            totalVerses = totalVerses,
            progress = if (isDownloaded) 1f else 0f
        )
    }

    override fun isSurahFullyDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean {
        return audioEngine.isSurahDownloaded(surahIndex, totalVerses, language)
    }

    override fun deleteSurahAudio(surahIndex: Int, language: String): Boolean {
        val res = audioEngine.deleteSurah(surahIndex, language)
        val map = _downloadStatusFlow.value.toMutableMap()
        map.remove(surahIndex)
        _downloadStatusFlow.value = map
        return res
    }

    override fun getCacheInfo(): AudioCacheInfo = AudioCacheInfo(0L, "0 MB", 0)
    override fun clearAllAudioCache(): Boolean = true
}
