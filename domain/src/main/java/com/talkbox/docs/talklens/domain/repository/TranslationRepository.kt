package com.talkbox.docs.talklens.domain.repository

import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.DownloadProgress
import com.talkbox.docs.talklens.domain.model.TranslatedText
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for translation operations
 */
interface TranslationRepository {
    /**
     * Translate text from source language to target language
     */
    suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<TranslatedText>

    /**
     * Download translation model for a specific language
     */
    fun downloadModel(language: Language): Flow<DownloadProgress>

    /**
     * Check if translation model is downloaded for a language
     */
    suspend fun isModelDownloaded(language: Language): Boolean

    /**
     * Delete translation model for a language
     */
    suspend fun deleteModel(language: Language): Result<Unit>

    /**
     * Get list of downloaded language models
     */
    suspend fun getDownloadedLanguages(): List<Language>
}
