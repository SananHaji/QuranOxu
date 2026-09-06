package az.sananhaji.quranoxu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        var showDetails by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("QuranOxu - Bismillah", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { showDetails = !showDetails }) {
                Text(if (showDetails) "Gizlət" else "Quran Oxu")
            }
            AnimatedVisibility(showDetails) {
                Text("Müqəddəs Quran və Azərbaycan dilində tərcüməsi")
            }
        }
    }
}
