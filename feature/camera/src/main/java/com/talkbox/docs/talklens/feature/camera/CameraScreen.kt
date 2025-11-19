package com.talkbox.docs.talklens.feature.camera

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensCard
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensLoadingIndicator
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensPrimaryButton
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensSecondaryButton

/**
 * Camera screen for capturing and recognizing text
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateToTranslation: (String) -> Unit,
    onNavigateToMultiPageTranslation: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val flashEnabled by viewModel.flashEnabled.collectAsStateWithLifecycle()
    val multiPageMode by viewModel.multiPageMode.collectAsStateWithLifecycle()
    val currentDocument by viewModel.currentDocument.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (multiPageMode) "Multi-Page Scan"
                        else "Scan Document"
                    )
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    PermissionDeniedContent(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }

                uiState is CameraUiState.MultiPageCapture -> {
                    val multiPageState = uiState as CameraUiState.MultiPageCapture
                    MultiPageCaptureContent(
                        document = multiPageState.document,
                        flashEnabled = flashEnabled,
                        onToggleFlash = viewModel::toggleFlash,
                        onImageCaptured = viewModel::onImageCaptured,
                        onDeletePage = viewModel::deletePage,
                        onContinueCapturing = viewModel::continueCapturing,
                        onFinishCapture = {
                            val documentId = multiPageState.document.id
                            onNavigateToMultiPageTranslation(documentId)
                        }
                    )
                }

                uiState is CameraUiState.Success -> {
                    val successState = uiState as CameraUiState.Success
                    RecognitionResultScreen(
                        capturedImage = successState.capturedImage.asImageBitmap(),
                        recognizedText = successState.recognizedText.text,
                        confidence = successState.recognizedText.confidence,
                        onRetake = viewModel::retryCapture,
                        onUseText = {
                            onNavigateToTranslation(successState.recognizedText.text)
                        },
                        onEnableMultiPage = viewModel::enableMultiPageMode
                    )
                }

                uiState is CameraUiState.Error -> {
                    val errorState = uiState as CameraUiState.Error
                    ErrorContent(
                        message = errorState.message,
                        onRetry = viewModel::retryCapture
                    )
                }

                uiState is CameraUiState.Processing -> {
                    ProcessingContent()
                }

                else -> {
                    CameraContent(
                        flashEnabled = flashEnabled,
                        onToggleFlash = viewModel::toggleFlash,
                        onImageCaptured = viewModel::onImageCaptured,
                        onEnableMultiPage = viewModel::enableMultiPageMode
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraContent(
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    onImageCaptured: (android.graphics.Bitmap) -> Unit,
    onEnableMultiPage: () -> Unit
) {
    val context = LocalContext.current
    val imageCaptureHolder = remember { ImageCaptureHolder() }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashEnabled = flashEnabled,
            onImageCaptured = onImageCaptured,
            onError = { /* Handle error */ },
            imageCaptureUseCase = imageCaptureHolder
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            // Multi-page mode button
            IconButton(
                onClick = onEnableMultiPage,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Pages,
                    contentDescription = "Multi-page mode"
                )
            }

            // Flash toggle
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashEnabled) "Flash On" else "Flash Off"
                )
            }
        }

        // Capture button
        FloatingActionButton(
            onClick = {
                captureImage(
                    imageCapture = imageCaptureHolder.imageCapture,
                    executor = executor,
                    onImageCaptured = onImageCaptured,
                    onError = { /* Handle error */ }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture",
                modifier = Modifier.size(32.dp)
            )
        }

        // Hint text
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Position document within frame and tap capture",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecognitionResultScreen(
    capturedImage: androidx.compose.ui.graphics.ImageBitmap,
    recognizedText: String,
    confidence: Float,
    onRetake: () -> Unit,
    onUseText: () -> Unit,
    onEnableMultiPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Text Recognition Complete",
            style = MaterialTheme.typography.titleLarge
        )

        // Captured image preview
        Card {
            Image(
                bitmap = capturedImage,
                contentDescription = "Captured document",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // Confidence indicator
        TalkLensCard {
            Text(
                text = "Confidence: ${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    confidence > 0.8f -> MaterialTheme.colorScheme.primary
                    confidence > 0.5f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }

        // Recognized text
        TalkLensCard {
            Column {
                Text(
                    text = "Recognized Text",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recognizedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Multi-page option
        TalkLensSecondaryButton(
            onClick = onEnableMultiPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add More Pages")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TalkLensSecondaryButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retake")
            }

            TalkLensPrimaryButton(
                onClick = onUseText,
                modifier = Modifier.weight(1f)
            ) {
                Text("Translate")
            }
        }
    }
}

@Composable
private fun ProcessingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        TalkLensLoadingIndicator(
            message = "Recognizing text..."
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
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

@Composable
private fun MultiPageCaptureContent(
    document: com.talkbox.docs.talklens.domain.model.MultiPageDocument,
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    onImageCaptured: (android.graphics.Bitmap) -> Unit,
    onDeletePage: (String) -> Unit,
    onContinueCapturing: () -> Unit,
    onFinishCapture: () -> Unit
) {
    val context = LocalContext.current
    val imageCaptureHolder = remember { ImageCaptureHolder() }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashEnabled = flashEnabled,
            onImageCaptured = onImageCaptured,
            onError = { /* Handle error */ },
            imageCaptureUseCase = imageCaptureHolder
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashEnabled) "Flash On" else "Flash Off"
                )
            }
        }

        // Bottom section with thumbnails and controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thumbnail strip
            if (document.pages.isNotEmpty()) {
                PageThumbnailStrip(
                    pages = document.pages,
                    onPageClick = { /* Could show preview */ },
                    onDeletePage = onDeletePage
                )
            }

            // Page counter and controls
            PageCounterControls(
                pageCount = document.pageCount,
                onAddMorePages = onContinueCapturing,
                onFinishCapture = onFinishCapture
            )
        }

        // Capture button (when idle)
        FloatingActionButton(
            onClick = {
                captureImage(
                    imageCapture = imageCaptureHolder.imageCapture,
                    executor = executor,
                    onImageCaptured = onImageCaptured,
                    onError = { /* Handle error */ }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (document.pages.isEmpty()) 32.dp else 240.dp)
                .size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TalkLens needs camera access to scan documents",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        TalkLensPrimaryButton(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}
