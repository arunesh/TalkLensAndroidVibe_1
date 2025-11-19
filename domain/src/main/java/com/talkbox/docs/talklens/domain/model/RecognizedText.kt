package com.talkbox.docs.talklens.domain.model

import android.graphics.Rect

/**
 * Domain model for recognized text from an image
 */
data class RecognizedText(
    val text: String,
    val blocks: List<TextBlock>,
    val confidence: Float
)

data class TextBlock(
    val text: String,
    val boundingBox: Rect?,
    val confidence: Float
)
