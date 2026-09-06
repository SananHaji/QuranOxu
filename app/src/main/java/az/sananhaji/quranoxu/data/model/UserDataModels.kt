package az.sananhaji.quranoxu.data.model

data class UserNote(
    val id: Long = 0,
    val surahIndex: Int,
    val verseNumber: Int,
    val surahName: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Bookmark(
    val id: Long = 0,
    val surahIndex: Int,
    val verseNumber: Int,
    val surahName: String,
    val timestamp: Long = System.currentTimeMillis()
)
