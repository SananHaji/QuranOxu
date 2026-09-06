package az.sananhaji.quranoxu.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.sananhaji.quranoxu.domain.model.VerseEntity

@Composable
fun NoteDialog(
    verse: VerseEntity,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit,
    onDeleteNote: () -> Unit
) {
    var noteText by remember { mutableStateOf(verse.noteText ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ayə ${verse.verseNumber} üçün Not",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Bu ayə haqqında öz notlarınızı yazın:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Notunuzu bura daxil edin...") },
                    minLines = 3,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSaveNote(noteText) }
            ) {
                Text("Yadda saxla")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Ləğv et")
            }
        }
    )
}
