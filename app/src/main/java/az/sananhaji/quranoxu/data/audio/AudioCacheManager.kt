package az.sananhaji.quranoxu.data.audio

import android.content.Context
import az.sananhaji.quranoxu.domain.model.AudioCacheInfo
import az.sananhaji.quranoxu.domain.model.SurahDownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class AudioCacheManager(private val context: Context) {

    private val baseCacheDir: File by lazy {
        File(context.filesDir, "audio_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    private val _downloadStatusFlow = MutableStateFlow<Map<Int, SurahDownloadStatus>>(emptyMap())
    val downloadStatusFlow: StateFlow<Map<Int, SurahDownloadStatus>> = _downloadStatusFlow.asStateFlow()

    fun getVerseAudioUrl(surahIndex: Int, verseNumber: Int, language: String): String {
        return if (language == "azerbaijani") {
            String.format(Locale.US, "https://everyayah.com/data/translations/azerbaijani/balayev/%03d%03d.mp3", surahIndex, verseNumber)
        } else {
            String.format(Locale.US, "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", surahIndex, verseNumber)
        }
    }

    fun getSurahDirectory(surahIndex: Int, language: String): File {
        val langDir = File(baseCacheDir, language)
        val surahDir = File(langDir, surahIndex.toString())
        if (!surahDir.exists()) {
            surahDir.mkdirs()
        }
        return surahDir
    }

    fun getVerseFile(surahIndex: Int, verseNumber: Int, language: String): File {
        val surahDir = getSurahDirectory(surahIndex, language)
        return File(surahDir, String.format(Locale.US, "%03d%03d.mp3", surahIndex, verseNumber))
    }

    fun isVerseCached(surahIndex: Int, verseNumber: Int, language: String): Boolean {
        val file = getVerseFile(surahIndex, verseNumber, language)
        return file.exists() && file.length() > 500L // valid mp3 header threshold
    }

    suspend fun getOrDownloadVerseFile(surahIndex: Int, verseNumber: Int, language: String): File? = withContext(Dispatchers.IO) {
        val targetFile = getVerseFile(surahIndex, verseNumber, language)
        if (isVerseCached(surahIndex, verseNumber, language)) {
            return@withContext targetFile
        }

        val success = downloadSingleVerse(surahIndex, verseNumber, language, targetFile)
        if (success && targetFile.exists()) {
            targetFile
        } else {
            null
        }
    }

    private fun downloadSingleVerse(surahIndex: Int, verseNumber: Int, language: String, targetFile: File): Boolean {
        val audioUrl = getVerseAudioUrl(surahIndex, verseNumber, language)
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

        var connection: HttpURLConnection? = null
        try {
            val url = URL(audioUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "QuranOxuApp/1.0")
            }

            if (connection.responseCode !in 200..299) {
                return false
            }

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 500L) {
                if (targetFile.exists()) targetFile.delete()
                return tempFile.renameTo(targetFile)
            } else {
                if (tempFile.exists()) tempFile.delete()
                return false
            }
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            return false
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun downloadSurah(
        surahIndex: Int,
        totalVerses: Int,
        language: String,
        onProgress: ((downloaded: Int, total: Int) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        updateDownloadStatus(
            surahIndex,
            SurahDownloadStatus(
                surahIndex = surahIndex,
                isDownloaded = false,
                isDownloading = true,
                downloadedVerses = 0,
                totalVerses = totalVerses,
                progress = 0f
            )
        )

        var downloadedCount = 0
        for (v in 1..totalVerses) {
            val file = getVerseFile(surahIndex, v, language)
            if (isVerseCached(surahIndex, v, language)) {
                downloadedCount++
            } else {
                val ok = downloadSingleVerse(surahIndex, v, language, file)
                if (ok) {
                    downloadedCount++
                }
            }

            val progress = downloadedCount.toFloat() / totalVerses
            updateDownloadStatus(
                surahIndex,
                SurahDownloadStatus(
                    surahIndex = surahIndex,
                    isDownloaded = downloadedCount == totalVerses,
                    isDownloading = downloadedCount < totalVerses,
                    downloadedVerses = downloadedCount,
                    totalVerses = totalVerses,
                    progress = progress,
                    sizeBytes = getSurahDiskSize(surahIndex, language)
                )
            )
            onProgress?.invoke(downloadedCount, totalVerses)
        }

        val isFullyDownloaded = downloadedCount == totalVerses
        updateDownloadStatus(
            surahIndex,
            SurahDownloadStatus(
                surahIndex = surahIndex,
                isDownloaded = isFullyDownloaded,
                isDownloading = false,
                downloadedVerses = downloadedCount,
                totalVerses = totalVerses,
                progress = if (isFullyDownloaded) 1f else (downloadedCount.toFloat() / totalVerses),
                sizeBytes = getSurahDiskSize(surahIndex, language)
            )
        )
        return@withContext isFullyDownloaded
    }

    suspend fun preBufferSurah(surahIndex: Int, totalVerses: Int, language: String) = withContext(Dispatchers.IO) {
        if (surahIndex !in 1..114 || totalVerses <= 0) return@withContext
        for (v in 1..totalVerses) {
            val file = getVerseFile(surahIndex, v, language)
            if (!isVerseCached(surahIndex, v, language)) {
                downloadSingleVerse(surahIndex, v, language, file)
            }
        }
    }

    suspend fun preBufferNextVerses(surahIndex: Int, currentVerse: Int, totalVerses: Int, count: Int, language: String) = withContext(Dispatchers.IO) {
        val endVerse = minOf(currentVerse + count, totalVerses)
        for (v in (currentVerse + 1)..endVerse) {
            val file = getVerseFile(surahIndex, v, language)
            if (!isVerseCached(surahIndex, v, language)) {
                downloadSingleVerse(surahIndex, v, language, file)
            }
        }
    }

    fun isSurahFullyDownloaded(surahIndex: Int, totalVerses: Int, language: String): Boolean {
        if (totalVerses <= 0) return false
        val surahDir = File(File(baseCacheDir, language), surahIndex.toString())
        if (!surahDir.exists() || !surahDir.isDirectory) return false

        var cachedCount = 0
        for (v in 1..totalVerses) {
            val f = File(surahDir, String.format(Locale.US, "%03d%03d.mp3", surahIndex, v))
            if (f.exists() && f.length() > 500L) {
                cachedCount++
            } else {
                return false
            }
        }
        return cachedCount == totalVerses
    }

    fun getSurahDownloadedCount(surahIndex: Int, totalVerses: Int, language: String): Int {
        val surahDir = File(File(baseCacheDir, language), surahIndex.toString())
        if (!surahDir.exists() || !surahDir.isDirectory) return 0
        var cachedCount = 0
        for (v in 1..totalVerses) {
            val f = File(surahDir, String.format(Locale.US, "%03d%03d.mp3", surahIndex, v))
            if (f.exists() && f.length() > 500L) {
                cachedCount++
            }
        }
        return cachedCount
    }

    fun getSurahDiskSize(surahIndex: Int, language: String): Long {
        val surahDir = File(File(baseCacheDir, language), surahIndex.toString())
        if (!surahDir.exists() || !surahDir.isDirectory) return 0L
        return surahDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): SurahDownloadStatus {
        val active = _downloadStatusFlow.value[surahIndex]
        if (active != null && active.isDownloading) {
            return active
        }
        val downloadedCount = getSurahDownloadedCount(surahIndex, totalVerses, language)
        val isFull = totalVerses > 0 && downloadedCount >= totalVerses
        return SurahDownloadStatus(
            surahIndex = surahIndex,
            isDownloaded = isFull,
            isDownloading = false,
            downloadedVerses = downloadedCount,
            totalVerses = totalVerses,
            progress = if (totalVerses > 0) downloadedCount.toFloat() / totalVerses else 0f,
            sizeBytes = getSurahDiskSize(surahIndex, language)
        )
    }

    fun deleteSurahCache(surahIndex: Int, language: String): Boolean {
        val surahDir = File(File(baseCacheDir, language), surahIndex.toString())
        val deleted = if (surahDir.exists()) {
            surahDir.deleteRecursively()
        } else true

        val currentMap = _downloadStatusFlow.value.toMutableMap()
        currentMap.remove(surahIndex)
        _downloadStatusFlow.value = currentMap
        return deleted
    }

    fun getTotalCacheSizeBytes(): Long {
        if (!baseCacheDir.exists()) return 0L
        return baseCacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes <= 0L -> "0 MB"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
            else -> String.format(Locale.US, "%.2f GB", bytes / (1024f * 1024f * 1024f))
        }
    }

    fun getCacheInfo(): AudioCacheInfo {
        val totalBytes = getTotalCacheSizeBytes()
        var downloadedSurahsCount = 0

        val languages = listOf("arabic", "azerbaijani")
        for (lang in languages) {
            val langDir = File(baseCacheDir, lang)
            if (langDir.exists() && langDir.isDirectory) {
                val surahDirs = langDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
                downloadedSurahsCount += surahDirs.count { dir ->
                    val mp3s = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".mp3") }
                    !mp3s.isNullOrEmpty()
                }
            }
        }

        return AudioCacheInfo(
            totalSizeBytes = totalBytes,
            formattedSize = formatBytes(totalBytes),
            downloadedSurahsCount = downloadedSurahsCount
        )
    }

    fun clearAllCache(): Boolean {
        val deleted = if (baseCacheDir.exists()) {
            baseCacheDir.deleteRecursively()
        } else true
        baseCacheDir.mkdirs()
        _downloadStatusFlow.value = emptyMap()
        return deleted
    }

    private fun updateDownloadStatus(surahIndex: Int, status: SurahDownloadStatus) {
        val current = _downloadStatusFlow.value.toMutableMap()
        current[surahIndex] = status
        _downloadStatusFlow.value = current
    }
}
