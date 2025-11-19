package com.talkbox.docs.talklens.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.talkbox.docs.talklens.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for page operations
 */
@Dao
interface PageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Update
    suspend fun updatePage(page: PageEntity)

    @Delete
    suspend fun deletePage(page: PageEntity)

    @Query("SELECT * FROM pages WHERE id = :pageId")
    suspend fun getPage(pageId: String): PageEntity?

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun getPages(documentId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    suspend fun getPagesSync(documentId: String): List<PageEntity>

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesByDocument(documentId: String)
}
