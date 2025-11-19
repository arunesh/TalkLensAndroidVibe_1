package com.talkbox.docs.talklens.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.talkbox.docs.talklens.domain.model.ImportFileType
import com.talkbox.docs.talklens.domain.model.ImportResult
import com.talkbox.docs.talklens.domain.model.ImportedFile
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.model.Page
import com.talkbox.docs.talklens.domain.repository.FileImportRepository
import com.talkbox.docs.talklens.domain.repository.TextRecognitionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FileImportRepository
 */
@Singleton
class FileImportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val textRecognitionRepository: TextRecognitionRepository
) : FileImportRepository {

    // In-memory storage for recent imports
    private val recentImports = mutableListOf<ImportedFile>()

    override fun importFile(uri: Uri): Flow<ImportResult> = flow {
        try {
            // Get file info
            val fileInfo = getFileInfo(uri).getOrThrow()

            when (fileInfo.fileType) {
                ImportFileType.IMAGE_JPEG, ImportFileType.IMAGE_PNG -> {
                    importImageFile(uri, fileInfo)
                        .collect { emit(it) }
                }
                ImportFileType.PDF -> {
                    importPdfFile(uri, fileInfo)
                        .collect { emit(it) }
                }
            }

            // Add to recent imports
            recentImports.add(0, fileInfo)
            if (recentImports.size > 20) {
                recentImports.removeAt(recentImports.size - 1)
            }
        } catch (e: Exception) {
            emit(ImportResult.Error(
                message = e.message ?: "Failed to import file",
                fileName = null
            ))
        }
    }

    override suspend fun getFileInfo(uri: Uri): Result<ImportedFile> {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

                    val fileName = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                    val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L

                    // Determine file type
                    val mimeType = context.contentResolver.getType(uri)
                    val fileType = ImportFileType.fromMimeType(mimeType)
                        ?: run {
                            val extension = fileName.substringAfterLast('.', "")
                            ImportFileType.fromExtension(extension)
                        }
                        ?: throw IllegalArgumentException("Unsupported file type: $mimeType")

                    Result.success(
                        ImportedFile(
                            uri = uri,
                            fileName = fileName,
                            fileType = fileType,
                            fileSize = fileSize
                        )
                    )
                } else {
                    Result.failure(IllegalArgumentException("File not found"))
                }
            } ?: Result.failure(IllegalArgumentException("Unable to query file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRecentImports(): Flow<List<ImportedFile>> = flow {
        emit(recentImports.toList())
    }

    override suspend fun deleteImport(importedFile: ImportedFile): Result<Unit> {
        return try {
            recentImports.remove(importedFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun importImageFile(uri: Uri, fileInfo: ImportedFile): Flow<ImportResult> = flow {
        emit(ImportResult.InProgress(
            currentPage = 1,
            totalPages = 1,
            fileName = fileInfo.fileName
        ))

        val bitmap = loadBitmapFromUri(uri)
            ?: throw IllegalArgumentException("Failed to load image")

        // Recognize text
        val recognitionResult = textRecognitionRepository.recognizeText(bitmap)
        val recognizedText = recognitionResult.getOrThrow()

        // Create document
        val page = Page(
            id = UUID.randomUUID().toString(),
            pageNumber = 1,
            image = bitmap,
            recognizedText = recognizedText
        )

        val document = MultiPageDocument(
            id = UUID.randomUUID().toString(),
            pages = listOf(page)
        )

        emit(ImportResult.Success(
            document = document,
            fileName = fileInfo.fileName,
            fileType = fileInfo.fileType
        ))
    }

    private fun importPdfFile(uri: Uri, fileInfo: ImportedFile): Flow<ImportResult> = flow {
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("Failed to open PDF file")

        fileDescriptor.use { fd ->
            val pdfRenderer = PdfRenderer(fd)

            try {
                val totalPages = pdfRenderer.pageCount
                val pages = mutableListOf<Page>()

                for (pageIndex in 0 until totalPages) {
                    emit(ImportResult.InProgress(
                        currentPage = pageIndex + 1,
                        totalPages = totalPages,
                        fileName = fileInfo.fileName
                    ))

                    val pdfPage = pdfRenderer.openPage(pageIndex)

                    // Render page to bitmap
                    val bitmap = Bitmap.createBitmap(
                        pdfPage.width,
                        pdfPage.height,
                        Bitmap.Config.ARGB_8888
                    )
                    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pdfPage.close()

                    // Recognize text
                    val recognitionResult = textRecognitionRepository.recognizeText(bitmap)
                    val recognizedText = recognitionResult.getOrNull()

                    pages.add(
                        Page(
                            id = UUID.randomUUID().toString(),
                            pageNumber = pageIndex + 1,
                            image = bitmap,
                            recognizedText = recognizedText
                        )
                    )
                }

                pdfRenderer.close()

                val document = MultiPageDocument(
                    id = UUID.randomUUID().toString(),
                    pages = pages
                )

                emit(ImportResult.Success(
                    document = document,
                    fileName = fileInfo.fileName,
                    fileType = fileInfo.fileType
                ))
            } catch (e: Exception) {
                pdfRenderer.close()
                throw e
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
