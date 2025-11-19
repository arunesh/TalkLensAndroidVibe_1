package com.talkbox.docs.talklens.domain.model

/**
 * Represents a file type that can be imported
 */
enum class ImportFileType(val mimeType: String, val extensions: List<String>) {
    IMAGE_JPEG("image/jpeg", listOf("jpg", "jpeg")),
    IMAGE_PNG("image/png", listOf("png")),
    PDF("application/pdf", listOf("pdf"));

    companion object {
        fun fromMimeType(mimeType: String?): ImportFileType? {
            return entries.find { it.mimeType == mimeType }
        }

        fun fromExtension(extension: String): ImportFileType? {
            return entries.find { extension.lowercase() in it.extensions }
        }

        val allMimeTypes: List<String>
            get() = entries.map { it.mimeType }
    }
}
