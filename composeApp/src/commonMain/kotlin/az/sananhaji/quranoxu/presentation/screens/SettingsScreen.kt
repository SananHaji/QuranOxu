package az.sananhaji.quranoxu.presentation.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.sananhaji.quranoxu.presentation.mvi.SettingsIntent
import az.sananhaji.quranoxu.presentation.viewmodel.SettingsViewModel
import az.sananhaji.quranoxu.ui.theme.ThemeMode

import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showClearCacheDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        // Section 1: Translation Language Selection (from Database)
        Text(
            text = "Tərcümə Dili (Baza Dilləri)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Verilənlər bazasından avtomatik aşkar edilən dillər:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                state.supportedLanguages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.processIntent(SettingsIntent.SetLanguage(lang.code)) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedLanguage == lang.code,
                            onClick = { viewModel.processIntent(SettingsIntent.SetLanguage(lang.code)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (state.selectedLanguage == lang.code) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: Audio Recitation Language Selection
        Text(
            text = "Səsli Oxunuş (Qiraət Dili)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                listOf(
                    "arabic" to "Ərəbcə (Mişari Rəşid Əl-Əlafəsi)",
                    "azerbaijani" to "Azərbaycanca (Rasim Balayev - Səsli Qiraət)"
                ).forEach { (code, title) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.processIntent(SettingsIntent.SetAudioLanguage(code)) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.audioLanguage == code,
                            onClick = { viewModel.processIntent(SettingsIntent.SetAudioLanguage(code)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (state.audioLanguage == code) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 3: Audio Cache & Offline Storage Management
        Text(
            text = "Səs Keşi və Oflayn Yaddaş",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Yüklənmiş və avtomatik buferlənmiş səs faylları internet olmadan dinləməyə imkan verir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lokal Yaddaş Həcmi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.audioCacheInfo.formattedSize} (${state.audioCacheInfo.downloadedSurahsCount} surə oflayn saxlanılır)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showClearCacheDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bütün Səs Keşini Təmizlə",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 4: Arabic Text Preferences
        Text(
            text = "Ərəbcə Mətn Tənzimləmələri",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Orijinal ərəbcə mətni göstər",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hər ayənin daxilində ilkin olaraq ərəbcə mətni göstərir (Həmçinin ayə kartındakı 'Ərəbcə' düyməsi ilə tək-tək dəyişə bilərsiniz)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = state.showArabicByDefault,
                    onCheckedChange = { viewModel.processIntent(SettingsIntent.SetShowArabicByDefault(it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 5: App Theme Mode
        Text(
            text = "Tətbiq Görünüşü (Mövzu)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                listOf(
                    Triple(ThemeMode.SYSTEM, "Sistem standartı", Icons.Default.BrightnessAuto),
                    Triple(ThemeMode.LIGHT, "İşıqlı rejim (Light)", Icons.Default.LightMode),
                    Triple(ThemeMode.DARK, "Qaranlıq rejim (Dark)", Icons.Default.DarkMode)
                ).forEach { (mode, title, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.processIntent(SettingsIntent.SetThemeMode(mode)) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.processIntent(SettingsIntent.SetThemeMode(mode)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (state.themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (state.themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 80.dp))
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Audio keşi təmizlənsin?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Bütün surələr üzrə yüklənmiş və buferlənmiş səs faylları daxili yaddaşdan silinəcək (${state.audioCacheInfo.formattedSize}). Təsdiq edirsiniz?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        viewModel.processIntent(SettingsIntent.ClearAllAudioCache)
                    }
                ) {
                    Text("Təmizlə", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Ləğv et")
                }
            }
        )
    }
}
}
