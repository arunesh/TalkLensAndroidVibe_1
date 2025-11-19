package com.talkbox.docs.talklens.domain.usecase

import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.BatchTranslationResult
import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.model.Page
import com.talkbox.docs.talklens.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for translating all pages in a multi-page document
 */
class BatchTranslateDocumentUseCase @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    operator fun invoke(
        document: MultiPageDocument,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Flow<BatchTranslationResult> = flow {
        if (sourceLanguage == targetLanguage) {
            emit(
                BatchTranslationResult.Error(
                    failedPage = 0,
                    message = "Source and target languages must be different",
                    partiallyTranslatedPages = emptyList()
                )
            )
            return@flow
        }

        val translatedPages = mutableListOf<Page>()

        document.pages.forEachIndexed { index, page ->
            // Emit progress
            emit(
                BatchTranslationResult.InProgress(
                    currentPage = index + 1,
                    totalPages = document.pages.size,
                    translatedPages = translatedPages.toList()
                )
            )

            // Get recognized text from page
            val recognizedText = page.recognizedText
            if (recognizedText == null) {
                emit(
                    BatchTranslationResult.Error(
                        failedPage = index + 1,
                        message = "Page ${index + 1} has not been recognized yet",
                        partiallyTranslatedPages = translatedPages.toList()
                    )
                )
                return@flow
            }

            // Translate the page
            val translationResult = translationRepository.translate(
                text = recognizedText.text,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage
            )

            translationResult.fold(
                onSuccess = { translatedText ->
                    // Add translated page to list
                    translatedPages.add(
                        page.copy(translatedText = translatedText)
                    )
                },
                onFailure = { error ->
                    emit(
                        BatchTranslationResult.Error(
                            failedPage = index + 1,
                            message = error.message ?: "Translation failed for page ${index + 1}",
                            partiallyTranslatedPages = translatedPages.toList()
                        )
                    )
                    return@flow
                }
            )
        }

        // All pages translated successfully
        emit(
            BatchTranslationResult.Success(
                document = document.copy(
                    pages = translatedPages,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
    }
}
