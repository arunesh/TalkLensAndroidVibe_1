package com.talkbox.docs.talklens.domain.usecase

import android.net.Uri
import com.talkbox.docs.talklens.domain.model.ImportResult
import com.talkbox.docs.talklens.domain.repository.FileImportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for importing files from gallery or file system
 */
class ImportFileUseCase @Inject constructor(
    private val fileImportRepository: FileImportRepository
) {
    /**
     * Import a file and convert to document with OCR
     * Returns a flow of import progress
     */
    operator fun invoke(uri: Uri): Flow<ImportResult> {
        return fileImportRepository.importFile(uri)
    }
}
