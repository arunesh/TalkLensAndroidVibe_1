package com.talkbox.docs.talklens.feature.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

/**
 * CameraX Preview Composable
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean = false,
    onImageCaptured: (Bitmap) -> Unit = {},
    onError: (String) -> Unit = {},
    imageCaptureUseCase: ImageCaptureHolder
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(flashEnabled) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(
                    if (flashEnabled) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            imageCaptureUseCase.imageCapture = imageCapture

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                onError("Failed to start camera: ${e.message}")
            }
        }, executor)

        onDispose {
            cameraProviderFuture.get().unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Holder class for ImageCapture use case
 */
class ImageCaptureHolder {
    var imageCapture: ImageCapture? = null
}

/**
 * Capture image from ImageCapture use case
 */
fun captureImage(
    imageCapture: ImageCapture?,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    if (imageCapture == null) {
        onError("Camera not ready")
        return
    }

    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bitmap = imageProxyToBitmap(image)
                    val processedBitmap = preprocessBitmap(bitmap)
                    image.close()
                    onImageCaptured(processedBitmap)
                } catch (e: Exception) {
                    image.close()
                    onError("Failed to process image: ${e.message}")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError("Capture failed: ${exception.message}")
            }
        }
    )
}

/**
 * Convert ImageProxy to Bitmap
 */
private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // Rotate if needed
    return if (image.imageInfo.rotationDegrees != 0) {
        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

/**
 * Preprocess bitmap for better OCR
 */
private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
    val maxDimension = 2048
    return if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        Bitmap.createScaledBitmap(bitmap, width, height, true)
    } else {
        bitmap
    }
}
