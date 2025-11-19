package com.talkbox.docs.talklens.data.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.talkbox.docs.talklens.domain.model.RecognizedText
import com.talkbox.docs.talklens.domain.model.TextBlock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit implementation of text recognition
 */
@Singleton
class MlKitTextRecognizer @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): Result<RecognizedText> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(image).await()

            val blocks = visionText.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox,
                    confidence = block.confidence ?: 0f
                )
            }

            val recognizedText = RecognizedText(
                text = visionText.text,
                blocks = blocks,
                confidence = blocks.map { it.confidence }.average().toFloat()
            )

            Result.success(recognizedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        recognizer.close()
    }
}
