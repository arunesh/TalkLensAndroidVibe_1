package com.talkbox.docs.talklens.domain.usecase

import com.talkbox.docs.talklens.domain.model.MultiPageDocument
import com.talkbox.docs.talklens.domain.model.Page
import javax.inject.Inject

/**
 * Use case for managing pages in a multi-page document
 */
class ManagePagesUseCase @Inject constructor() {

    /**
     * Add a new page to the document
     */
    fun addPage(
        document: MultiPageDocument,
        page: Page
    ): MultiPageDocument {
        val updatedPages = document.pages.toMutableList().apply {
            // Update page number based on position
            add(page.copy(pageNumber = size + 1))
        }
        return document.copy(
            pages = updatedPages,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Delete a page from the document
     */
    fun deletePage(
        document: MultiPageDocument,
        pageId: String
    ): Result<MultiPageDocument> {
        val pageIndex = document.pages.indexOfFirst { it.id == pageId }
        if (pageIndex == -1) {
            return Result.failure(IllegalArgumentException("Page not found"))
        }

        val updatedPages = document.pages.toMutableList().apply {
            removeAt(pageIndex)
        }.mapIndexed { index, page ->
            // Renumber pages after deletion
            page.copy(pageNumber = index + 1)
        }

        return Result.success(
            document.copy(
                pages = updatedPages,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Reorder pages in the document
     */
    fun reorderPages(
        document: MultiPageDocument,
        fromIndex: Int,
        toIndex: Int
    ): Result<MultiPageDocument> {
        if (fromIndex !in document.pages.indices || toIndex !in document.pages.indices) {
            return Result.failure(IllegalArgumentException("Invalid page indices"))
        }

        val updatedPages = document.pages.toMutableList().apply {
            val page = removeAt(fromIndex)
            add(toIndex, page)
        }.mapIndexed { index, page ->
            // Renumber all pages
            page.copy(pageNumber = index + 1)
        }

        return Result.success(
            document.copy(
                pages = updatedPages,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Update a page with new data (e.g., after translation)
     */
    fun updatePage(
        document: MultiPageDocument,
        updatedPage: Page
    ): Result<MultiPageDocument> {
        val pageIndex = document.pages.indexOfFirst { it.id == updatedPage.id }
        if (pageIndex == -1) {
            return Result.failure(IllegalArgumentException("Page not found"))
        }

        val updatedPages = document.pages.toMutableList().apply {
            set(pageIndex, updatedPage)
        }

        return Result.success(
            document.copy(
                pages = updatedPages,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
