package az.sananhaji.quranoxu.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.sananhaji.quranoxu.domain.model.AudioStateEntity

@Composable
fun AudioPlayerBar(
    audioState: AudioStateEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onSetSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onBarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isVisible = audioState.surahIndex > 0 && audioState.verseNumber > 0
    var showTimerDialog by remember { mutableStateOf(false) }

    if (showTimerDialog) {
        SleepTimerDialog(
            remainingSeconds = audioState.remainingSleepTimerSeconds,
            onSetTimer = onSetSleepTimer,
            onCancelTimer = onCancelSleepTimer,
            onDismiss = { showTimerDialog = false }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (onBarClick != null) {
                                Modifier.clickable { onBarClick() }
                            } else Modifier
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = audioState.surahName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val timerText = if (audioState.remainingSleepTimerSeconds > 0) {
                            val mins = audioState.remainingSleepTimerSeconds / 60
                            val secs = audioState.remainingSleepTimerSeconds % 60
                            " • ⏱ ${String.format("%02d:%02d", mins, secs)}"
                        } else ""

                        val statusExtra = when {
                            audioState.isBuffering -> " • Yüklənir..."
                            audioState.isOfflineAvailable -> " • 💾 Oflayn"
                            else -> ""
                        }

                        Text(
                            text = "Ayə ${audioState.verseNumber} / ${audioState.totalVerses}$statusExtra$timerText",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (audioState.remainingSleepTimerSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sleep Timer Button
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Dinləmə taymeri",
                            tint = if (audioState.remainingSleepTimerSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Previous Verse Button
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Əvvəlki ayə",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = {
                            if (audioState.isPlaying) onPause() else onResume()
                        }
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "Pauza" else "Oxut",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Next Verse Button
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Növbəti ayə",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Stop Button
                    IconButton(onClick = onStop) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dayandır",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
