package az.sananhaji.quranoxu.domain.usecase

import az.sananhaji.quranoxu.domain.model.AudioStateEntity
import az.sananhaji.quranoxu.domain.model.BookmarkEntity
import az.sananhaji.quranoxu.domain.model.SupportedLanguageEntity
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.UserNoteEntity
import az.sananhaji.quranoxu.domain.model.VerseEntity
import az.sananhaji.quranoxu.domain.repository.AudioRepositoryContract
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import kotlinx.coroutines.flow.StateFlow

class GetSurahsUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(): List<SurahEntity> = repository.getAllSurahs()
}

class GetSurahDetailUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(surahIndex: Int): Pair<SurahEntity?, List<VerseEntity>> =
        repository.getSurahWithVerses(surahIndex)
}

class GetSupportedLanguagesUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(): List<SupportedLanguageEntity> =
        repository.getSupportedLanguages()
}

class ToggleBookmarkUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(surahIndex: Int, verseNumber: Int, surahName: String): Boolean =
        repository.toggleBookmark(surahIndex, verseNumber, surahName)
}

class SaveNoteUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String) =
        repository.saveNote(surahIndex, verseNumber, surahName, noteText)
}

class DeleteNoteUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(surahIndex: Int, verseNumber: Int) =
        repository.deleteNote(surahIndex, verseNumber)
}

class GetBookmarksUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(): List<BookmarkEntity> = repository.getAllBookmarks()
}

class GetNotesUseCase(private val repository: QuranRepositoryContract) {
    suspend operator fun invoke(): List<UserNoteEntity> = repository.getAllNotes()
}

class ControlAudioUseCase(private val audioRepository: AudioRepositoryContract) {
    val audioState: StateFlow<AudioStateEntity> get() = audioRepository.audioStateFlow

    fun playVerse(surahIndex: Int, verseNumber: Int, totalVerses: Int, surahName: String, audioLanguage: String = "arabic") {
        audioRepository.playVerse(surahIndex, verseNumber, totalVerses, surahName, audioLanguage)
    }

    fun playSurah(surahIndex: Int, totalVerses: Int, surahName: String, audioLanguage: String = "arabic") {
        audioRepository.playSurah(surahIndex, totalVerses, surahName, audioLanguage)
    }

    fun pause() {
        audioRepository.pauseAudio()
    }

    fun resume() {
        audioRepository.resumeAudio()
    }

    fun stop() {
        audioRepository.stopAudio()
    }

    fun nextVerse() {
        audioRepository.nextVerse()
    }

    fun previousVerse() {
        audioRepository.previousVerse()
    }

    fun setSleepTimer(minutes: Int) {
        audioRepository.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        audioRepository.cancelSleepTimer()
    }

    fun setPlaybackSpeed(speed: Float) {
        audioRepository.setPlaybackSpeed(speed)
    }

    val downloadStatusFlow: StateFlow<Map<Int, az.sananhaji.quranoxu.domain.model.SurahDownloadStatus>>
        get() = audioRepository.downloadStatusFlow

    suspend fun downloadSurah(surahIndex: Int, totalVerses: Int, language: String): Boolean {
        return audioRepository.downloadSurah(surahIndex, totalVerses, language)
    }

    fun cancelDownloadSurah(surahIndex: Int, totalVerses: Int = 0, language: String = "arabic") {
        audioRepository.cancelDownloadSurah(surahIndex, totalVerses, language)
    }

    fun getSurahDownloadStatus(surahIndex: Int, totalVerses: Int, language: String): az.sananhaji.quranoxu.domain.model.SurahDownloadStatus {
        return audioRepository.getSurahDownloadStatus(surahIndex, totalVerses, language)
    }

    fun deleteSurahAudio(surahIndex: Int, language: String): Boolean {
        return audioRepository.deleteSurahAudio(surahIndex, language)
    }

    fun getCacheInfo(): az.sananhaji.quranoxu.domain.model.AudioCacheInfo {
        return audioRepository.getCacheInfo()
    }

    fun clearAllAudioCache(): Boolean {
        return audioRepository.clearAllAudioCache()
    }
}
