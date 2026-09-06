package az.sananhaji.quranoxu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import android.content.res.Configuration
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import az.sananhaji.quranoxu.data.db.QuranDatabaseHelper
import az.sananhaji.quranoxu.data.preferences.SettingsPreferences
import az.sananhaji.quranoxu.data.repository.AudioRepositoryImpl
import az.sananhaji.quranoxu.data.repository.QuranRepositoryImpl
import az.sananhaji.quranoxu.domain.usecase.ControlAudioUseCase
import az.sananhaji.quranoxu.domain.usecase.DeleteNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.GetBookmarksUseCase
import az.sananhaji.quranoxu.domain.usecase.GetNotesUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSupportedLanguagesUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSurahDetailUseCase
import az.sananhaji.quranoxu.domain.usecase.GetSurahsUseCase
import az.sananhaji.quranoxu.domain.usecase.SaveNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.ToggleBookmarkUseCase
import az.sananhaji.quranoxu.presentation.components.AudioPlayerBar
import az.sananhaji.quranoxu.presentation.screens.AnalyticsScreen
import az.sananhaji.quranoxu.presentation.screens.BookmarksNotesScreen
import az.sananhaji.quranoxu.presentation.screens.SettingsScreen
import az.sananhaji.quranoxu.presentation.screens.SurahDetailScreen
import az.sananhaji.quranoxu.presentation.screens.SurahListScreen
import az.sananhaji.quranoxu.presentation.viewmodel.AnalyticsViewModel
import az.sananhaji.quranoxu.presentation.viewmodel.BookmarksNotesViewModel
import az.sananhaji.quranoxu.presentation.viewmodel.MainViewModel
import az.sananhaji.quranoxu.presentation.viewmodel.SettingsViewModel
import az.sananhaji.quranoxu.presentation.viewmodel.SurahDetailViewModel
import az.sananhaji.quranoxu.presentation.viewmodel.SurahListViewModel
import az.sananhaji.quranoxu.service.ReminderManager
import az.sananhaji.quranoxu.ui.theme.QuranOxuTheme
import az.sananhaji.quranoxu.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val dbHelper by lazy { QuranDatabaseHelper(applicationContext) }
    private val quranRepository by lazy { QuranRepositoryImpl(dbHelper) }
    private val audioRepository by lazy { AudioRepositoryImpl(applicationContext) }
    private val settingsPreferences by lazy { SettingsPreferences(applicationContext) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(
            controlAudioUseCase = ControlAudioUseCase(audioRepository),
            settingsPreferences = settingsPreferences
        )
    }

    private val surahListViewModel: SurahListViewModel by viewModels {
        SurahListViewModel.Factory(
            getSurahsUseCase = GetSurahsUseCase(quranRepository),
            quranRepository = quranRepository,
            controlAudioUseCase = ControlAudioUseCase(audioRepository),
            settingsPreferences = settingsPreferences
        )
    }

    private val surahDetailViewModel: SurahDetailViewModel by viewModels {
        SurahDetailViewModel.Factory(
            getSurahDetailUseCase = GetSurahDetailUseCase(quranRepository),
            toggleBookmarkUseCase = ToggleBookmarkUseCase(quranRepository),
            saveNoteUseCase = SaveNoteUseCase(quranRepository),
            deleteNoteUseCase = DeleteNoteUseCase(quranRepository),
            controlAudioUseCase = ControlAudioUseCase(audioRepository),
            quranRepository = quranRepository,
            settingsPreferences = settingsPreferences
        )
    }

    private val bookmarksNotesViewModel: BookmarksNotesViewModel by viewModels {
        BookmarksNotesViewModel.Factory(
            getBookmarksUseCase = GetBookmarksUseCase(quranRepository),
            getNotesUseCase = GetNotesUseCase(quranRepository),
            toggleBookmarkUseCase = ToggleBookmarkUseCase(quranRepository),
            saveNoteUseCase = SaveNoteUseCase(quranRepository),
            deleteNoteUseCase = DeleteNoteUseCase(quranRepository)
        )
    }

    private val analyticsViewModel: AnalyticsViewModel by viewModels {
        AnalyticsViewModel.Factory(
            quranRepository = quranRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            getSupportedLanguagesUseCase = GetSupportedLanguagesUseCase(quranRepository),
            controlAudioUseCase = ControlAudioUseCase(audioRepository),
            settingsPreferences = settingsPreferences
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule daily reading reminder notification at 20:00 (8 PM)
        ReminderManager.scheduleDailyReminder(applicationContext)

        handleWidgetIntent(intent)

        setContent {
            val settingsState by settingsViewModel.state.collectAsState()

            QuranOxuTheme(themeMode = settingsState.themeMode) {
                MainAppContent(
                    mainViewModel = mainViewModel,
                    surahListViewModel = surahListViewModel,
                    surahDetailViewModel = surahDetailViewModel,
                    bookmarksNotesViewModel = bookmarksNotesViewModel,
                    analyticsViewModel = analyticsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: android.content.Intent?) {
        intent?.let {
            val surahIndex = it.getIntExtra("surah_index", -1)
            val verseNumber = it.getIntExtra("verse_number", -1)
            if (surahIndex > 0) {
                mainViewModel.openSurahDetail(surahIndex, if (verseNumber > 0) verseNumber else null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    mainViewModel: MainViewModel,
    surahListViewModel: SurahListViewModel,
    surahDetailViewModel: SurahDetailViewModel,
    bookmarksNotesViewModel: BookmarksNotesViewModel,
    analyticsViewModel: AnalyticsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val selectedSurahNav by mainViewModel.selectedSurahNav.collectAsState()
    val audioState by mainViewModel.audioState.collectAsState()
    val activeTab by mainViewModel.activeTab.collectAsState()
    val settingsState by settingsViewModel.state.collectAsState()
    val surahDetailState by surahDetailViewModel.state.collectAsState()

    var isFullscreen by remember { mutableStateOf(false) }

    // Request Notification Permission on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {}
        )
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle System Back Press when viewing a Surah detail
    BackHandler(enabled = selectedSurahNav != null) {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            mainViewModel.clearSelectedSurah()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isLandscape && selectedSurahNav == null && !isFullscreen) {
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    if (isFullscreen) PaddingValues(0.dp)
                    else if (isLandscape) PaddingValues(0.dp)
                    else innerPadding
                )
        ) {
            if (isLandscape && selectedSurahNav == null && !isFullscreen) {
                NavigationRail(
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NavigationRailItem(
                            selected = activeTab == 0,
                            onClick = { mainViewModel.setActiveTab(0) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = "Surələr",
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Surələr",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true
                        )
                        NavigationRailItem(
                            selected = activeTab == 1,
                            onClick = { mainViewModel.setActiveTab(1) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Qeydlər",
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Qeydlər",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true
                        )
                        NavigationRailItem(
                            selected = activeTab == 2,
                            onClick = { mainViewModel.setActiveTab(2) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analiz",
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Analiz",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true
                        )
                        NavigationRailItem(
                            selected = activeTab == 3,
                            onClick = { mainViewModel.setActiveTab(3) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Tənzimləmələr",
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Tənzimləmələr",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        if (isLandscape && !isFullscreen) {
                            PaddingValues(
                                top = innerPadding.calculateTopPadding(),
                                bottom = 0.dp
                            )
                        } else PaddingValues(0.dp)
                    )
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

                // Floating Bottom Audio Player Bar
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