package com.talkbox.docs.talklens.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.talkbox.docs.talklens.data.local.entity.DocumentEntity
import com.talkbox.docs.talklens.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for document operations
 */
@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocument(documentId: String): DocumentEntity?

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>>

    @Query("DELETE FROM documents WHERE createdAt < :timestamp")
    suspend fun deleteOldDocuments(timestamp: Long)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()
}
