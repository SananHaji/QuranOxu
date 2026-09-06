package az.sananhaji.quranoxu.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * Builds a regex pattern for case-insensitive smart search with directional Azerbaijani letter mapping:
 * - 'e', 'E' -> [eEəƏäÄ]
 * - 'a', 'A' -> [aAəƏ]
 * - 'ə', 'Ə' -> [əƏeEaAäÄ]
 * - 'o', 'O' -> [oOöÖ]
 * - 'ö', 'Ö' -> [öÖoO]
 * - 'u', 'U' -> [uUüÜ]
 * - 'ü', 'Ü' -> [üÜuU]
 * - 'g', 'G' -> [gGğĞ]
 * - 'ğ', 'Ğ' -> [ğĞgG]
 * - 'i', 'I', 'İ', 'ı' -> [iIİı]
 * - 'c', 'C' -> [cCçÇ]
 * - 'ç', 'Ç' -> [çÇcC]
 * - 's', 'S' -> [sSşŞ]
 * - 'ş', 'Ş' -> [şŞsS]
 * All other characters match both uppercase and lowercase forms.
 */
fun buildSmartSearchRegex(query: String): Regex {
    val pattern = StringBuilder()
    for (ch in query) {
        val lower = ch.lowercaseChar()
        val group = when (lower) {
            'e' -> "[eEəƏäÄ]"
            'a' -> "[aAəƏ]"
            'ə' -> "[əƏeEaAäÄ]"
            'o' -> "[oOöÖ]"
            'ö' -> "[öÖoO]"
            'u' -> "[uUüÜ]"
            'ü' -> "[üÜuU]"
            'g' -> "[gGğĞ]"
            'ğ' -> "[ğĞgG]"
            'i', 'ı' -> "[iIİı]"
            'c' -> "[cCçÇ]"
            'ç' -> "[çÇcC]"
            's' -> "[sSşŞ]"
            'ş' -> "[şŞsS]"
            else -> {
                val u = ch.uppercaseChar()
                val l = ch.lowercaseChar()
                if (u != l) {
                    "[${Regex.escape(l.toString())}${Regex.escape(u.toString())}]"
                } else {
                    Regex.escape(ch.toString())
                }
            }
        }
        pattern.append(group)
    }
    return Regex(pattern.toString(), RegexOption.IGNORE_CASE)
}

/**
 * Extension function checking if this string contains the query using case-insensitive smart search rules.
 */
fun String.containsSmart(query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    return buildSmartSearchRegex(trimmed).containsMatchIn(this)
}

/**
 * Builds an AnnotatedString highlighting all occurrences of query in text.
 * Uses case-insensitive smart search regex to highlight matches.
 */
fun buildHighlightAnnotatedString(
    text: String,
    query: String,
    highlightColor: Color,
    backgroundColor: Color = Color.Unspecified
): AnnotatedString {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) {
        return AnnotatedString(text)
    }

    val regex = buildSmartSearchRegex(trimmedQuery)
    val matches = regex.findAll(text).toList()
    if (matches.isEmpty()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        append(text)
        for (match in matches) {
            addStyle(
                style = SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    background = backgroundColor
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}
