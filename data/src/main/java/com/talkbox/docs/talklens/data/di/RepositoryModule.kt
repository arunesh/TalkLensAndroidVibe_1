package com.talkbox.docs.talklens.data.di

import com.talkbox.docs.talklens.data.repository.TextRecognitionRepositoryImpl
import com.talkbox.docs.talklens.data.repository.TranslationRepositoryImpl
import com.talkbox.docs.talklens.domain.repository.TextRecognitionRepository
import com.talkbox.docs.talklens.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTextRecognitionRepository(
        impl: TextRecognitionRepositoryImpl
    ): TextRecognitionRepository

    @Binds
    @Singleton
    abstract fun bindTranslationRepository(
        impl: TranslationRepositoryImpl
    ): TranslationRepository
}
