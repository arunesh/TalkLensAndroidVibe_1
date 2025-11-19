package com.talkbox.docs.talklens.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing document information
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val createdAt: Long,
    val updatedAt: Long
)
