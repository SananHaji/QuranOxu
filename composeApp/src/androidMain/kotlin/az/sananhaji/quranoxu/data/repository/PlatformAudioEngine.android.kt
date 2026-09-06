package az.sananhaji.quranoxu.data.repository

import android.media.AudioAttributes
import android.media.MediaPlayer

actual class PlatformAudioEngine actual constructor() {
    private var mediaPlayer: MediaPlayer? = null

    actual fun playUrl(url: String, onCompleted: () -> Unit) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnCompletionListener { onCompleted() }
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {}
    }

    actual fun resume() {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {}
    }

    actual fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {}
    }

    actual fun setRate(speed: Float) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            }
        } catch (e: Exception) {}
    }
}
