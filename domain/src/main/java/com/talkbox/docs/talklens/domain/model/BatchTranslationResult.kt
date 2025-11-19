package com.talkbox.docs.talklens.domain.model

/**
 * Result of batch translation operation
 */
sealed interface BatchTranslationResult {
    /**
     * Translation in progress
     */
    data class InProgress(
        val currentPage: Int,
        val totalPages: Int,
        val translatedPages: List<Page>
    ) : BatchTranslationResult {
        val progress: Float
            get() = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
    }

    /**
     * Translation completed successfully
     */
    data class Success(
        val document: MultiPageDocument
    ) : BatchTranslationResult

    /**
     * Translation failed
     */
    data class Error(
        val failedPage: Int,
        val message: String,
        val partiallyTranslatedPages: List<Page>
    ) : BatchTranslationResult
}
