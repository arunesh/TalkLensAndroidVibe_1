package com.talkbox.docs.talklens.core.model

/**
 * Represents supported languages for translation
 */
enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    CHINESE_SIMPLIFIED("zh", "Chinese (Simplified)"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Russian"),
    ITALIAN("it", "Italian"),
    DUTCH("nl", "Dutch"),
    POLISH("pl", "Polish"),
    TURKISH("tr", "Turkish"),
    VIETNAMESE("vi", "Vietnamese"),
    THAI("th", "Thai"),
    INDONESIAN("id", "Indonesian");

    companion object {
        fun fromCode(code: String): Language? = entries.find { it.code == code }
    }
}
