package com.talkbox.docs.talklens.domain.model

import com.talkbox.docs.talklens.core.model.Language

/**
 * Represents the download progress of an ML model
 */
data class DownloadProgress(
    val language: Language,
    val modelType: ModelType,
    val state: DownloadState,
    val progress: Float = 0f,
    val error: String? = null
)

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class ModelType {
    TEXT_RECOGNITION,
    TRANSLATION
}
