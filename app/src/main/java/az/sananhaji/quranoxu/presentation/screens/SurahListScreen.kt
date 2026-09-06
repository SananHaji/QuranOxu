package az.sananhaji.quranoxu.presentation.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.sananhaji.quranoxu.domain.model.SurahSortMode
import az.sananhaji.quranoxu.presentation.components.SurahCard
import az.sananhaji.quranoxu.presentation.mvi.SurahListIntent
import az.sananhaji.quranoxu.presentation.viewmodel.SurahListViewModel

import az.sananhaji.quranoxu.util.buildHighlightAnnotatedString

@Composable
fun SurahListScreen(
    viewModel: SurahListViewModel,
    onSurahClick: (Int, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.processIntent(SurahListIntent.LoadSurahs)
    }

    val state by viewModel.state.collectAsState()
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val surahGridState = rememberLazyGridState()
    val verseListState = rememberLazyListState()

    LaunchedEffect(state.sortMode) {
        surahGridState.scrollToItem(0)
        verseListState.scrollToItem(0)
    }

    val progress = state.overallProgress

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Mode Selector Tabs (Surələr vs Bütün Ayələr - Ətraflı Axtarış)
        TabRow(
            selectedTabIndex = if (state.isVerseSearch) 1 else 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Tab(
                selected = !state.isVerseSearch,
                onClick = { viewModel.processIntent(SurahListIntent.SetSearchMode(false)) },
                text = { Text("Surələr", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = state.isVerseSearch,
                onClick = { viewModel.processIntent(SurahListIntent.SetSearchMode(true)) },
                text = { Text("Bütün Ayələr (Ətraflı)", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Search Bar & Sort Dropdown Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (state.isVerseSearch) state.verseSearchQuery else state.searchQuery,
                onValueChange = { query ->
                    if (state.isVerseSearch) {
                        viewModel.processIntent(SurahListIntent.SearchVerses(query))
                    } else {
                        viewModel.processIntent(SurahListIntent.SearchSurahs(query))
                    }
                },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                placeholder = {
                    Text(
                        text = if (state.isVerseSearch) "Bütün ayələrdə axtar (Smart Search)..." else "Surə adı və ya nömrəsi axtar...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Axtar"
                    )
                },
                trailingIcon = {
                    val activeQuery = if (state.isVerseSearch) state.verseSearchQuery else state.searchQuery
                    if (activeQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            if (state.isVerseSearch) {
                                viewModel.processIntent(SurahListIntent.SearchVerses(""))
                            } else {
                                viewModel.processIntent(SurahListIntent.SearchSurahs(""))
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Təmizlə"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Selector Button & Dropdown Menu
            Box {
                IconButton(
                    onClick = { isSortMenuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sırala",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    SurahSortMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mode.displayName,
                                    fontWeight = if (state.sortMode == mode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (state.sortMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                viewModel.processIntent(SurahListIntent.SetSortMode(mode))
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // When in standard Surah search mode, show surah list with scrollable last read card
        if (!state.isVerseSearch) {
            if (state.isLoading && state.filteredSurahs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredSurahs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Axtarışa uyğun surə tapılmadı.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                    state = surahGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = if (isLandscape) 20.dp else 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dedicated Compact Last Read Location Card (scrolls together with surahs)
                    if (progress.lastReadSurahIndex > 0) {
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            key = "last_read_card"
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable { onSurahClick(progress.lastReadSurahIndex, progress.lastReadVerseNumber) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Son Qaldığınız Yer",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                            )
                                            Text(
                                                text = "${progress.lastReadSurahName} • ${progress.lastReadVerseNumber}-ci ayə",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSurahClick(progress.lastReadSurahIndex, progress.lastReadVerseNumber) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Davam et", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Davam et",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(
                        items = state.filteredSurahs,
                        key = { it.index }
                    ) { surah ->
                        SurahCard(
                            surah = surah,
                            onClick = { onSurahClick(surah.index, null) },
                            showRevelationOrder = state.sortMode == SurahSortMode.REVELATION_ORDER,
                            searchQuery = state.searchQuery,
                            onResetReadProgress = { viewModel.processIntent(SurahListIntent.ResetSurahReadProgress(surah.index)) },
                            downloadStatus = state.downloadStatuses[surah.index],
                            onDownloadSurah = { viewModel.processIntent(SurahListIntent.DownloadSurah(surah.index, surah.verseCount)) },
                            onCancelDownloadSurah = { viewModel.processIntent(SurahListIntent.CancelDownloadSurah(surah.index, surah.verseCount)) },
                            onDeleteSurahAudio = { viewModel.processIntent(SurahListIntent.DeleteSurahAudio(surah.index)) }
                        )
                    }
                }
            }
        } else {
            // Detailed Verse Search Results UI
            if (state.isVerseSearchLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.verseSearchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bütün 114 Surə və 6,236 Ayə daxilində smart axtarış etmək üçün söz daxil edin.\n\nMəsələn: 'bagislayan', 'rebb', 'cennet'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else if (state.verseSearchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"${state.verseSearchQuery}\" axtarışına uyğun ayə tapılmadı.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "${state.verseSearchResults.size} ayə tapıldı",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    state = verseListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (isLandscape) 20.dp else 80.dp)
                ) {
                    items(
                        items = state.verseSearchResults,
                        key = { "${it.surahIndex}_${it.verseNumber}" }
                    ) { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onSurahClick(result.surahIndex, result.verseNumber) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val surahLabel = if (state.sortMode == SurahSortMode.REVELATION_ORDER) {
                                        "${result.surahName} (Nüzul #${result.revelationOrder}) • Ayə ${result.verseNumber}"
                                    } else {
                                        "${result.surahName} • Ayə ${result.verseNumber}"
                                    }
                                    Text(
                                        text = buildHighlightAnnotatedString(
                                            text = surahLabel,
                                            query = state.verseSearchQuery,
                                            highlightColor = MaterialTheme.colorScheme.primary,
                                            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = buildHighlightAnnotatedString(
                                        text = result.text,
                                        query = state.verseSearchQuery,
                                        highlightColor = MaterialTheme.colorScheme.primary,
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!result.arabicText.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = result.arabicText,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

