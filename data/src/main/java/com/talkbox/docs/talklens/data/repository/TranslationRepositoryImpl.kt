package com.talkbox.docs.talklens.data.repository

import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.data.local.dao.ModelInfoDao
import com.talkbox.docs.talklens.data.local.entity.ModelInfoEntity
import com.talkbox.docs.talklens.data.mlkit.MlKitTranslator
import com.talkbox.docs.talklens.domain.model.DownloadProgress
import com.talkbox.docs.talklens.domain.model.DownloadState
import com.talkbox.docs.talklens.domain.model.ModelType
import com.talkbox.docs.talklens.domain.model.TranslatedText
import com.talkbox.docs.talklens.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TranslationRepository using ML Kit
 */
@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val mlKitTranslator: MlKitTranslator,
    private val modelInfoDao: ModelInfoDao
) : TranslationRepository {

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<TranslatedText> {
        return mlKitTranslator.translate(text, sourceLanguage, targetLanguage)
    }

    override fun downloadModel(language: Language): Flow<DownloadProgress> {
        return mlKitTranslator.downloadModel(language)
            .onEach { progress ->
                // Update database when download completes
                if (progress.state == DownloadState.COMPLETED) {
                    modelInfoDao.insertModelInfo(
                        ModelInfoEntity(
                            languageCode = language.code,
                            modelType = ModelType.TRANSLATION.name,
                            isDownloaded = true,
                            size = 0L, // ML Kit doesn't provide size info
                            version = "1.0",
                            downloadedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
    }

    override suspend fun isModelDownloaded(language: Language): Boolean {
        return mlKitTranslator.isModelDownloaded(language)
    }

    override suspend fun deleteModel(language: Language): Result<Unit> {
        val result = mlKitTranslator.deleteModel(language)
        if (result.isSuccess) {
            modelInfoDao.deleteModelInfo(language.code, ModelType.TRANSLATION.name)
        }
        return result
    }

    override suspend fun getDownloadedLanguages(): List<Language> {
        return mlKitTranslator.getDownloadedLanguages()
    }
}
