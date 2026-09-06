@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package az.sananhaji.quranoxu.data.repository

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.Foundation.*
import platform.darwin.NSObject

actual class PlatformAudioEngine actual constructor() {
    private var player: AVPlayer? = null
    private var endObserver: Any? = null
    private val cancelledSurahs = mutableSetOf<Int>()

    init {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Throwable) {
            // Audio session setup
        }
    }

    private fun getCacheDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cachePath = paths.firstOrNull() as? String ?: NSTemporaryDirectory()
        val audioPath = "$cachePath/audio_cache"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(audioPath)) {
            fileManager.createDirectoryAtPath(audioPath, withIntermediateDirectories = true, attributes = null, error = null)
        }
        return audioPath
    }

    private fun getVerseFilePath(surahIndex: Int, verseNumber: Int, language: String): String {
        val dir = "${getCacheDirectory()}/$language/$surahIndex"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        val vStr = verseNumber.toString().padStart(3, '0')
        val sStr = surahIndex.toString().padStart(3, '0')
        return "$dir/$sStr$vStr.mp3"
    }

    actual fun playUrl(url: String, onCompleted: () -> Unit) {
        val nsUrl = if (url.startsWith("/")) {
            NSURL.fileURLWithPath(url)
        } else {
            NSURL.URLWithString(url)
        } ?: return

        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null

        val playerItem = AVPlayerItem.playerItemWithURL(nsUrl)

        if (player == null) {
            player = AVPlayer(playerItem = playerItem)
        } else {
            player?.replaceCurrentItemWithPlayerItem(playerItem)
        }

        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            println("[AudioDebug] AVPlayerItemDidPlayToEndTimeNotification triggered! Invoking onCompleted()")
            onCompleted()
        }

        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun resume() {
        player?.play()
    }

    actual fun stop() {
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        player?.pause()
        player?.replaceCurrentItemWithPlayerItem(null)
    }

    actual fun setRate(speed: Float) {
        player?.setRate(speed)
    }

    actual fun downloadSurah(
        surahIndex: Int,
        totalVerses: Int,
        language: String,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Boolean {
        cancelledSurahs.remove(surahIndex)
        val fileManager = NSFileManager.defaultManager

        for (v in 1..totalVerses) {
            if (cancelledSurahs.contains(surahIndex)) {
                return false
            }
            val targetPath = getVerseFilePath(surahIndex, v, language)
            val exists = fileManager.fileExistsAtPath(targetPath)
            val size = if (exists) {
                val attrs = fileManager.attributesOfItemAtPath(targetPath, null)
                (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            } else 0L

            if (size < 500L) {
                val sIdx = surahIndex.toString().padStart(3, '0')
                val vNum = v.toString().padStart(3, '0')
                val urlString = if (language == "azerbaijani") {
                    "https://everyayah.com/data/translations/azerbaijani/balayev/$sIdx$vNum.mp3"
                } else {
                    "https://everyayah.com/data/Alafasy_128kbps/$sIdx$vNum.mp3"
                }
                val nsUrl = NSURL.URLWithString(urlString) ?: continue
                val data = NSData.dataWithContentsOfURL(nsUrl)
                if (data != null && data.length > 500u) {
                    data.writeToFile(targetPath, true)
                } else {
                    return false
                }
            }
            onProgress(v, totalVerses)
        }
        return true
    }

    actual fun cancelDownload(surahIndex: Int) {
        cancelledSurahs.add(surahIndex)
    }

    actual fun isSurahDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean {
        if (totalVerses <= 0) return false
        val fileManager = NSFileManager.defaultManager
        for (v in 1..totalVerses) {
            val path = getVerseFilePath(surahIndex, v, language)
            if (!fileManager.fileExistsAtPath(path)) return false
            val attrs = fileManager.attributesOfItemAtPath(path, null) ?: return false
            val size = (attrs[NSFileSize] as? NSNumber)?.longValue ?: 0L
            if (size < 500L) return false
        }
        return true
    }

    actual fun deleteSurah(surahIndex: Int, language: String): Boolean {
        val dir = "${getCacheDirectory()}/$language/$surahIndex"
        val fileManager = NSFileManager.defaultManager
        return if (fileManager.fileExistsAtPath(dir)) {
            fileManager.removeItemAtPath(dir, null)
        } else true
    }

    actual fun getLocalAudioUrlOrRemote(surahIndex: Int, verseNumber: Int, language: String, remoteUrl: String): String {
        val path = getVerseFilePath(surahIndex, verseNumber, language)
        val fileManager = NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(path)) {
            val attrs = fileManager.attributesOfItemAtPath(path, null)
            val size = (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            if (size > 500L) {
                return path
            }
        }
        return remoteUrl
    }
}
