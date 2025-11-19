package com.talkbox.docs.talklens.domain.repository

import android.net.Uri
import com.talkbox.docs.talklens.domain.model.ImportFileType
import com.talkbox.docs.talklens.domain.model.ImportResult
import com.talkbox.docs.talklens.domain.model.ImportedFile
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import kotlinx.coroutines.flow.Flow

/**
 * Repository for file import operations
 */
interface FileImportRepository {
    /**
     * Import a file and convert it to a document
     * Emits progress updates during import
     */
    fun importFile(uri: Uri): Flow<ImportResult>

    /**
     * Get file information from URI
     */
    suspend fun getFileInfo(uri: Uri): Result<ImportedFile>

    /**
     * Get recently imported files
     */
    fun getRecentImports(): Flow<List<ImportedFile>>

    /**
     * Delete an imported file from history
     */
    suspend fun deleteImport(importedFile: ImportedFile): Result<Unit>
}
