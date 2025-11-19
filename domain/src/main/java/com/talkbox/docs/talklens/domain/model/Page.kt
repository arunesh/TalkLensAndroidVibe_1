package com.talkbox.docs.talklens.domain.model

import android.graphics.Bitmap

/**
 * Domain model representing a single page in a multi-page document
 */
data class Page(
    val id: String,
    val pageNumber: Int,
    val image: Bitmap,
    val recognizedText: RecognizedText?,
    val translatedText: TranslatedText? = null,
    val capturedAt: Long = System.currentTimeMillis()
)
