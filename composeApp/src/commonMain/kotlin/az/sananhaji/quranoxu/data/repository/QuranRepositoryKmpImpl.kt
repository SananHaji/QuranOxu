@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package az.sananhaji.quranoxu.data.repository

import az.sananhaji.quranoxu.domain.model.*
import az.sananhaji.quranoxu.domain.repository.QuranRepositoryContract
import az.sananhaji.quranoxu.resources.Res
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SurahJsonModel(
    val index: Int,
    val verse_count: Int,
    val name_arabic_latin: String? = null,
    val verses_latin: List<String> = emptyList(),
    val verses_azerbaijani: List<String> = emptyList(),
    val verses_arabic: List<String> = emptyList(),
    val name_azeri: String = "",
    val name_azeri_tercume: String? = null,
    val place_azeri: String? = null,
    val place_eng: String? = null,
    val name_arabic_with_arabic_letter: String? = null
)

class QuranRepositoryKmpImpl(
    private val settings: Settings = Settings()
) : QuranRepositoryContract {

    companion object {
        val REVELATION_ORDERS = intArrayOf(
            5, 87, 89, 92, 112, 55, 39, 88, 113, 51,
            52, 53, 96, 72, 54, 70, 50, 69, 44, 45,
            73, 103, 74, 102, 42, 47, 48, 49, 85, 84,
            57, 75, 90, 58, 43, 41, 56, 38, 59, 60,
            61, 62, 63, 64, 65, 66, 95, 111, 106, 34,
            67, 76, 23, 37, 97, 46, 94, 105, 101, 91,
            109, 110, 104, 108, 99, 107, 77, 2, 78, 79,
            71, 40, 3, 4, 31, 98, 33, 80, 81, 24,
            7, 82, 86, 83, 27, 36, 8, 68, 10, 35,
            26, 9, 11, 12, 28, 1, 25, 100, 93, 14,
            30, 16, 13, 32, 19, 29, 17, 15, 18, 114,
            6, 22, 20, 21
        )

        private const val KEY_BOOKMARKS = "user_bookmarks"
        private const val KEY_NOTES = "user_notes"
        private const val KEY_READ_VERSES = "user_read_verses"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cachedSurahs: List<SurahJsonModel>? = null

    private suspend fun loadSurahs(): List<SurahJsonModel> {
        return mutex.withLock {
            cachedSurahs ?: withContext(Dispatchers.Default) {
                val bytes = Res.readBytes("files/quran_data.json")
                val text = bytes.decodeToString()
                val list = json.decodeFromString<List<SurahJsonModel>>(text)
                cachedSurahs = list
                list
            }
        }
    }

    override suspend fun getSupportedLanguages(): List<SupportedLanguageEntity> {
        return listOf(
            SupportedLanguageEntity(code = "az", displayName = "Azərbaycan"),
            SupportedLanguageEntity(code = "ar", displayName = "العربية"),
            SupportedLanguageEntity(code = "latin", displayName = "Oxunuş (Transkripsiya)")
        )
    }

    override suspend fun getAllSurahs(): List<SurahEntity> {
        val raw = loadSurahs()
        val readVerses = getReadVersesMap()
        return raw.map { model ->
            val revOrder = if (model.index in 1..114) REVELATION_ORDERS[model.index - 1] else model.index
            val readCount = readVerses[model.index]?.size ?: 0
            SurahEntity(
                index = model.index,
                verseCount = model.verse_count,
                nameArabicLatin = model.name_arabic_latin,
                versesLatin = emptyList(),
                versesAzerbaijani = emptyList(),
                versesArabic = emptyList(),
                nameAzeri = model.name_azeri,
                nameAzeriTercume = model.name_azeri_tercume,
                placeAzeri = model.place_azeri,
                placeEng = model.place_eng,
                nameArabicWithArabicLetter = model.name_arabic_with_arabic_letter,
                revelationOrder = revOrder,
                readVerseCount = readCount
            )
        }
    }

    override suspend fun getSurahWithVerses(surahIndex: Int): Pair<SurahEntity?, List<VerseEntity>> {
        val raw = loadSurahs()
        val model = raw.find { it.index == surahIndex } ?: return Pair(null, emptyList())
        val revOrder = if (model.index in 1..114) REVELATION_ORDERS[model.index - 1] else model.index

        val bookmarks = getAllBookmarks().filter { it.surahIndex == surahIndex }.map { it.verseNumber }.toSet()
        val notes = getAllNotes().filter { it.surahIndex == surahIndex }.associate { it.verseNumber to it.noteText }
        val readVerses = getReadVersesMap()[surahIndex] ?: emptySet()

        val count = model.verse_count
        val verses = (0 until count).map { i ->
            val vNum = i + 1
            VerseEntity(
                surahIndex = surahIndex,
                verseNumber = vNum,
                arabicText = model.verses_arabic.getOrElse(i) { "" },
                azeriText = model.verses_azerbaijani.getOrElse(i) { "" },
                latinText = model.verses_latin.getOrNull(i),
                isBookmarked = bookmarks.contains(vNum),
                isRead = readVerses.contains(vNum),
                noteText = notes[vNum]
            )
        }

        val surahEntity = SurahEntity(
            index = model.index,
            verseCount = model.verse_count,
            nameArabicLatin = model.name_arabic_latin,
            versesLatin = model.verses_latin,
            versesAzerbaijani = model.verses_azerbaijani,
            versesArabic = model.verses_arabic,
            nameAzeri = model.name_azeri,
            nameAzeriTercume = model.name_azeri_tercume,
            placeAzeri = model.place_azeri,
            placeEng = model.place_eng,
            nameArabicWithArabicLetter = model.name_arabic_with_arabic_letter,
            revelationOrder = revOrder,
            readVerseCount = readVerses.size
        )

        return Pair(surahEntity, verses)
    }

    override suspend fun toggleBookmark(surahIndex: Int, verseNumber: Int, surahName: String): Boolean {
        val current = getAllBookmarks().toMutableList()
        val existing = current.indexOfFirst { it.surahIndex == surahIndex && it.verseNumber == verseNumber }
        val isAdded: Boolean
        if (existing >= 0) {
            current.removeAt(existing)
            isAdded = false
        } else {
            current.add(
                BookmarkEntity(
                    id = az.sananhaji.quranoxu.util.getCurrentTimeMillis(),
                    surahIndex = surahIndex,
                    verseNumber = verseNumber,
                    surahName = surahName,
                    timestamp = az.sananhaji.quranoxu.util.getCurrentTimeMillis()
                )
            )
            isAdded = true
        }
        val encoded = json.encodeToString(current)
        settings.putString(KEY_BOOKMARKS, encoded)
        return isAdded
    }

    override suspend fun saveNote(surahIndex: Int, verseNumber: Int, surahName: String, noteText: String) {
        val current = getAllNotes().toMutableList()
        current.removeAll { it.surahIndex == surahIndex && it.verseNumber == verseNumber }
        current.add(
            UserNoteEntity(
                id = az.sananhaji.quranoxu.util.getCurrentTimeMillis(),
                surahIndex = surahIndex,
                verseNumber = verseNumber,
                surahName = surahName,
                noteText = noteText,
                timestamp = az.sananhaji.quranoxu.util.getCurrentTimeMillis()
            )
        )
        val encoded = json.encodeToString(current)
        settings.putString(KEY_NOTES, encoded)
    }

    override suspend fun deleteNote(surahIndex: Int, verseNumber: Int) {
        val current = getAllNotes().toMutableList()
        current.removeAll { it.surahIndex == surahIndex && it.verseNumber == verseNumber }
        val encoded = json.encodeToString(current)
        settings.putString(KEY_NOTES, encoded)
    }

    override suspend fun getAllBookmarks(): List<BookmarkEntity> {
        val raw = settings.getStringOrNull(KEY_BOOKMARKS) ?: return emptyList()
        return try {
            json.decodeFromString<List<BookmarkEntity>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllNotes(): List<UserNoteEntity> {
        val raw = settings.getStringOrNull(KEY_NOTES) ?: return emptyList()
        return try {
            json.decodeFromString<List<UserNoteEntity>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun markVerseRead(surahIndex: Int, verseNumber: Int, surahName: String) {
        val map = getReadVersesMap().toMutableMap()
        val set = map.getOrPut(surahIndex) { mutableSetOf() }.toMutableSet()
        set.add(verseNumber)
        map[surahIndex] = set
        saveReadVersesMap(map)
    }

    override suspend fun resetSurahReadProgress(surahIndex: Int) {
        val map = getReadVersesMap().toMutableMap()
        map.remove(surahIndex)
        saveReadVersesMap(map)
    }

    override suspend fun getOverallProgress(): OverallProgressEntity {
        val map = getReadVersesMap()
        val totalRead = map.values.sumOf { it.size }
        val totalVerses = 6236
        return OverallProgressEntity(
            totalReadVerses = totalRead,
            totalVerses = totalVerses,
            completedSurahsCount = 0
        )
    }

    override suspend fun searchVerses(query: String): List<VerseSearchResultEntity> {
        if (query.isBlank()) return emptyList()
        val raw = loadSurahs()
        val results = mutableListOf<VerseSearchResultEntity>()
        val cleanQ = query.trim().lowercase()
        for (surah in raw) {
            for (i in 0 until surah.verse_count) {
                val azeri = surah.verses_azerbaijani.getOrNull(i) ?: ""
                val arabic = surah.verses_arabic.getOrNull(i) ?: ""
                val latin = surah.verses_latin.getOrNull(i) ?: ""
                if (azeri.lowercase().contains(cleanQ) || latin.lowercase().contains(cleanQ) || arabic.contains(cleanQ)) {
                    results.add(
                        VerseSearchResultEntity(
                            surahIndex = surah.index,
                            verseNumber = i + 1,
                            surahName = surah.name_azeri,
                            text = azeri,
                            arabicText = arabic,
                            revelationOrder = surah.index
                        )
                    )
                }
            }
        }
        return results
    }

    private fun getReadVersesMap(): Map<Int, Set<Int>> {
        val raw = settings.getStringOrNull(KEY_READ_VERSES) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<Int, Set<Int>>>(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveReadVersesMap(map: Map<Int, Set<Int>>) {
        val encoded = json.encodeToString(map)
        settings.putString(KEY_READ_VERSES, encoded)
    }
}
