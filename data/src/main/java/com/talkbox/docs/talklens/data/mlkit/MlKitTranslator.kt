package com.talkbox.docs.talklens.data.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.DownloadProgress
import com.talkbox.docs.talklens.domain.model.DownloadState
import com.talkbox.docs.talklens.domain.model.ModelType
import com.talkbox.docs.talklens.domain.model.TranslatedText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit implementation of translation
 */
@Singleton
class MlKitTranslator @Inject constructor() {

    private val modelManager = RemoteModelManager.getInstance()
    private val translators = mutableMapOf<Pair<Language, Language>, com.google.mlkit.nl.translate.Translator>()

    suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<TranslatedText> {
        return try {
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)

            // Ensure model is downloaded
            translator.downloadModelIfNeeded().await()

            // Translate the text
            val translatedText = translator.translate(text).await()

            Result.success(
                TranslatedText(
                    text = translatedText,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun downloadModel(language: Language): Flow<DownloadProgress> = callbackFlow {
        try {
            val mlKitLanguageCode = language.toMlKitLanguageCode()
            val model = TranslateRemoteModel.Builder(mlKitLanguageCode).build()

            trySend(
                DownloadProgress(
                    language = language,
                    modelType = ModelType.TRANSLATION,
                    state = DownloadState.DOWNLOADING,
                    progress = 0f
                )
            )

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            modelManager.download(model, conditions).await()

            trySend(
                DownloadProgress(
                    language = language,
                    modelType = ModelType.TRANSLATION,
                    state = DownloadState.COMPLETED,
                    progress = 1f
                )
            )
        } catch (e: Exception) {
            trySend(
                DownloadProgress(
                    language = language,
                    modelType = ModelType.TRANSLATION,
                    state = DownloadState.FAILED,
                    error = e.message
                )
            )
        }

        awaitClose()
    }

    suspend fun isModelDownloaded(language: Language): Boolean {
        return try {
            val mlKitLanguageCode = language.toMlKitLanguageCode()
            val model = TranslateRemoteModel.Builder(mlKitLanguageCode).build()
            modelManager.isModelDownloaded(model).await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteModel(language: Language): Result<Unit> {
        return try {
            val mlKitLanguageCode = language.toMlKitLanguageCode()
            val model = TranslateRemoteModel.Builder(mlKitLanguageCode).build()
            modelManager.deleteDownloadedModel(model).await()

            // Close and remove translator if exists
            translators.entries.removeIf { (key, translator) ->
                if (key.first == language || key.second == language) {
                    translator.close()
                    true
                } else {
                    false
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDownloadedLanguages(): List<Language> {
        return try {
            val downloadedModels = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
            downloadedModels.mapNotNull { model ->
                Language.entries.find { it.code == model.language }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getOrCreateTranslator(
        sourceLanguage: Language,
        targetLanguage: Language
    ): com.google.mlkit.nl.translate.Translator {
        val key = sourceLanguage to targetLanguage
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage.toMlKitLanguageCode())
                .setTargetLanguage(targetLanguage.toMlKitLanguageCode())
                .build()
            Translation.getClient(options)
        }
    }

    fun closeAll() {
        translators.values.forEach { it.close() }
        translators.clear()
    }

    private fun Language.toMlKitLanguageCode(): String {
        return when (this) {
            Language.ENGLISH -> TranslateLanguage.ENGLISH
            Language.SPANISH -> TranslateLanguage.SPANISH
            Language.FRENCH -> TranslateLanguage.FRENCH
            Language.GERMAN -> TranslateLanguage.GERMAN
            Language.CHINESE_SIMPLIFIED -> TranslateLanguage.CHINESE
            Language.JAPANESE -> TranslateLanguage.JAPANESE
            Language.KOREAN -> TranslateLanguage.KOREAN
            Language.ARABIC -> TranslateLanguage.ARABIC
            Language.HINDI -> TranslateLanguage.HINDI
            Language.PORTUGUESE -> TranslateLanguage.PORTUGUESE
            Language.RUSSIAN -> TranslateLanguage.RUSSIAN
            Language.ITALIAN -> TranslateLanguage.ITALIAN
            Language.DUTCH -> TranslateLanguage.DUTCH
            Language.POLISH -> TranslateLanguage.POLISH
            Language.TURKISH -> TranslateLanguage.TURKISH
            Language.VIETNAMESE -> TranslateLanguage.VIETNAMESE
            Language.THAI -> TranslateLanguage.THAI
            Language.INDONESIAN -> TranslateLanguage.INDONESIAN
        }
    }
}
