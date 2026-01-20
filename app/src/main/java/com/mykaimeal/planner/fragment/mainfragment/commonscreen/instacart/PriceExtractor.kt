package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

class PriceExtractor(private val context: Context) {

    private val yoloDetector = YOLODetector(context) // <-- your custom YOLO wrapper

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Main function to extract prices from a screenshot bitmap
    suspend fun extractPricesFromScreenshot(bitmap: Bitmap): List<ShoppingItem> {
        // Step 1: Detect regions with YOLO
        val detections = yoloDetector.detectObjects(bitmap)
        Log.d("YOLOModelCheck","detections"+detections)
        // Step 2: Group detections into rows (items on same line)
        val rows = groupDetectionsByRow(detections)
        Log.d("YOLOModelCheck","rows"+rows)
        // Step 3: OCR for each detection
        val items = mutableListOf<ShoppingItem>()
        for (row in rows) {
            var productName = ""
            var price = ""
            for (detection in row) {
                val croppedBitmap = cropBitmap(bitmap, detection)
                val text = extractTextFromBitmap(croppedBitmap)

                when (detection.className) {
                    "product_name" -> productName = text
                    "price" -> price = text
                }
            }
            if (productName.isNotEmpty() && price.isNotEmpty()) {
                items.add(ShoppingItem(productName, price))
            }
        }
        return items
    }

    // Crop region from the screenshot
    private fun cropBitmap(source: Bitmap, detection: Detection): Bitmap {
        val x = detection.x.toInt().coerceAtLeast(0)
        val y = detection.y.toInt().coerceAtLeast(0)
        val width = detection.width.toInt().coerceAtMost(source.width - x)
        val height = detection.height.toInt().coerceAtMost(source.height - y)
        return Bitmap.createBitmap(source, x, y, width, height)
    }

    // OCR with coroutine wrapper
    private suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { cont ->
            textRecognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text.trim()) {} }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    // Group detections into rows
    private fun groupDetectionsByRow(
        detections: List<Detection>,
        threshold: Float = 30f
    ): List<List<Detection>> {
        val sorted = detections.sortedBy { it.y }
        val rows = mutableListOf<MutableList<Detection>>()
        for (detection in sorted) {
            var addedToRow = false
            for (row in rows) {
                val avgY = row.sumOf { it.y.toDouble() } / row.size
                if (abs(detection.y - avgY) < threshold) {
                    row.add(detection)
                    addedToRow = true
                    break
                }
            }
            if (!addedToRow) {
                rows.add(mutableListOf(detection))
            }
        }
        // Sort each row by X position
        return rows.map { row -> row.sortedBy { it.x } }
    }
}
