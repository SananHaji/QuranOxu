package az.sananhaji.quranoxu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import az.sananhaji.quranoxu.presentation.components.NoteDialog
import az.sananhaji.quranoxu.presentation.components.VerseCard
import az.sananhaji.quranoxu.presentation.mvi.SurahDetailIntent
import az.sananhaji.quranoxu.presentation.viewmodel.SurahDetailViewModel

import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload

@Composable
fun SurahDetailScreen(
    viewModel: SurahDetailViewModel,
    surahIndex: Int,
    initialVerseNumber: Int? = null,
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(surahIndex, initialVerseNumber) {
        viewModel.loadSurah(surahIndex, initialVerseNumber)
    }

    val state by viewModel.state.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    val currentSurah = state.surah
    val listState = rememberLazyListState()
    var showGoToVerseDialog by remember { mutableStateOf(false) }
    var showDeleteAudioDialog by remember { mutableStateOf(false) }

    // Auto-scroll to the currently playing verse
    LaunchedEffect(audioState.verseNumber, audioState.surahIndex) {
        if (currentSurah != null && audioState.surahIndex == currentSurah.index && audioState.verseNumber > 0) {
            val targetIndex = audioState.verseNumber // Item index 0 is Surah Header
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Direct scroll to target verse when navigating from Bookmarks/Notes or AudioPlayerBar click
    LaunchedEffect(state.targetScrollVerseNumber, state.scrollTrigger, currentSurah) {
        state.targetScrollVerseNumber?.let { verseNumber ->
            if (currentSurah != null && verseNumber > 0) {
                listState.animateScrollToItem(verseNumber)
            }
        }
    }

    if (state.isLoading || currentSurah == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = if (isFullscreen) 48.dp else 8.dp, bottom = 80.dp)
        ) {
            // Surah Banner Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                        ) {
                            // Fullscreen Toggle Button
                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Tam Ekran",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            text = currentSurah.nameAzeri,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        currentSurah.nameAzeriTercume?.let { translation ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        currentSurah.nameArabicWithArabicLetter?.let { arabicName ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = arabicName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Go to Verse Button
                            OutlinedButton(
                                onClick = { showGoToVerseDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tag,
                                    contentDescription = "Ayəyə keç",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Ayəyə keç", style = MaterialTheme.typography.labelMedium)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Play Full Surah Button
                            Button(
                                onClick = { viewModel.processIntent(SurahDetailIntent.PlayFullSurah) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Surəni Dinlə",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Surəni Dinlə", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Offline Audio Download / Delete Button
                            val dl = state.downloadStatus
                            when {
                                dl.isDownloading -> {
                                    OutlinedButton(
                                        onClick = {},
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${(dl.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                dl.isDownloaded -> {
                                    OutlinedButton(
                                        onClick = { showDeleteAudioDialog = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Oflayn yüklənib (Silmək üçün toxunun)",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Oflayn", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                else -> {
                                    OutlinedButton(
                                        onClick = { viewModel.processIntent(SurahDetailIntent.DownloadCurrentSurah) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = "Oflayn Yüklə",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Yüklə", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bismillah Header (except Surah 9 At-Tawbah and Surah 1)
                if (currentSurah.index != 9 && currentSurah.index != 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // List of Verses
            items(
                items = state.verses,
                key = { "${it.surahIndex}_${it.verseNumber}" }
            ) { verse ->
                LaunchedEffect(verse.verseNumber) {
                    if (!verse.isRead) {
                        viewModel.processIntent(SurahDetailIntent.MarkVerseRead(verse.verseNumber))
                    }
                }

                val isArabicVisible = state.perVerseArabicOverrides[verse.verseNumber] ?: settingsState.showArabicByDefault
                val isPlayingThisVerse = audioState.surahIndex == currentSurah.index && audioState.verseNumber == verse.verseNumber

                VerseCard(
                    verse = verse,
                    isArabicVisible = isArabicVisible,
                    selectedLanguage = settingsState.selectedLanguage,
                    isPlayingThisVerse = isPlayingThisVerse,
                    onPlayAudio = { viewModel.processIntent(SurahDetailIntent.PlayVerse(verse.verseNumber)) },
                    onToggleArabic = { viewModel.processIntent(SurahDetailIntent.ToggleArabic(verse.verseNumber)) },
                    onBookmarkToggle = { viewModel.processIntent(SurahDetailIntent.ToggleBookmark(verse)) },
                    onEditNote = { viewModel.processIntent(SurahDetailIntent.OpenNoteDialog(verse)) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        // Floating Exit Fullscreen Button when Fullscreen mode is active
        if (isFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onToggleFullscreen() }
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Tam Ekrandan Çıx",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Note Dialog
        state.editingVerse?.let { verseToEdit ->
            NoteDialog(
                verse = verseToEdit,
                onDismiss = { viewModel.processIntent(SurahDetailIntent.CloseNoteDialog) },
                onSaveNote = { noteText -> viewModel.processIntent(SurahDetailIntent.SaveNote(verseToEdit, noteText)) },
                onDeleteNote = { viewModel.processIntent(SurahDetailIntent.DeleteNote(verseToEdit)) }
            )
        }

        // Go to Verse Dialog
        if (showGoToVerseDialog) {
            var inputVerse by remember { mutableStateOf("") }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showGoToVerseDialog = false },
                title = { Text("Ayəyə keç", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "${currentSurah.nameAzeri} (1 - ${currentSurah.verseCount} ayə)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputVerse,
                            onValueChange = {
                                inputVerse = it
                                errorMessage = null
                            },
                            label = { Text("Ayə nömrəsi") },
                            placeholder = { Text("Məsələn: 25") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = errorMessage != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val vNum = inputVerse.toIntOrNull()
                            if (vNum != null && vNum in 1..currentSurah.verseCount) {
                                showGoToVerseDialog = false
                                viewModel.processIntent(SurahDetailIntent.ScrollToVerse(vNum))
                            } else {
                                errorMessage = "Xahiş olunur 1 və ${currentSurah.verseCount} arasında rəqəm daxil edin"
                            }
                        }
                    ) {
                        Text("Keç", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoToVerseDialog = false }) {
                        Text("Ləğv et")
                    }
                }
            )
        }

        // Delete Downloaded Audio Dialog
        if (showDeleteAudioDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAudioDialog = false },
                title = { Text("Oflayn səsi sil?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "${currentSurah.nameAzeri} surəsinin yaddaşda saxlanılan bütün səs faylları silinəcək.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAudioDialog = false
                            viewModel.processIntent(SurahDetailIntent.DeleteCurrentSurahAudio)
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
}
