package com.talkbox.docs.talklens.domain.repository

import android.graphics.Bitmap
import com.talkbox.docs.talklens.domain.model.RecognizedText

/**
 * Repository interface for text recognition operations
 */
interface TextRecognitionRepository {
    /**
     * Recognize text from an image
     */
    suspend fun recognizeText(image: Bitmap): Result<RecognizedText>

    /**
     * Check if text recognition model is available
     */
    suspend fun isModelAvailable(): Boolean
}
