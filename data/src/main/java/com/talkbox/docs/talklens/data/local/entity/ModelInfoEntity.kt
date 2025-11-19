package com.talkbox.docs.talklens.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for tracking downloaded ML models
 */
@Entity(tableName = "model_info")
data class ModelInfoEntity(
    @PrimaryKey
    val languageCode: String,
    val modelType: String, // TEXT_RECOGNITION or TRANSLATION
    val isDownloaded: Boolean,
    val size: Long,
    val version: String,
    val downloadedAt: Long?
)
