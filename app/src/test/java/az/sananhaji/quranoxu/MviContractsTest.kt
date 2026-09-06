package az.sananhaji.quranoxu

import az.sananhaji.quranoxu.presentation.mvi.SurahDetailIntent
import az.sananhaji.quranoxu.presentation.mvi.SurahListIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MviContractsTest {

    @Test
    fun testCancelDownloadSurahIntent() {
        val intent = SurahListIntent.CancelDownloadSurah(surahIndex = 1, totalVerses = 7)
        assertEquals(1, intent.surahIndex)
        assertEquals(7, intent.totalVerses)
    }

    @Test
    fun testCancelCurrentSurahDownloadIntent() {
        val intent = SurahDetailIntent.CancelCurrentSurahDownload
        assertTrue(intent is SurahDetailIntent)
    }

    @Test
    fun testAudioStatePlaybackSpeed() {
        val audioState = az.sananhaji.quranoxu.domain.model.AudioStateEntity(playbackSpeed = 1.5f)
        assertEquals(1.5f, audioState.playbackSpeed, 0.001f)
    }
}
