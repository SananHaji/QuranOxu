package az.sananhaji.quranoxu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.sananhaji.quranoxu.ui.components.NoteDialog
import az.sananhaji.quranoxu.ui.components.VerseCard
import az.sananhaji.quranoxu.ui.viewmodel.QuranViewModel

@Composable
fun SurahDetailScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val surah by viewModel.selectedSurah.collectAsState()
    val verses by viewModel.versesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val editingVerse by viewModel.editingNoteVerse.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val showArabicByDefault by viewModel.showArabicByDefault.collectAsState()
    val perVerseOverrides by viewModel.perVerseArabicOverrides.collectAsState()

    val currentSurah = surah

    if (isLoading || currentSurah == null) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
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

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${currentSurah.placeAzeri ?: ""} • ${currentSurah.verseCount} ayə",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Bismillah Header (except Surah 9 At-Tawbah and Surah 1 where Bismillah is verse 1)
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
                items = verses,
                key = { "${it.surahIndex}_${it.verseNumber}" }
            ) { verse ->
                val isArabicVisible = perVerseOverrides[verse.verseNumber] ?: showArabicByDefault
                VerseCard(
                    verse = verse,
                    isArabicVisible = isArabicVisible,
                    selectedLanguage = selectedLanguage,
                    onToggleArabic = { viewModel.togglePerVerseArabic(verse.verseNumber) },
                    onBookmarkToggle = { viewModel.toggleBookmark(verse) },
                    onEditNote = { viewModel.openNoteDialog(verse) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        // Note Dialog Dialog
        editingVerse?.let { verseToEdit ->
            NoteDialog(
                verse = verseToEdit,
                onDismiss = { viewModel.closeNoteDialog() },
                onSaveNote = { noteText -> viewModel.saveNote(verseToEdit, noteText) },
                onDeleteNote = { viewModel.deleteNote(verseToEdit) }
            )
        }
    }
}
