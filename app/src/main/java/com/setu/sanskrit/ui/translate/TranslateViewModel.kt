package com.setu.sanskrit.ui.translate

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.setu.sanskrit.AppContainer
import com.setu.sanskrit.data.TranslationRepository
import com.setu.sanskrit.domain.model.TargetLanguage
import com.setu.sanskrit.domain.model.TranslationResult
import com.setu.sanskrit.ocr.TextScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TranslateStatus {
    data object Idle : TranslateStatus
    data object Loading : TranslateStatus
    data class Success(val result: TranslationResult) : TranslateStatus
    data class Error(val message: String) : TranslateStatus
}

data class TranslateUiState(
    val input: String = "",
    val language: TargetLanguage = TargetLanguage.MARATHI,
    val status: TranslateStatus = TranslateStatus.Idle,
    val scanning: Boolean = false,
)

/** All screen logic lives here. The screen only displays `state` and calls these functions. */
class TranslateViewModel(
    private val repository: TranslationRepository,
    private val scanner: TextScanner,
) : ViewModel() {

    private val _state = MutableStateFlow(TranslateUiState())
    val state: StateFlow<TranslateUiState> = _state.asStateFlow()

    fun onInputChange(text: String) = _state.update { it.copy(input = text) }
    fun onLanguageChange(lang: TargetLanguage) = _state.update { it.copy(language = lang) }
    fun clear() = _state.update { TranslateUiState(language = it.language) }

    fun translate() {
        val text = _state.value.input.trim()
        if (text.isEmpty()) return
        _state.update { it.copy(status = TranslateStatus.Loading) }
        viewModelScope.launch {
            repository.translate(text, _state.value.language).fold(
                onSuccess = { r -> _state.update { it.copy(status = TranslateStatus.Success(r)) } },
                onFailure = { e ->
                    _state.update { it.copy(status = TranslateStatus.Error(e.message ?: "Something went wrong.")) }
                },
            )
        }
    }

    fun scan(uri: Uri) {
        _state.update { it.copy(scanning = true) }
        viewModelScope.launch {
            runCatching { scanner.recognize(uri) }
                .onSuccess { text ->
                    _state.update {
                        if (text.isBlank())
                            it.copy(scanning = false, status = TranslateStatus.Error("No text found in that image. Try a sharper, well-lit photo."))
                        else it.copy(scanning = false, input = text, status = TranslateStatus.Idle)
                    }
                }
                .onFailure {
                    _state.update { s -> s.copy(scanning = false, status = TranslateStatus.Error("Couldn't read that image.")) }
                }
        }
    }

    /** Called when the user taps an item in History. */
    fun showResult(result: TranslationResult) = _state.update {
        TranslateUiState(input = result.source, language = result.language, status = TranslateStatus.Success(result))
    }

    class Factory(private val c: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TranslateViewModel(c.repository, c.scanner) as T
    }
}
