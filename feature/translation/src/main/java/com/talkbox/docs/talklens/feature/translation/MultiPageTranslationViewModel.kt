package com.talkbox.docs.talklens.feature.translation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.data.cache.DocumentCache
import com.talkbox.docs.talklens.domain.model.BatchTranslationResult
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.usecase.BatchTranslateDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for multi-page document translation
 */
@HiltViewModel
class MultiPageTranslationViewModel @Inject constructor(
    private val batchTranslateDocumentUseCase: BatchTranslateDocumentUseCase,
    private val documentCache: DocumentCache,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentId: String = checkNotNull(savedStateHandle["documentId"])

    private val _uiState = MutableStateFlow<MultiPageTranslationUiState>(
        MultiPageTranslationUiState.Loading
    )
    val uiState: StateFlow<MultiPageTranslationUiState> = _uiState.asStateFlow()

    private val _sourceLanguage = MutableStateFlow(Language.ENGLISH)
    val sourceLanguage: StateFlow<Language> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.SPANISH)
    val targetLanguage: StateFlow<Language> = _targetLanguage.asStateFlow()

    private var currentDocument: MultiPageDocument? = null

    init {
        // Load document from cache
        loadDocument()
    }

    private fun loadDocument() {
        val document = documentCache.getDocument(documentId)
        if (document != null) {
            currentDocument = document
            _uiState.value = MultiPageTranslationUiState.Ready(document)
        } else {
            _uiState.value = MultiPageTranslationUiState.Error(
                message = "Document not found. Please try capturing again."
            )
        }
    }

    fun setSourceLanguage(language: Language) {
        _sourceLanguage.value = language
    }

    fun setTargetLanguage(language: Language) {
        _targetLanguage.value = language
    }

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp

        // If we have a translated document, swap it back
        val state = _uiState.value
        if (state is MultiPageTranslationUiState.Success) {
            // Reset to ready to allow re-translation
            _uiState.value = MultiPageTranslationUiState.Ready(
                document = currentDocument ?: state.translatedDocument
            )
        }
    }

    fun translateDocument() {
        val document = currentDocument ?: run {
            _uiState.value = MultiPageTranslationUiState.Error(
                message = "No document to translate"
            )
            return
        }

        viewModelScope.launch {
            batchTranslateDocumentUseCase(
                document = document,
                sourceLanguage = _sourceLanguage.value,
                targetLanguage = _targetLanguage.value
            ).collect { result ->
                when (result) {
                    is BatchTranslationResult.InProgress -> {
                        _uiState.value = MultiPageTranslationUiState.Translating(
                            progress = result.progress,
                            currentPage = result.currentPage,
                            totalPages = result.totalPages
                        )
                    }

                    is BatchTranslationResult.Success -> {
                        currentDocument = result.document
                        _uiState.value = MultiPageTranslationUiState.Success(
                            translatedDocument = result.document
                        )
                    }

                    is BatchTranslationResult.Error -> {
                        _uiState.value = MultiPageTranslationUiState.Error(
                            message = result.message,
                            partiallyTranslatedPages = result.partiallyTranslatedPages
                        )
                    }
                }
            }
        }
    }

    fun retry() {
        val document = currentDocument
        if (document != null) {
            _uiState.value = MultiPageTranslationUiState.Ready(document)
        }
    }
}

/**
 * UI state for multi-page translation
 */
sealed interface MultiPageTranslationUiState {
    data object Loading : MultiPageTranslationUiState

    data class Ready(
        val document: MultiPageDocument
    ) : MultiPageTranslationUiState

    data class Translating(
        val progress: Float,
        val currentPage: Int,
        val totalPages: Int
    ) : MultiPageTranslationUiState

    data class Success(
        val translatedDocument: MultiPageDocument
    ) : MultiPageTranslationUiState

    data class Error(
        val message: String,
        val partiallyTranslatedPages: List<com.talkbox.docs.talklens.domain.model.Page> = emptyList()
    ) : MultiPageTranslationUiState
}
