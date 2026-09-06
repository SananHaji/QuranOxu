package az.sananhaji.quranoxu.data.model

data class Surah(
    val index: Int,
    val verseCount: Int,
    val nameArabicLatin: String?,
    val versesLatin: List<String>,
    val versesAzerbaijani: List<String>,
    val versesArabic: List<String>,
    val nameAzeri: String,
    val nameAzeriTercume: String?,
    val placeAzeri: String?,
    val placeEng: String?,
    val nameArabicWithArabicLetter: String?
)
