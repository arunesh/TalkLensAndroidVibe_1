package com.talkbox.docs.talklens.data.di

import android.content.Context
import androidx.room.Room
import com.talkbox.docs.talklens.data.local.TalkLensDatabase
import com.talkbox.docs.talklens.data.local.dao.DocumentDao
import com.talkbox.docs.talklens.data.local.dao.ModelInfoDao
import com.talkbox.docs.talklens.data.local.dao.PageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTalkLensDatabase(
        @ApplicationContext context: Context
    ): TalkLensDatabase {
        return Room.databaseBuilder(
            context,
            TalkLensDatabase::class.java,
            TalkLensDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDocumentDao(database: TalkLensDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun providePageDao(database: TalkLensDatabase): PageDao {
        return database.pageDao()
    }

    @Provides
    fun provideModelInfoDao(database: TalkLensDatabase): ModelInfoDao {
        return database.modelInfoDao()
    }
}
