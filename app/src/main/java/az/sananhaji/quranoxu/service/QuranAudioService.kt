package az.sananhaji.quranoxu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import az.sananhaji.quranoxu.MainActivity
import az.sananhaji.quranoxu.data.audio.AudioCacheManager
import az.sananhaji.quranoxu.data.db.QuranDatabaseHelper
import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranAudioService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var mediaPlayer: MediaPlayer? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var audioCacheManager: AudioCacheManager
    private lateinit var dbHelper: QuranDatabaseHelper
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var preBufferJob: kotlinx.coroutines.Job? = null

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): QuranAudioService = this@QuranAudioService
    }

    companion object {
        const val CHANNEL_ID = "QuranAudioServiceChannel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_VERSE = "az.sananhaji.quranoxu.ACTION_PLAY_VERSE"
        const val ACTION_PAUSE = "az.sananhaji.quranoxu.ACTION_PAUSE"
        const val ACTION_RESUME = "az.sananhaji.quranoxu.ACTION_RESUME"
        const val ACTION_STOP = "az.sananhaji.quranoxu.ACTION_STOP"
        const val ACTION_NEXT = "az.sananhaji.quranoxu.ACTION_NEXT"
        const val ACTION_PREV = "az.sananhaji.quranoxu.ACTION_PREV"
        const val ACTION_SET_SLEEP_TIMER = "az.sananhaji.quranoxu.ACTION_SET_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "az.sananhaji.quranoxu.ACTION_CANCEL_SLEEP_TIMER"

        const val EXTRA_SURAH_INDEX = "EXTRA_SURAH_INDEX"
        const val EXTRA_VERSE_NUMBER = "EXTRA_VERSE_NUMBER"
        const val EXTRA_TOTAL_VERSES = "EXTRA_TOTAL_VERSES"
        const val EXTRA_SURAH_NAME = "EXTRA_SURAH_NAME"
        const val EXTRA_AUDIO_LANGUAGE = "EXTRA_AUDIO_LANGUAGE"
        const val EXTRA_SLEEP_MINUTES = "EXTRA_SLEEP_MINUTES"

        private val _audioStateFlow = MutableStateFlow(AudioStateEntity())
        val audioStateFlow: StateFlow<AudioStateEntity> = _audioStateFlow.asStateFlow()

        fun playVerse(
            context: Context,
            surahIndex: Int,
            verseNumber: Int,
            totalVerses: Int,
            surahName: String,
            audioLanguage: String = "arabic"
        ) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_PLAY_VERSE
                putExtra(EXTRA_SURAH_INDEX, surahIndex)
                putExtra(EXTRA_VERSE_NUMBER, verseNumber)
                putExtra(EXTRA_TOTAL_VERSES, totalVerses)
                putExtra(EXTRA_SURAH_NAME, surahName)
                putExtra(EXTRA_AUDIO_LANGUAGE, audioLanguage)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_PAUSE }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_RESUME }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }

        fun nextVerse(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_NEXT }
            context.startService(intent)
        }

        fun previousVerse(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_PREV }
            context.startService(intent)
        }

        fun setSleepTimer(context: Context, minutes: Int) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_SET_SLEEP_TIMER
                putExtra(EXTRA_SLEEP_MINUTES, minutes)
            }
            context.startService(intent)
        }

        fun cancelSleepTimer(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply { action = ACTION_CANCEL_SLEEP_TIMER }
            context.startService(intent)
        }
    }

    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    override fun onCreate() {
        super.onCreate()
        audioCacheManager = AudioCacheManager(applicationContext)
        dbHelper = QuranDatabaseHelper(applicationContext)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_VERSE -> {
                val surahIndex = intent.getIntExtra(EXTRA_SURAH_INDEX, 1)
                val verseNumber = intent.getIntExtra(EXTRA_VERSE_NUMBER, 1)
                val totalVerses = intent.getIntExtra(EXTRA_TOTAL_VERSES, 1)
                val surahName = intent.getStringExtra(EXTRA_SURAH_NAME) ?: "Surə $surahIndex"
                val audioLanguage = intent.getStringExtra(EXTRA_AUDIO_LANGUAGE) ?: "arabic"
                startPlayback(surahIndex, verseNumber, totalVerses, surahName, audioLanguage)
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_STOP -> stopPlayback()
            ACTION_NEXT -> playNextVerse()
            ACTION_PREV -> playPreviousVerse()
            ACTION_SET_SLEEP_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_SLEEP_MINUTES, 15)
                startSleepTimer(minutes)
            }
            ACTION_CANCEL_SLEEP_TIMER -> cancelSleepTimer()
        }
        return START_STICKY
    }

    private fun acquireLocks() {
        try {
            if (wakeLock == null) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuranOxu:AudioPlayLock").apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.let {
                if (!it.isHeld) it.acquire(24 * 60 * 60 * 1000L) // 24 hours max
            }

            if (wifiLock == null) {
                val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager.WIFI_MODE_FULL
                }
                wifiLock = wm.createWifiLock(mode, "QuranOxu:AudioWifiLock").apply {
                    setReferenceCounted(false)
                }
            }
            wifiLock?.let {
                if (!it.isHeld) it.acquire()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseLocks() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wifiLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPlayback(surahIndex: Int, verseNumber: Int, totalVerses: Int, surahName: String, audioLanguage: String) {
        acquireLocks()
        serviceScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null

                dbHelper.markVerseRead(surahIndex, verseNumber, surahName)

                val isCurrentlyCached = audioCacheManager.isVerseCached(surahIndex, verseNumber, audioLanguage)
                val initialState = AudioStateEntity(
                    isPlaying = true,
                    surahIndex = surahIndex,
                    surahName = surahName,
                    verseNumber = verseNumber,
                    totalVerses = totalVerses,
                    audioLanguage = audioLanguage,
                    isBuffering = !isCurrentlyCached,
                    isOfflineAvailable = isCurrentlyCached
                )
                _audioStateFlow.value = initialState
                startForeground(NOTIFICATION_ID, buildNotification(initialState))

                // Get from local disk or download directly to local storage
                val localAudioFile = audioCacheManager.getOrDownloadVerseFile(surahIndex, verseNumber, audioLanguage)

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

                    if (localAudioFile != null && localAudioFile.exists()) {
                        setDataSource(localAudioFile.absolutePath)
                    } else {
                        // Fallback to direct URL if offline download failed
                        val audioUrl = audioCacheManager.getVerseAudioUrl(surahIndex, verseNumber, audioLanguage)
                        setDataSource(audioUrl)
                    }

                    setOnCompletionListener(this@QuranAudioService)
                    setOnErrorListener(this@QuranAudioService)
                    prepare()
                    start()
                }
                mediaPlayer = player

                val playingState = initialState.copy(
                    isPlaying = true,
                    isBuffering = false,
                    isOfflineAvailable = localAudioFile != null && localAudioFile.exists()
                )
                _audioStateFlow.value = playingState
                updateNotification(playingState)

                // Trigger lookahead pre-buffering: next verses + next 3 surahs!
                triggerLookaheadPrebuffering(surahIndex, verseNumber, totalVerses, audioLanguage)

            } catch (e: Exception) {
                e.printStackTrace()
                _audioStateFlow.value = _audioStateFlow.value.copy(isPlaying = false, isBuffering = false)
            }
        }
    }

    private fun triggerLookaheadPrebuffering(surahIndex: Int, currentVerse: Int, totalVerses: Int, audioLanguage: String) {
        preBufferJob?.cancel()
        preBufferJob = serviceScope.launch(Dispatchers.IO) {
            try {
                // 1. Immediately pre-buffer the next 5 verses of the current surah
                audioCacheManager.preBufferNextVerses(surahIndex, currentVerse, totalVerses, 5, audioLanguage)

                // 2. Pre-buffer remaining verses of current surah
                if (currentVerse + 5 < totalVerses) {
                    audioCacheManager.preBufferNextVerses(surahIndex, currentVerse + 5, totalVerses, totalVerses, audioLanguage)
                }

                // 3. Pre-buffer the next 2-3 Surahs in advance as requested by user
                val maxSurah = minOf(surahIndex + 3, 114)
                for (nextSurahIndex in (surahIndex + 1)..maxSurah) {
                    val sEntity = dbHelper.getSurahByIndex(nextSurahIndex)
                    val sVerseCount = sEntity?.verseCount ?: 0
                    if (sVerseCount > 0) {
                        audioCacheManager.preBufferSurah(nextSurahIndex, sVerseCount, audioLanguage)
                    }
                }
            } catch (e: Exception) {
                // Network dropped or stopped; ignore silently
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        releaseLocks()
        val updatedState = _audioStateFlow.value.copy(isPlaying = false)
        _audioStateFlow.value = updatedState
        updateNotification(updatedState)
    }

    private fun resumePlayback() {
        acquireLocks()
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
        val current = _audioStateFlow.value
        if (current.surahIndex > 0 && current.verseNumber > 0) {
            triggerLookaheadPrebuffering(current.surahIndex, current.verseNumber, current.totalVerses, current.audioLanguage)
        }
        val updatedState = _audioStateFlow.value.copy(isPlaying = true)
        _audioStateFlow.value = updatedState
        updateNotification(updatedState)
    }

    private fun stopPlayback() {
        preBufferJob?.cancel()
        cancelSleepTimer()
        releaseLocks()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _audioStateFlow.value = AudioStateEntity()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60L
        var remaining = totalSeconds

        _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = remaining)

        sleepTimerJob = serviceScope.launch(Dispatchers.Default) {
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000L)
                remaining--
                _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = remaining)
            }
            withContext(Dispatchers.Main) {
                pausePlayback()
                _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = 0L)
            }
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _audioStateFlow.value = _audioStateFlow.value.copy(remainingSleepTimerSeconds = 0L)
    }

    private fun playNextVerse() {
        val current = _audioStateFlow.value
        if (current.verseNumber < current.totalVerses) {
            startPlayback(current.surahIndex, current.verseNumber + 1, current.totalVerses, current.surahName, current.audioLanguage)
        } else {
            stopPlayback()
        }
    }

    private fun playPreviousVerse() {
        val current = _audioStateFlow.value
        if (current.verseNumber > 1) {
            startPlayback(current.surahIndex, current.verseNumber - 1, current.totalVerses, current.surahName, current.audioLanguage)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val current = _audioStateFlow.value
        if (current.isPlaying && current.verseNumber < current.totalVerses) {
            startPlayback(current.surahIndex, current.verseNumber + 1, current.totalVerses, current.surahName, current.audioLanguage)
        } else {
            stopPlayback()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        stopPlayback()
        return true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quran Audio Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Quran verse audio playback notification"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(state: AudioStateEntity): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Action
        val prevIntent = Intent(this, QuranAudioService::class.java).apply { action = ACTION_PREV }
        val prevPending = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE)
        val prevAction = NotificationCompat.Action(android.R.drawable.ic_media_previous, "Əvvəlki", prevPending)

        // Pause/Resume Action
        val pauseOrResumeAction = if (state.isPlaying) {
            val pauseIntent = Intent(this, QuranAudioService::class.java).apply { action = ACTION_PAUSE }
            val pausePending = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pauza", pausePending)
        } else {
            val resumeIntent = Intent(this, QuranAudioService::class.java).apply { action = ACTION_RESUME }
            val resumePending = PendingIntent.getService(this, 3, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Davam et", resumePending)
        }

        // Next Action
        val nextIntent = Intent(this, QuranAudioService::class.java).apply { action = ACTION_NEXT }
        val nextPending = PendingIntent.getService(this, 4, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        val nextAction = NotificationCompat.Action(android.R.drawable.ic_media_next, "Növbəti", nextPending)

        // Stop Action
        val stopIntent = Intent(this, QuranAudioService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 5, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopAction = NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, "Dayandır", stopPending)

        val langLabel = if (state.audioLanguage == "azerbaijani") "(Azərbaycan səsli tərcümə - Rasim Balayev)" else "(Ərəbcə - Əlafəsi)"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${state.surahName} $langLabel")
            .setContentText("Ayə ${state.verseNumber} / ${state.totalVerses}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(prevAction)
            .addAction(pauseOrResumeAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setOngoing(state.isPlaying)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(state: AudioStateEntity) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    override fun onDestroy() {
        super.onDestroy()
        preBufferJob?.cancel()
        releaseLocks()
        serviceJob.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
