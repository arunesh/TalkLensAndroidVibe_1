package com.talkbox.docs.talklens.domain.usecase

import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.DownloadProgress
import com.talkbox.docs.talklens.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for downloading required ML models during initial setup
 */
class DownloadRequiredModelsUseCase @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    /**
     * Download models for initial setup
     * By default, downloads English and one user-selected language
     */
    operator fun invoke(selectedLanguage: Language): Flow<List<DownloadProgress>> = flow {
        val languagesToDownload = mutableListOf<Language>()

        // Always include English
        if (!translationRepository.isModelDownloaded(Language.ENGLISH)) {
            languagesToDownload.add(Language.ENGLISH)
        }

        // Add user-selected language if different from English
        if (selectedLanguage != Language.ENGLISH &&
            !translationRepository.isModelDownloaded(selectedLanguage)
        ) {
            languagesToDownload.add(selectedLanguage)
        }

        val progressList = mutableListOf<DownloadProgress>()

        // Download each language sequentially
        languagesToDownload.forEach { language ->
            translationRepository.downloadModel(language).collect { progress ->
                // Update progress for this language
                val index = progressList.indexOfFirst { it.language == language }
                if (index >= 0) {
                    progressList[index] = progress
                } else {
                    progressList.add(progress)
                }

                emit(progressList.toList())
            }
        }
    }
}
