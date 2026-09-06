package az.sananhaji.quranoxu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import az.sananhaji.quranoxu.data.preferences.SettingsPreferences
import az.sananhaji.quranoxu.data.repository.AudioRepositoryKmpImpl
import az.sananhaji.quranoxu.data.repository.QuranRepositoryKmpImpl
import az.sananhaji.quranoxu.domain.usecase.*
import az.sananhaji.quranoxu.presentation.components.AudioPlayerBar
import az.sananhaji.quranoxu.presentation.screens.*
import az.sananhaji.quranoxu.presentation.viewmodel.*
import az.sananhaji.quranoxu.ui.theme.QuranOxuTheme

@Composable
fun App() {
    val settings = remember { SettingsPreferences() }
    val quranRepo = remember { QuranRepositoryKmpImpl() }
    val audioRepo = remember { AudioRepositoryKmpImpl() }

    val controlAudioUseCase = remember { ControlAudioUseCase(audioRepo) }
    val getSurahsUseCase = remember { GetSurahsUseCase(quranRepo) }
    val getSurahDetailUseCase = remember { GetSurahDetailUseCase(quranRepo) }
    val toggleBookmarkUseCase = remember { ToggleBookmarkUseCase(quranRepo) }
    val saveNoteUseCase = remember { SaveNoteUseCase(quranRepo) }
    val deleteNoteUseCase = remember { DeleteNoteUseCase(quranRepo) }
    val getBookmarksUseCase = remember { GetBookmarksUseCase(quranRepo) }
    val getNotesUseCase = remember { GetNotesUseCase(quranRepo) }
    val getSupportedLanguagesUseCase = remember { GetSupportedLanguagesUseCase(quranRepo) }

    val mainViewModel = remember { MainViewModel(controlAudioUseCase, settings) }
    val surahListViewModel = remember { SurahListViewModel(getSurahsUseCase, quranRepo, controlAudioUseCase, settings) }
    val surahDetailViewModel = remember { SurahDetailViewModel(getSurahDetailUseCase, toggleBookmarkUseCase, saveNoteUseCase, deleteNoteUseCase, controlAudioUseCase, quranRepo, settings) }
    val bookmarksNotesViewModel = remember { BookmarksNotesViewModel(getBookmarksUseCase, getNotesUseCase, toggleBookmarkUseCase, saveNoteUseCase, deleteNoteUseCase) }
    val analyticsViewModel = remember { AnalyticsViewModel(quranRepo) }
    val settingsViewModel = remember { SettingsViewModel(getSupportedLanguagesUseCase, controlAudioUseCase, settings) }

    val settingsState by settingsViewModel.state.collectAsState()
    val selectedSurahNav by mainViewModel.selectedSurahNav.collectAsState()
    val audioState by mainViewModel.audioState.collectAsState()
    val activeTab by mainViewModel.activeTab.collectAsState()

    var isFullscreen by remember { mutableStateOf(false) }

    QuranOxuTheme(themeMode = settingsState.themeMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (selectedSurahNav == null && !isFullscreen) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = activeTab == 0,
                            onClick = { mainViewModel.setActiveTab(0) },
                            icon = { Icon(Icons.Default.Book, contentDescription = "Surələr") },
                            label = { Text("Surələr") }
                        )
                        NavigationBarItem(
                            selected = activeTab == 1,
                            onClick = { mainViewModel.setActiveTab(1) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Qeydlər") },
                            label = { Text("Qeydlər") }
                        )
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { mainViewModel.setActiveTab(2) },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Analiz") },
                            label = { Text("Analiz") }
                        )
                        NavigationBarItem(
                            selected = activeTab == 3,
                            onClick = { mainViewModel.setActiveTab(3) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Tənzimləmələr") },
                            label = { Text("Tənzimləmələr") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullscreen) PaddingValues(0.dp) else innerPadding)
            ) {
                val surahNav = selectedSurahNav
                when {
                    surahNav != null -> {
                        SurahDetailScreen(
                            viewModel = surahDetailViewModel,
                            surahIndex = surahNav.first,
                            initialVerseNumber = surahNav.second,
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = { isFullscreen = !isFullscreen },
                            onBack = { mainViewModel.clearSelectedSurah() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    activeTab == 0 -> {
                        SurahListScreen(
                            viewModel = surahListViewModel,
                            onSurahClick = { surahIndex, verseNumber ->
                                mainViewModel.openSurahDetail(surahIndex, verseNumber)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    activeTab == 1 -> {
                        BookmarksNotesScreen(
                            viewModel = bookmarksNotesViewModel,
                            onNavigateToSurah = { surahIndex, verseNumber ->
                                mainViewModel.openSurahDetail(surahIndex, verseNumber)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    activeTab == 2 -> {
                        AnalyticsScreen(
                            viewModel = analyticsViewModel,
                            onNavigateToSurah = { surahIndex, verseNum ->
                                mainViewModel.openSurahDetail(surahIndex, verseNum)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Persistent Floating Bottom Audio Player Bar
                AudioPlayerBar(
                    audioState = audioState,
                    onPause = { mainViewModel.pauseAudio() },
                    onResume = { mainViewModel.resumeAudio() },
                    onNext = { mainViewModel.nextAudio() },
                    onPrevious = { mainViewModel.previousAudio() },
                    onStop = { mainViewModel.stopAudio() },
                    onSetSleepTimer = { minutes -> mainViewModel.setSleepTimer(minutes) },
                    onCancelSleepTimer = { mainViewModel.cancelSleepTimer() },
                    onSetPlaybackSpeed = { speed -> mainViewModel.setPlaybackSpeed(speed) },
                    onBarClick = {
                        if (audioState.surahIndex > 0 && audioState.verseNumber > 0) {
                            mainViewModel.openSurahDetail(audioState.surahIndex, audioState.verseNumber)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = 620.dp)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}
