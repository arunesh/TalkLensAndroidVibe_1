package com.talkbox.docs.talklens.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.DownloadProgress
import com.talkbox.docs.talklens.domain.model.DownloadState
import com.talkbox.docs.talklens.domain.usecase.DownloadRequiredModelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Setup screen
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val downloadRequiredModelsUseCase: DownloadRequiredModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.LanguageSelection)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Language.SPANISH)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    fun onLanguageSelected(language: Language) {
        _selectedLanguage.value = language
    }

    fun startDownload() {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Downloading(emptyList())

            downloadRequiredModelsUseCase(_selectedLanguage.value)
                .catch { error ->
                    _uiState.value = SetupUiState.Error(error.message ?: "Download failed")
                }
                .collect { progressList ->
                    _uiState.value = SetupUiState.Downloading(progressList)

                    // Check if all downloads are complete
                    val allComplete = progressList.all { it.state == DownloadState.COMPLETED }
                    val anyFailed = progressList.any { it.state == DownloadState.FAILED }

                    when {
                        anyFailed -> {
                            val failedModel = progressList.first { it.state == DownloadState.FAILED }
                            _uiState.value = SetupUiState.Error(
                                failedModel.error ?: "Download failed"
                            )
                        }
                        allComplete -> {
                            _uiState.value = SetupUiState.Completed
                        }
                    }
                }
        }
    }

    fun retryDownload() {
        startDownload()
    }
}

/**
 * UI state for Setup screen
 */
sealed interface SetupUiState {
    data object LanguageSelection : SetupUiState
    data class Downloading(val progress: List<DownloadProgress>) : SetupUiState
    data class Error(val message: String) : SetupUiState
    data object Completed : SetupUiState
}
