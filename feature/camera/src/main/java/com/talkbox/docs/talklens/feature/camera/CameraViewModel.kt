package com.talkbox.docs.talklens.feature.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.domain.model.RecognizedText
import com.talkbox.docs.talklens.domain.usecase.RecognizeTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Camera screen
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val recognizeTextUseCase: RecognizeTextUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled.asStateFlow()

    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Processing

            recognizeTextUseCase(bitmap).fold(
                onSuccess = { recognizedText ->
                    if (recognizedText.text.isBlank()) {
                        _uiState.value = CameraUiState.Error("No text detected. Please try again.")
                    } else {
                        _uiState.value = CameraUiState.Success(
                            capturedImage = bitmap,
                            recognizedText = recognizedText
                        )
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
    data class Success(
        val capturedImage: Bitmap,
        val recognizedText: RecognizedText
    ) : CameraUiState
    data class Error(val message: String) : CameraUiState
}
