package com.talkbox.docs.talklens.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "talklens_preferences")

/**
 * Manager for app preferences using DataStore
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        private val DEFAULT_SOURCE_LANGUAGE = stringPreferencesKey("default_source_language")
        private val DEFAULT_TARGET_LANGUAGE = stringPreferencesKey("default_target_language")
    }

    val isSetupCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SETUP_COMPLETED] ?: false
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[SETUP_COMPLETED] = completed
        }
    }

    val defaultSourceLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_SOURCE_LANGUAGE] ?: "es" // Spanish default
    }

    val defaultTargetLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_TARGET_LANGUAGE] ?: "en" // English default
    }

    suspend fun setDefaultSourceLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_SOURCE_LANGUAGE] = languageCode
        }
    }

    suspend fun setDefaultTargetLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_TARGET_LANGUAGE] = languageCode
        }
    }
}
