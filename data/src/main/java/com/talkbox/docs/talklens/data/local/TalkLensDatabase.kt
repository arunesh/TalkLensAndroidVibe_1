package com.talkbox.docs.talklens.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.talkbox.docs.talklens.data.local.dao.DocumentDao
import com.talkbox.docs.talklens.data.local.dao.ModelInfoDao
import com.talkbox.docs.talklens.data.local.dao.PageDao
import com.talkbox.docs.talklens.data.local.entity.DocumentEntity
import com.talkbox.docs.talklens.data.local.entity.ModelInfoEntity
import com.talkbox.docs.talklens.data.local.entity.PageEntity

/**
 * Room database for TalkLens app
 */
@Database(
    entities = [
        DocumentEntity::class,
        PageEntity::class,
        ModelInfoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TalkLensDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun pageDao(): PageDao
    abstract fun modelInfoDao(): ModelInfoDao

    companion object {
        const val DATABASE_NAME = "talklens_database"
    }
}
