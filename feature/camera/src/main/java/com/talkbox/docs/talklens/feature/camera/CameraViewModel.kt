package com.talkbox.docs.talklens.feature.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.data.cache.DocumentCache
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.model.Page
import com.talkbox.docs.talklens.domain.model.RecognizedText
import com.talkbox.docs.talklens.domain.usecase.ManagePagesUseCase
import com.talkbox.docs.talklens.domain.usecase.RecognizeTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Camera screen with multi-page support
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val recognizeTextUseCase: RecognizeTextUseCase,
    private val managePagesUseCase: ManagePagesUseCase,
    private val documentCache: DocumentCache
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled.asStateFlow()

    private val _multiPageMode = MutableStateFlow(false)
    val multiPageMode: StateFlow<Boolean> = _multiPageMode.asStateFlow()

    private val _currentDocument = MutableStateFlow<MultiPageDocument?>(null)
    val currentDocument: StateFlow<MultiPageDocument?> = _currentDocument.asStateFlow()

    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Processing

            recognizeTextUseCase(bitmap).fold(
                onSuccess = { recognizedText ->
                    if (recognizedText.text.isBlank()) {
                        _uiState.value = CameraUiState.Error("No text detected. Please try again.")
                    } else {
                        if (_multiPageMode.value) {
                            // Add page to current document
                            addPageToDocument(bitmap, recognizedText)
                        } else {
                            // Single page mode
                            _uiState.value = CameraUiState.Success(
                                capturedImage = bitmap,
                                recognizedText = recognizedText
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = CameraUiState.Error(
                        error.message ?: "Failed to recognize text"
                    )
                }
            )
        }
    }

    private fun addPageToDocument(bitmap: Bitmap, recognizedText: RecognizedText) {
        val newPage = Page(
            id = UUID.randomUUID().toString(),
            pageNumber = (_currentDocument.value?.pageCount ?: 0) + 1,
            image = bitmap,
            recognizedText = recognizedText
        )

        val document = _currentDocument.value ?: MultiPageDocument(
            id = UUID.randomUUID().toString(),
            pages = emptyList()
        )

        val updatedDocument = managePagesUseCase.addPage(document, newPage)
        _currentDocument.value = updatedDocument

        // Store in cache for navigation
        documentCache.putDocument(updatedDocument)

        _uiState.value = CameraUiState.MultiPageCapture(
            document = updatedDocument,
            latestPage = newPage
        )
    }

    fun enableMultiPageMode() {
        _multiPageMode.value = true
        _currentDocument.value = MultiPageDocument(
            id = UUID.randomUUID().toString(),
            pages = emptyList()
        )
    }

    fun disableMultiPageMode() {
        _multiPageMode.value = false
        _currentDocument.value = null
        _uiState.value = CameraUiState.Idle
    }

    fun deletePage(pageId: String) {
        val document = _currentDocument.value ?: return
        managePagesUseCase.deletePage(document, pageId).fold(
            onSuccess = { updatedDocument ->
                _currentDocument.value = updatedDocument
                if (updatedDocument.pages.isEmpty()) {
                    _uiState.value = CameraUiState.Idle
                } else {
                    _uiState.value = CameraUiState.MultiPageCapture(
                        document = updatedDocument,
                        latestPage = updatedDocument.pages.lastOrNull()
                    )
                }
            },
            onFailure = { error ->
                _uiState.value = CameraUiState.Error(
                    error.message ?: "Failed to delete page"
                )
            }
        )
    }

    fun reorderPages(fromIndex: Int, toIndex: Int) {
        val document = _currentDocument.value ?: return
        managePagesUseCase.reorderPages(document, fromIndex, toIndex).fold(
            onSuccess = { updatedDocument ->
                _currentDocument.value = updatedDocument
            },
            onFailure = { error ->
                _uiState.value = CameraUiState.Error(
                    error.message ?: "Failed to reorder pages"
                )
            }
        )
    }

    fun continueCapturing() {
        _uiState.value = CameraUiState.Idle
    }

    fun toggleFlash() {
        _flashEnabled.update { !it }
    }

    fun resetToIdle() {
        _uiState.value = CameraUiState.Idle
    }

    fun retryCapture() {
        _uiState.value = CameraUiState.Idle
    }
}

/**
 * UI state for Camera screen
 */
sealed interface CameraUiState {
    data object Idle : CameraUiState
    data object Processing : CameraUiState

    /**
     * Single page capture success
     */
    data class Success(
        val capturedImage: Bitmap,
        val recognizedText: RecognizedText
    ) : CameraUiState

    /**
     * Multi-page capture mode - shows captured pages
     */
    data class MultiPageCapture(
        val document: MultiPageDocument,
        val latestPage: Page?
    ) : CameraUiState

    data class Error(val message: String) : CameraUiState
}
