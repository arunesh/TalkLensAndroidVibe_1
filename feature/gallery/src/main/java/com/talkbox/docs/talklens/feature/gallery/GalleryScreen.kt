package com.talkbox.docs.talklens.feature.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkbox.docs.talklens.core.designsystem.component.EmptyState
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensCard
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensLoadingIndicator
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensPrimaryButton
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensSecondaryButton
import com.talkbox.docs.talklens.domain.model.ImportFileType
import com.talkbox.docs.talklens.domain.model.ImportedFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gallery screen for importing files
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateToTranslation: (String) -> Unit = {},
    onNavigateToMultiPageTranslation: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentImports by viewModel.recentImports.collectAsStateWithLifecycle()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Files") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GalleryUiState.Idle -> {
                    IdleContent(
                        recentImports = recentImports,
                        onImportFile = { filePicker.launch("*/*") },
                        onDeleteImport = viewModel::deleteImport,
                        onImportClick = { importedFile ->
                            // Re-import the file
                            viewModel.importFile(importedFile.uri)
                        }
                    )
                }

                is GalleryUiState.Importing -> {
                    ImportingContent(
                        progress = state.progress,
                        currentPage = state.currentPage,
                        totalPages = state.totalPages,
                        fileName = state.fileName
                    )
                }

                is GalleryUiState.ImportSuccess -> {
                    ImportSuccessContent(
                        document = state.document,
                        fileName = state.fileName,
                        onTranslate = {
                            val documentId = state.document.id
                            if (state.document.pageCount == 1) {
                                // Single page - go to single translation
                                val text = state.document.pages.firstOrNull()?.recognizedText?.text ?: ""
                                onNavigateToTranslation(text)
                            } else {
                                // Multi-page - go to multi-page translation
                                onNavigateToMultiPageTranslation(documentId)
                            }
                        },
                        onImportAnother = {
                            viewModel.resetToIdle()
                            filePicker.launch("*/*")
                        }
                    )
                }

                is GalleryUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.resetToIdle() }
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(
    recentImports: List<ImportedFile>,
    onImportFile: () -> Unit,
    onDeleteImport: (ImportedFile) -> Unit,
    onImportClick: (ImportedFile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Import button
        TalkLensPrimaryButton(
            onClick = onImportFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.FileOpen,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from Files")
        }

        // Recent imports
        if (recentImports.isEmpty()) {
            EmptyState(
                message = "No recent imports\n\nImport images or PDFs to get started",
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = "Recent Imports",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = recentImports,
                    key = { "${it.uri}-${it.importedAt}" }
                ) { importedFile ->
                    RecentImportCard(
                        importedFile = importedFile,
                        onDelete = { onDeleteImport(importedFile) },
                        onClick = { onImportClick(importedFile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportingContent(
    progress: Float,
    currentPage: Int,
    totalPages: Int,
    fileName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TalkLensLoadingIndicator(
            message = "Importing file..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (totalPages > 1) "Page $currentPage of $totalPages" else "Processing...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImportSuccessContent(
    document: com.talkbox.docs.talklens.domain.model.MultiPageDocument,
    fileName: String,
    onTranslate: () -> Unit,
    onImportAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TalkLensCard {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Import Complete",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${document.pageCount} ${if (document.pageCount == 1) "page" else "pages"} • ${document.pages.count { it.recognizedText != null }} recognized",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Preview
        if (document.pages.isNotEmpty()) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = document.pages,
                    key = { it.id }
                ) { page ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                bitmap = page.image.asImageBitmap(),
                                contentDescription = "Page ${page.pageNumber}",
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Page ${page.pageNumber}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = page.recognizedText?.text?.take(100) ?: "No text recognized",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TalkLensSecondaryButton(
                onClick = onImportAnother,
                modifier = Modifier.weight(1f)
            ) {
                Text("Import Another")
            }

            TalkLensPrimaryButton(
                onClick = onTranslate,
                modifier = Modifier.weight(1f),
                enabled = document.isAllPagesRecognized
            ) {
                Text("Translate")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Import Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        TalkLensPrimaryButton(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentImportCard(
    importedFile: ImportedFile,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File type icon
            Icon(
                imageVector = when (importedFile.fileType) {
                    ImportFileType.PDF -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.Image
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = importedFile.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatFileSize(importedFile.fileSize)} • ${formatDate(importedFile.importedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
