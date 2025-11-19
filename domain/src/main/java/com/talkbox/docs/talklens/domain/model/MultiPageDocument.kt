package com.talkbox.docs.talklens.domain.model

import com.talkbox.docs.talklens.core.model.Language

/**
 * Domain model representing a multi-page document
 */
data class MultiPageDocument(
    val id: String,
    val pages: List<Page>,
    val sourceLanguage: Language? = null,
    val targetLanguage: Language? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get total number of pages
     */
    val pageCount: Int
        get() = pages.size

    /**
     * Check if all pages have been recognized
     */
    val isAllPagesRecognized: Boolean
        get() = pages.all { it.recognizedText != null }

    /**
     * Check if all pages have been translated
     */
    val isAllPagesTranslated: Boolean
        get() = pages.all { it.translatedText != null }

    /**
     * Get combined text from all recognized pages
     */
    fun getCombinedRecognizedText(): String {
        return pages
            .mapNotNull { it.recognizedText?.text }
            .joinToString("\n\n--- Page ${pages.indexOf(it) + 1} ---\n\n")
    }

    /**
     * Get combined translated text from all pages
     */
    fun getCombinedTranslatedText(): String {
        return pages
            .mapNotNull { it.translatedText?.text }
            .joinToString("\n\n--- Page ${pages.indexOf(it) + 1} ---\n\n")
    }
}
