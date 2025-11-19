package com.talkbox.docs.talklens.feature.camera.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility functions for CameraX operations
 */
object CameraUtils {

    /**
     * Get ProcessCameraProvider instance
     */
    suspend fun getCameraProvider(context: Context): ProcessCameraProvider {
        return suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    continuation.resume(cameraProviderFuture.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * Capture image and convert to Bitmap
     */
    suspend fun captureImage(imageCapture: ImageCapture): Bitmap {
        return suspendCancellableCoroutine { continuation ->
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(imageCapture.targetRotation.let {
                    imageCapture as? Context
                } ?: throw IllegalStateException("Cannot get context")),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = imageProxyToBitmap(image)
                            image.close()
                            continuation.resume(bitmap)
                        } catch (e: Exception) {
                            image.close()
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    /**
     * Convert ImageProxy to Bitmap
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Rotate bitmap if needed based on image rotation
        return when (image.imageInfo.rotationDegrees) {
            0 -> bitmap
            else -> {
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
    }

    /**
     * Get default back camera selector
     */
    fun getBackCameraSelector(): CameraSelector {
        return CameraSelector.DEFAULT_BACK_CAMERA
    }

    /**
     * Preprocess bitmap for better OCR results
     */
    fun preprocessImage(bitmap: Bitmap): Bitmap {
        // For now, just ensure the image isn't too large
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
}
