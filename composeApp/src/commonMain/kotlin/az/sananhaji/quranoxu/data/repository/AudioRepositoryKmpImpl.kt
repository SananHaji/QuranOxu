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

expect class PlatformAudioEngine() {
    fun playUrl(url: String, onCompleted: () -> Unit)
    fun pause()
    fun resume()
    fun stop()
    fun setRate(speed: Float)
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
        val url = getAudioUrl(surahIndex, verseNumber, audioLanguage)
        _audioStateFlow.value = AudioStateEntity(
            isPlaying = true,
            isBuffering = false,
            surahIndex = surahIndex,
            surahName = surahName,
            verseNumber = verseNumber,
            totalVerses = totalVerses,
            audioLanguage = audioLanguage,
            playbackSpeed = _audioStateFlow.value.playbackSpeed
        )
        audioEngine.playUrl(url) {
            scope.launch {
                if (verseNumber < totalVerses) {
                    playVerse(surahIndex, verseNumber + 1, totalVerses, surahName, audioLanguage)
                } else {
                    stopAudio()
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
        _audioStateFlow.value = _audioStateFlow.value.copy(isPlaying = false)
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

    override suspend fun downloadSurah(surahIndex: Int, totalVerses: Int, language: String, onProgress: ((downloaded: Int, total: Int) -> Unit)?): Boolean = true
    override fun cancelDownloadSurah(surahIndex: Int, totalVerses: Int, language: String) {}
    override fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): SurahDownloadStatus = SurahDownloadStatus(surahIndex = surahIndex, totalVerses = totalVerses)
    override fun isSurahFullyDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean = false
    override fun deleteSurahAudio(surahIndex: Int, language: String): Boolean = true
    override fun getCacheInfo(): AudioCacheInfo = AudioCacheInfo(0L, "0 MB", 0)
    override fun clearAllAudioCache(): Boolean = true
}
