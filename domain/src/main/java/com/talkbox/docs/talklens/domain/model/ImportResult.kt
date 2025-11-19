package com.talkbox.docs.talklens.domain.model

import android.net.Uri

/**
 * Result of file import operation
 */
sealed interface ImportResult {
    /**
     * Import in progress
     */
    data class InProgress(
        val currentPage: Int,
        val totalPages: Int,
        val fileName: String
    ) : ImportResult {
        val progress: Float
            get() = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
    }

    /**
     * Import completed successfully
     */
    data class Success(
        val document: MultiPageDocument,
        val fileName: String,
        val fileType: ImportFileType
    ) : ImportResult

    /**
     * Import failed
     */
    data class Error(
        val message: String,
        val fileName: String? = null
    ) : ImportResult
}

/**
 * Represents an imported file ready for processing
 */
data class ImportedFile(
    val uri: Uri,
    val fileName: String,
    val fileType: ImportFileType,
    val fileSize: Long,
    val importedAt: Long = System.currentTimeMillis()
)
