package az.sananhaji.quranoxu

import az.sananhaji.quranoxu.data.repository.AudioRepositoryKmpImpl
import az.sananhaji.quranoxu.data.repository.PlatformAudioEngine
import az.sananhaji.quranoxu.domain.model.SurahDownloadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioRepositoryTest {

    @Test
    fun testInitialAudioState() {
        val repo = AudioRepositoryKmpImpl()
        val state = repo.audioStateFlow.value
        assertFalse(state.isPlaying)
        assertEquals(0, state.surahIndex)
        assertEquals(0, state.verseNumber)
        assertEquals(1.0f, state.playbackSpeed)
    }

    @Test
    fun testPlayVerseUpdatesState() {
        val repo = AudioRepositoryKmpImpl()
        repo.playVerse(1, 1, 7, "Fatihə", "azerbaijani")
        val state = repo.audioStateFlow.value
        assertTrue(state.isPlaying)
        assertEquals(1, state.surahIndex)
        assertEquals(1, state.verseNumber)
        assertEquals(7, state.totalVerses)
        assertEquals("Fatihə", state.surahName)
    }

    @Test
    fun testPauseAndResumeAudio() {
        val repo = AudioRepositoryKmpImpl()
        repo.playVerse(1, 1, 7, "Fatihə", "azerbaijani")
        assertTrue(repo.audioStateFlow.value.isPlaying)

        repo.pauseAudio()
        assertFalse(repo.audioStateFlow.value.isPlaying)
        assertEquals(1, repo.audioStateFlow.value.verseNumber)

        repo.resumeAudio()
        assertTrue(repo.audioStateFlow.value.isPlaying)
        assertEquals(1, repo.audioStateFlow.value.verseNumber)
    }

    @Test
    fun testStopAudioResetsState() {
        val repo = AudioRepositoryKmpImpl()
        repo.playVerse(1, 1, 7, "Fatihə", "azerbaijani")
        assertTrue(repo.audioStateFlow.value.isPlaying)

        repo.stopAudio()
        val state = repo.audioStateFlow.value
        assertFalse(state.isPlaying)
        assertEquals(0, state.surahIndex)
        assertEquals(0, state.verseNumber)
    }

    @Test
    fun testNextAndPreviousVerse() {
        val repo = AudioRepositoryKmpImpl()
        repo.playVerse(1, 2, 7, "Fatihə", "azerbaijani")
        assertEquals(2, repo.audioStateFlow.value.verseNumber)

        repo.nextVerse()
        assertEquals(3, repo.audioStateFlow.value.verseNumber)

        repo.previousVerse()
        assertEquals(2, repo.audioStateFlow.value.verseNumber)
    }

    @Test
    fun testSetPlaybackSpeed() {
        val repo = AudioRepositoryKmpImpl()
        repo.setPlaybackSpeed(1.5f)
        assertEquals(1.5f, repo.audioStateFlow.value.playbackSpeed)

        repo.playVerse(1, 1, 7, "Fatihə", "azerbaijani")
        assertEquals(1.5f, repo.audioStateFlow.value.playbackSpeed)
    }
}
