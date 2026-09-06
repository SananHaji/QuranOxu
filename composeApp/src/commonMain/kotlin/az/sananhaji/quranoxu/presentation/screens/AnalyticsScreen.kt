package az.sananhaji.quranoxu.presentation.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.sananhaji.quranoxu.presentation.components.SurahCard
import az.sananhaji.quranoxu.presentation.mvi.SurahDetailIntent
import az.sananhaji.quranoxu.presentation.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToSurah: (Int, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.processIntent(az.sananhaji.quranoxu.presentation.mvi.AnalyticsIntent.LoadAnalytics)
    }

    val state by viewModel.state.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val progress = state.overallProgress

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val readPercent = if (progress.totalVerses > 0) (progress.totalReadVerses.toFloat() / progress.totalVerses * 100).toInt() else 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
        Text(
            text = "Quran Oxuma Analitikası",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Main Stat Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ümumi Tərəqqi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "$readPercent%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress.totalReadVerses.toFloat() / progress.totalVerses },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Oxunmuş Ayələr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${progress.totalReadVerses} ayə",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = "Qalan Ayələr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${progress.totalVerses - progress.totalReadVerses} ayə",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = "Tamamlanan Surələr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${progress.completedSurahsCount} / 114",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Last Read Location Banner Card
        if (progress.lastReadSurahIndex > 0) {
            Text(
                text = "Son Qaldığınız Yer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${progress.lastReadSurahIndex}. ${progress.lastReadSurahName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${progress.lastReadVerseNumber}-ci ayədə qaldınız",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Button(
                        onClick = { onNavigateToSurah(progress.lastReadSurahIndex, progress.lastReadVerseNumber) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(text = "Davam et", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Smart Recommendation Card
        progress.recommendedSurah?.let { recommended ->
            Text(
                text = "Növbəti Tövsiyə Olunan Surə",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${recommended.index}. ${recommended.nameAzeri}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${recommended.verseCount} ayə • ${recommended.placeAzeri ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = { onNavigateToSurah(recommended.index, null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Oxu")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Oxu", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Oxunmuş surələr - Ən son oxunanlar birinci nümayiş olunur
        val readSurahs = state.surahs
            .filter { it.readVerseCount > 0 }
            .sortedByDescending { progress.surahLastReadTimestamps[it.index] ?: 0L }

        Text(
            text = if (readSurahs.isNotEmpty()) "Oxunmuş Surələr (${readSurahs.size})" else "Oxunmuş Surələr",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (readSurahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hələ ki heç bir surədən ayə oxunmayıb.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            readSurahs.forEach { surah ->
                SurahCard(
                    surah = surah,
                    onClick = { onNavigateToSurah(surah.index, null) },
                    onResetReadProgress = { viewModel.processIntent(az.sananhaji.quranoxu.presentation.mvi.AnalyticsIntent.ResetSurahReadProgress(surah.index)) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 80.dp))
        }
    }
}
}
