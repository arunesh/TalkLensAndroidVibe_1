package com.talkbox.docs.talklens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.talkbox.docs.talklens.data.local.entity.ModelInfoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for model info operations
 */
@Dao
interface ModelInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModelInfo(modelInfo: ModelInfoEntity)

    @Update
    suspend fun updateModelInfo(modelInfo: ModelInfoEntity)

    @Query("SELECT * FROM model_info WHERE languageCode = :languageCode AND modelType = :modelType")
    suspend fun getModelInfo(languageCode: String, modelType: String): ModelInfoEntity?

    @Query("SELECT * FROM model_info")
    fun getAllModelInfo(): Flow<List<ModelInfoEntity>>

    @Query("SELECT * FROM model_info WHERE isDownloaded = 1")
    fun getDownloadedModels(): Flow<List<ModelInfoEntity>>

    @Query("SELECT * FROM model_info WHERE modelType = :modelType AND isDownloaded = 1")
    suspend fun getDownloadedModelsByType(modelType: String): List<ModelInfoEntity>

    @Query("DELETE FROM model_info WHERE languageCode = :languageCode AND modelType = :modelType")
    suspend fun deleteModelInfo(languageCode: String, modelType: String)
}
