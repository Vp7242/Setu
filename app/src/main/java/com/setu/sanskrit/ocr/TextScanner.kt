package com.setu.sanskrit.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device Devanagari OCR (ML Kit). Good for printed text.
 * For handwritten manuscripts, send the image to the backend instead (see README, "Next steps").
 */
class TextScanner(private val context: Context) {
    private val recognizer =
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    suspend fun recognize(uri: Uri): String = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }
}
