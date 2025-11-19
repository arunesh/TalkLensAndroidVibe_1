package com.talkbox.docs.talklens.data.cache

import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for storing multi-page documents during the current session
 * This is used to pass documents between screens without serialization
 */
@Singleton
class DocumentCache @Inject constructor() {

    private val _documents = MutableStateFlow<Map<String, MultiPageDocument>>(emptyMap())
    val documents: StateFlow<Map<String, MultiPageDocument>> = _documents.asStateFlow()

    /**
     * Store a document in the cache
     */
    fun putDocument(document: MultiPageDocument) {
        _documents.value = _documents.value + (document.id to document)
    }

    /**
     * Retrieve a document from the cache
     */
    fun getDocument(id: String): MultiPageDocument? {
        return _documents.value[id]
    }

    /**
     * Remove a document from the cache
     */
    fun removeDocument(id: String) {
        _documents.value = _documents.value - id
    }

    /**
     * Clear all documents from the cache
     */
    fun clear() {
        _documents.value = emptyMap()
    }
}
