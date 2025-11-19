package com.talkbox.docs.talklens.data.repository

import android.graphics.Bitmap
import com.talkbox.docs.talklens.data.mlkit.MlKitTextRecognizer
import com.talkbox.docs.talklens.domain.model.RecognizedText
import com.talkbox.docs.talklens.domain.repository.TextRecognitionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TextRecognitionRepository using ML Kit
 */
@Singleton
class TextRecognitionRepositoryImpl @Inject constructor(
    private val mlKitTextRecognizer: MlKitTextRecognizer
) : TextRecognitionRepository {

    override suspend fun recognizeText(image: Bitmap): Result<RecognizedText> {
        return mlKitTextRecognizer.recognizeText(image)
    }

    override suspend fun isModelAvailable(): Boolean {
        // ML Kit text recognition is always available (bundled with app)
        return true
    }
}
