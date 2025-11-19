package com.talkbox.docs.talklens.domain.usecase

import com.talkbox.docs.talklens.core.model.Language
import com.talkbox.docs.talklens.domain.model.TranslatedText
import com.talkbox.docs.talklens.domain.repository.TranslationRepository
import javax.inject.Inject

/**
 * Use case for translating text from one language to another
 */
class TranslateTextUseCase @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    suspend operator fun invoke(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<TranslatedText> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Text cannot be empty"))
        }

        if (sourceLanguage == targetLanguage) {
            return Result.failure(IllegalArgumentException("Source and target languages must be different"))
        }

        return translationRepository.translate(text, sourceLanguage, targetLanguage)
    }
}
