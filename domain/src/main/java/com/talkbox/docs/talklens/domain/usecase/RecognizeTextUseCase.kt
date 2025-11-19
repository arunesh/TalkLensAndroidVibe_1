package com.talkbox.docs.talklens.domain.usecase

import android.graphics.Bitmap
import com.talkbox.docs.talklens.domain.model.RecognizedText
import com.talkbox.docs.talklens.domain.repository.TextRecognitionRepository
import javax.inject.Inject

/**
 * Use case for recognizing text from an image
 */
class RecognizeTextUseCase @Inject constructor(
    private val textRecognitionRepository: TextRecognitionRepository
) {
    suspend operator fun invoke(image: Bitmap): Result<RecognizedText> {
        return textRecognitionRepository.recognizeText(image)
    }
}
