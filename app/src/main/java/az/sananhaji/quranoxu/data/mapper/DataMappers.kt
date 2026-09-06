package az.sananhaji.quranoxu.data.mapper

import az.sananhaji.quranoxu.data.db.SupportedLanguage
import az.sananhaji.quranoxu.data.model.Bookmark
import az.sananhaji.quranoxu.data.model.Surah
import az.sananhaji.quranoxu.data.model.UserNote
import az.sananhaji.quranoxu.data.model.Verse
import az.sananhaji.quranoxu.domain.model.BookmarkEntity
import az.sananhaji.quranoxu.domain.model.SupportedLanguageEntity
import az.sananhaji.quranoxu.domain.model.SurahEntity
import az.sananhaji.quranoxu.domain.model.UserNoteEntity
import az.sananhaji.quranoxu.domain.model.VerseEntity

fun Surah.toEntity(revelationOrder: Int = index, readVerseCount: Int = 0): SurahEntity = SurahEntity(
    index = index,
    verseCount = verseCount,
    nameArabicLatin = nameArabicLatin,
    versesLatin = versesLatin,
    versesAzerbaijani = versesAzerbaijani,
    versesArabic = versesArabic,
    nameAzeri = nameAzeri,
    nameAzeriTercume = nameAzeriTercume,
    placeAzeri = placeAzeri,
    placeEng = placeEng,
    nameArabicWithArabicLetter = nameArabicWithArabicLetter,
    revelationOrder = revelationOrder,
    readVerseCount = readVerseCount
)

fun Verse.toEntity(): VerseEntity = VerseEntity(
    surahIndex = surahIndex,
    verseNumber = verseNumber,
    arabicText = arabicText,
    azeriText = azeriText,
    latinText = latinText,
    isBookmarked = isBookmarked,
    noteText = noteText
)

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    surahIndex = surahIndex,
    verseNumber = verseNumber,
    surahName = surahName,
    timestamp = timestamp
)

fun UserNote.toEntity(): UserNoteEntity = UserNoteEntity(
    id = id,
    surahIndex = surahIndex,
    verseNumber = verseNumber,
    surahName = surahName,
    noteText = noteText,
    timestamp = timestamp
)

fun SupportedLanguage.toEntity(): SupportedLanguageEntity = SupportedLanguageEntity(
    code = code,
    displayName = displayName
)
