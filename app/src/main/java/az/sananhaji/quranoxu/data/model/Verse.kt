package az.sananhaji.quranoxu.data.model

data class Verse(
    val surahIndex: Int,
    val verseNumber: Int,
    val arabicText: String,
    val azeriText: String,
    val latinText: String?,
    val isBookmarked: Boolean = false,
    val noteText: String? = null
)
