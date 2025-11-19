package com.talkbox.docs.talklens.feature.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.data.cache.DocumentCache
import com.talkbox.docs.talklens.domain.model.ImportResult
import com.talkbox.docs.talklens.domain.model.ImportedFile
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.repository.FileImportRepository
import com.talkbox.docs.talklens.domain.usecase.ImportFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gallery screen
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val importFileUseCase: ImportFileUseCase,
    private val fileImportRepository: FileImportRepository,
    private val documentCache: DocumentCache
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Idle)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _recentImports = MutableStateFlow<List<ImportedFile>>(emptyList())
    val recentImports: StateFlow<List<ImportedFile>> = _recentImports.asStateFlow()

    init {
        loadRecentImports()
    }

    private fun loadRecentImports() {
        viewModelScope.launch {
            fileImportRepository.getRecentImports().collect { imports ->
                _recentImports.value = imports
            }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            importFileUseCase(uri).collect { result ->
                when (result) {
                    is ImportResult.InProgress -> {
                        _uiState.value = GalleryUiState.Importing(
                            progress = result.progress,
                            currentPage = result.currentPage,
                            totalPages = result.totalPages,
                            fileName = result.fileName
                        )
                    }

                    is ImportResult.Success -> {
                        // Store document in cache
                        documentCache.putDocument(result.document)

                        _uiState.value = GalleryUiState.ImportSuccess(
                            document = result.document,
                            fileName = result.fileName
                        )

                        // Reload recent imports
                        loadRecentImports()
                    }

                    is ImportResult.Error -> {
                        _uiState.value = GalleryUiState.Error(
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = GalleryUiState.Idle
    }

    fun deleteImport(importedFile: ImportedFile) {
        viewModelScope.launch {
            fileImportRepository.deleteImport(importedFile)
            loadRecentImports()
        }
    }
}

/**
 * UI state for Gallery screen
 */
sealed interface GalleryUiState {
    data object Idle : GalleryUiState

    data class Importing(
        val progress: Float,
        val currentPage: Int,
        val totalPages: Int,
        val fileName: String
    ) : GalleryUiState

    data class ImportSuccess(
        val document: MultiPageDocument,
        val fileName: String
    ) : GalleryUiState

    data class Error(
        val message: String
    ) : GalleryUiState
}
