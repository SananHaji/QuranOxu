package az.sananhaji.quranoxu

import az.sananhaji.quranoxu.data.repository.QuranRepositoryKmpImpl
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuranRepositoryTest {

    @Test
    fun testGetAllSurahs() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)
        val surahs = repo.getAllSurahs()

        assertEquals(114, surahs.size, "Must contain exactly 114 surahs")
        val fatiha = surahs.first()
        assertEquals(1, fatiha.index)
        assertEquals(7, fatiha.verseCount)
        assertEquals("Fatihə surəsi", fatiha.nameAzeri)

        val nas = surahs.last()
        assertEquals(114, nas.index)
        assertEquals(6, nas.verseCount)
        assertEquals("Nas surəsi", nas.nameAzeri)
    }

    @Test
    fun testGetSurahWithVerses() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)
        val (surah, verses) = repo.getSurahWithVerses(1)

        assertNotNull(surah)
        assertEquals(1, surah.index)
        assertEquals(7, verses.size)
        assertEquals("Fatihə surəsi", surah.nameAzeri)
    }

    @Test
    fun testBookmarkSavingAndDeletion() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)

        assertTrue(repo.getAllBookmarks().isEmpty())

        repo.toggleBookmark(1, 1, "Fatihə")
        val bookmarks = repo.getAllBookmarks()
        assertEquals(1, bookmarks.size)
        assertEquals(1, bookmarks.first().surahIndex)
        assertEquals(1, bookmarks.first().verseNumber)

        repo.toggleBookmark(1, 1, "Fatihə")
        assertTrue(repo.getAllBookmarks().isEmpty())
    }

    @Test
    fun testNotesSavingAndDeletion() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)

        assertTrue(repo.getAllNotes().isEmpty())

        repo.saveNote(1, 1, "Fatihə", "Şəxsi qeyd")
        val notes = repo.getAllNotes()
        assertEquals(1, notes.size)
        assertEquals("Şəxsi qeyd", notes.first().noteText)

        repo.deleteNote(1, 1)
        assertTrue(repo.getAllNotes().isEmpty())
    }

    @Test
    fun testReadingProgress() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)

        repo.markVerseRead(1, 1, "Fatihə")
        repo.markVerseRead(1, 2, "Fatihə")
        var progress = repo.getOverallProgress()
        assertEquals(2, progress.totalReadVerses)

        repo.resetSurahReadProgress(1)
        progress = repo.getOverallProgress()
        assertEquals(0, progress.totalReadVerses)
    }

    @Test
    fun testSearchVerses() = runTest {
        val settings = MapSettings()
        val repo = QuranRepositoryKmpImpl(settings)

        val results = repo.searchVerses("Allah")
        assertTrue(results.isNotEmpty(), "Search for 'Allah' should return verses")
    }
}
