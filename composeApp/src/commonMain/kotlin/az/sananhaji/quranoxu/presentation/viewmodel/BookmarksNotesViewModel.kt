package az.sananhaji.quranoxu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.sananhaji.quranoxu.domain.usecase.DeleteNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.GetBookmarksUseCase
import az.sananhaji.quranoxu.domain.usecase.GetNotesUseCase
import az.sananhaji.quranoxu.domain.usecase.SaveNoteUseCase
import az.sananhaji.quranoxu.domain.usecase.ToggleBookmarkUseCase
import az.sananhaji.quranoxu.presentation.mvi.BookmarksNotesIntent
import az.sananhaji.quranoxu.presentation.mvi.BookmarksNotesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarksNotesViewModel(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksNotesState())
    val state: StateFlow<BookmarksNotesState> = _state.asStateFlow()

    init {
        processIntent(BookmarksNotesIntent.LoadData)
    }

    fun processIntent(intent: BookmarksNotesIntent) {
        when (intent) {
            is BookmarksNotesIntent.LoadData -> {
                viewModelScope.launch {
                    val bookmarks = getBookmarksUseCase()
                    val notes = getNotesUseCase()
                    _state.value = _state.value.copy(
                        bookmarks = bookmarks,
                        notes = notes
                    )
                }
            }
            is BookmarksNotesIntent.SelectTab -> {
                _state.value = _state.value.copy(selectedTab = intent.tabIndex)
            }
            is BookmarksNotesIntent.DeleteBookmark -> {
                viewModelScope.launch {
                    toggleBookmarkUseCase(intent.bookmark.surahIndex, intent.bookmark.verseNumber, intent.bookmark.surahName)
                    processIntent(BookmarksNotesIntent.LoadData)
                }
            }
            is BookmarksNotesIntent.OpenEditNoteDialog -> {
                _state.value = _state.value.copy(editingNote = intent.note)
            }
            is BookmarksNotesIntent.CloseEditNoteDialog -> {
                _state.value = _state.value.copy(editingNote = null)
            }
            is BookmarksNotesIntent.SaveEditedNote -> {
                viewModelScope.launch {
                    saveNoteUseCase(intent.note.surahIndex, intent.note.verseNumber, intent.note.surahName, intent.newText)
                    _state.value = _state.value.copy(editingNote = null)
                    processIntent(BookmarksNotesIntent.LoadData)
                }
            }
            is BookmarksNotesIntent.DeleteNote -> {
                viewModelScope.launch {
                    deleteNoteUseCase(intent.note.surahIndex, intent.note.verseNumber)
                    _state.value = _state.value.copy(editingNote = null)
                    processIntent(BookmarksNotesIntent.LoadData)
                }
            }
        }
    }
}
