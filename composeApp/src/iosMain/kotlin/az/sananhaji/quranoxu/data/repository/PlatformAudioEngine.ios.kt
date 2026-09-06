@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package az.sananhaji.quranoxu.data.repository

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.Foundation.NSURL
import platform.darwin.NSObject

actual class PlatformAudioEngine actual constructor() {
    private var player: AVPlayer? = null

    init {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Throwable) {
            // Audio session setup
        }
    }

    actual fun playUrl(url: String, onCompleted: () -> Unit) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val playerItem = AVPlayerItem.playerItemWithURL(nsUrl)
        player = AVPlayer.playerWithPlayerItem(playerItem)
        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun resume() {
        player?.play()
    }

    actual fun stop() {
        player?.pause()
        player = null
    }

    actual fun setRate(speed: Float) {
        player?.setRate(speed)
    }
}
