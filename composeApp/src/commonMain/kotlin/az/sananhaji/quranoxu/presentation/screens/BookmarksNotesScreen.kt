package az.sananhaji.quranoxu.presentation.screens

import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.sananhaji.quranoxu.presentation.mvi.BookmarksNotesIntent
import az.sananhaji.quranoxu.presentation.viewmodel.BookmarksNotesViewModel

@Composable
fun BookmarksNotesScreen(
    viewModel: BookmarksNotesViewModel,
    onNavigateToSurah: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Tab Selector (Əlfəcinlər / Notlar)
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.processIntent(BookmarksNotesIntent.SelectTab(0)) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (state.selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Əlfəcinlər (${state.bookmarks.size})",
                            fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            )

            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.processIntent(BookmarksNotesIntent.SelectTab(1)) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = if (state.selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Notlar (${state.notes.size})",
                            fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.selectedTab == 0) {
            // Bookmarks List
            if (state.bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hələ ki, heç bir ayə qeyd edilməyib.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (isLandscape) 20.dp else 80.dp)
                ) {
                    items(state.bookmarks, key = { "${it.surahIndex}_${it.verseNumber}" }) { bookmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onNavigateToSurah(bookmark.surahIndex, bookmark.verseNumber) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${bookmark.surahName} • Ayə ${bookmark.verseNumber}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Birbaşa ayəyə keçmək üçün toxunun",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.processIntent(BookmarksNotesIntent.DeleteBookmark(bookmark)) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Notes List
            if (state.notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hələ ki, heç bir not yazılmayıb.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (isLandscape) 20.dp else 80.dp)
                ) {
                    items(state.notes, key = { "${it.surahIndex}_${it.verseNumber}" }) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onNavigateToSurah(note.surahIndex, note.verseNumber) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${note.surahName} • Ayə ${note.verseNumber}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { viewModel.processIntent(BookmarksNotesIntent.OpenEditNoteDialog(note)) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Düzəliş et",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.processIntent(BookmarksNotesIntent.DeleteNote(note)) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Sil",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = note.noteText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Note Dialog when editing a note from the Notes tab
    state.editingNote?.let { noteToEdit ->
        var noteText by remember { mutableStateOf(noteToEdit.noteText) }

        AlertDialog(
            onDismissRequest = { viewModel.processIntent(BookmarksNotesIntent.CloseEditNoteDialog) },
            title = {
                Text(
                    text = "${noteToEdit.surahName} • Ayə ${noteToEdit.verseNumber} Notu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Notunuza düzəliş edin:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(BookmarksNotesIntent.SaveEditedNote(noteToEdit, noteText))
                    }
                ) {
                    Text("Yadda saxla")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.processIntent(BookmarksNotesIntent.CloseEditNoteDialog) }
                ) {
                    Text("Ləğv et")
                }
            }
        )
    }
}
}
