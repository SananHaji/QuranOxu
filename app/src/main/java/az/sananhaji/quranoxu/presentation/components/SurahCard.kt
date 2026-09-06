package az.sananhaji.quranoxu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.sananhaji.quranoxu.domain.model.SurahEntity

import androidx.compose.ui.text.AnnotatedString
import az.sananhaji.quranoxu.util.buildHighlightAnnotatedString

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import az.sananhaji.quranoxu.domain.model.SurahDownloadStatus

@Composable
fun SurahCard(
    surah: SurahEntity,
    onClick: () -> Unit,
    showRevelationOrder: Boolean = false,
    searchQuery: String = "",
    onResetReadProgress: (() -> Unit)? = null,
    downloadStatus: SurahDownloadStatus? = null,
    onDownloadSurah: (() -> Unit)? = null,
    onCancelDownloadSurah: (() -> Unit)? = null,
    onDeleteSurahAudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteAudioDialog by remember { mutableStateOf(false) }
    val progress = if (surah.verseCount > 0) surah.readVerseCount.toFloat() / surah.verseCount else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surah Index Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showRevelationOrder) "#${surah.revelationOrder}" else surah.index.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = if (showRevelationOrder) 13.sp else 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Surah Names & Meta
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            buildHighlightAnnotatedString(
                                text = surah.nameAzeri,
                                query = searchQuery,
                                highlightColor = MaterialTheme.colorScheme.primary,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        } else AnnotatedString(surah.nameAzeri),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    surah.nameAzeriTercume?.let { translation ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                buildHighlightAnnotatedString(
                                    text = translation,
                                    query = searchQuery,
                                    highlightColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            } else AnnotatedString(translation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        surah.placeAzeri?.let { place ->
                            Text(
                                text = place,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${surah.readVerseCount} / ${surah.verseCount} ayə oxunub",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (surah.readVerseCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (surah.readVerseCount > 0) FontWeight.Bold else FontWeight.Normal
                        )

                        if (surah.readVerseCount > 0 && onResetReadProgress != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showResetDialog = true }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Oxunmanı Sıfırla",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Arabic Title and Audio Download Action
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    surah.nameArabicWithArabicLetter?.let { arabicName ->
                        Text(
                            text = arabicName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Audio Download Status
                    if (downloadStatus != null) {
                        when {
                            downloadStatus.isDownloading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                        .padding(start = 6.dp, end = if (onCancelDownloadSurah != null) 4.dp else 6.dp, top = 2.dp, bottom = 2.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${(downloadStatus.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (onCancelDownloadSurah != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Yükləməni ləğv et",
                                            modifier = Modifier
                                                .size(13.dp)
                                                .clickable { onCancelDownloadSurah() },
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            downloadStatus.isDownloaded -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable { showDeleteAudioDialog = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Oflayn hazırdır (Səsləri sil)",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Oflayn",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            else -> {
                                if (onDownloadSurah != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { onDownloadSurah() }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = "Oflayn dinləmək üçün yüklə",
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Yüklə",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (surah.readVerseCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showResetDialog && onResetReadProgress != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Oxunmanı sıfırla?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "${surah.nameAzeri} surəsi üzrə oxunmuş ${surah.readVerseCount} ayənin oxunma tarixçəsi silinəcək.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetReadProgress()
                    }
                ) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Ləğv et")
                }
            }
        )
    }

    if (showDeleteAudioDialog && onDeleteSurahAudio != null) {
        AlertDialog(
            onDismissRequest = { showDeleteAudioDialog = false },
            title = { Text("Oflayn səsi sil?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "${surah.nameAzeri} surəsinin yaddaşda saxlanılan bütün səs faylları silinəcək.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAudioDialog = false
                        onDeleteSurahAudio()
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAudioDialog = false }) {
                    Text("Ləğv et")
                }
            }
        )
    }
}
