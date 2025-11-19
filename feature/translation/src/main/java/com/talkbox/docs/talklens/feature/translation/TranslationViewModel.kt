package com.talkbox.docs.talklens.feature.translation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.TranslatedText
import com.talkbox.docs.talklens.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Translation screen
 */
@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<TranslationUiState>(TranslationUiState.Idle)
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    private val _sourceLanguage = MutableStateFlow(Language.SPANISH)
    val sourceLanguage: StateFlow<Language> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.ENGLISH)
    val targetLanguage: StateFlow<Language> = _targetLanguage.asStateFlow()

    fun setSourceText(text: String) {
        _uiState.value = TranslationUiState.Ready(text)
    }

    fun onSourceLanguageChanged(language: Language) {
        _sourceLanguage.value = language
        // Re-translate if we have text
        val currentState = _uiState.value
        if (currentState is TranslationUiState.Success) {
            translateText(currentState.sourceText)
        }
    }

    fun onTargetLanguageChanged(language: Language) {
        _targetLanguage.value = language
        // Re-translate if we have text
        val currentState = _uiState.value
        if (currentState is TranslationUiState.Success) {
            translateText(currentState.sourceText)
        }
    }

    fun translateText(text: String) {
        viewModelScope.launch {
            _uiState.value = TranslationUiState.Translating(text)

            translateTextUseCase(
                text = text,
                sourceLanguage = _sourceLanguage.value,
                targetLanguage = _targetLanguage.value
            ).fold(
                onSuccess = { translatedText ->
                    _uiState.value = TranslationUiState.Success(
                        sourceText = text,
                        translatedText = translatedText
                    )
                },
                onFailure = { error ->
                    _uiState.value = TranslationUiState.Error(
                        sourceText = text,
                        message = error.message ?: "Translation failed"
                    )
                }
            )
        }
    }

    fun swapLanguages() {
        val currentState = _uiState.value
        if (currentState is TranslationUiState.Success) {
            // Swap languages
            val tempSource = _sourceLanguage.value
            _sourceLanguage.value = _targetLanguage.value
            _targetLanguage.value = tempSource

            // Swap texts and translate
            translateText(currentState.translatedText.text)
        }
    }

    fun retryTranslation() {
        val currentState = _uiState.value
        if (currentState is TranslationUiState.Error) {
            translateText(currentState.sourceText)
        }
    }
}

/**
 * UI state for Translation screen
 */
sealed interface TranslationUiState {
    data object Idle : TranslationUiState
    data class Ready(val sourceText: String) : TranslationUiState
    data class Translating(val sourceText: String) : TranslationUiState
    data class Success(
        val sourceText: String,
        val translatedText: TranslatedText
    ) : TranslationUiState
    data class Error(
        val sourceText: String,
        val message: String
    ) : TranslationUiState
}
