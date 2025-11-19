package com.talkbox.docs.talklens.domain.model

import com.talkbox.docs.talklens.core.model.Language

/**
 * Domain model for translated text
 */
data class TranslatedText(
    val text: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val timestamp: Long = System.currentTimeMillis()
)
