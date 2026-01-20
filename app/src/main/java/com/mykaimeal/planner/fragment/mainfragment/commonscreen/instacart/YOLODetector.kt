package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

/*
data class Detection(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val classId: Int,
    val className: String
)

class YOLODetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val INPUT_SIZE = 640
    private val CONFIDENCE_THRESHOLD = 0.5f

    init {
        loadModel()
    }

    private fun loadModel() {
        val modelFile = loadModelFile("best_int8.tflite")
        val options = Interpreter.Options()
            .setNumThreads(4)
            .setUseNNAPI(false)
        interpreter = Interpreter(modelFile, options)
    }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val assetManager = context.assets
        val inputStream = assetManager.open(modelName)
        val modelBytes = inputStream.readBytes()
        val buffer = ByteBuffer.allocateDirect(modelBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(modelBytes)
        buffer.rewind()
        return buffer
    }

    fun detectObjects(bitmap: Bitmap): List<Detection> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputArray = preprocessBitmap(scaledBitmap)
        val outputMap = HashMap<Int, Any>()
//        val output = Array(1) { Array(25200) { FloatArray(6) } }
        val output = Array(1) { Array(8) { FloatArray(8400) } }
        outputMap[0] = output
        interpreter?.runForMultipleInputsOutputs(arrayOf(inputArray), outputMap)
        return parseOutput(output[0], bitmap.width, bitmap.height)
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = ((pixel shr 16 and 0xFF) / 255.0f)
            val g = ((pixel shr 8 and 0xFF) / 255.0f)
            val b = ((pixel and 0xFF) / 255.0f)
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        buffer.rewind()
        return buffer
    }

    private fun parseOutput(output: Array<FloatArray>, originalWidth: Int, originalHeight: Int): List<Detection> {
        val detections = mutableListOf<Detection>()
        for (i in output.indices) {
            val confidence = output[i][4]
            if (confidence > CONFIDENCE_THRESHOLD) {
                val x = output[i][0] * originalWidth / INPUT_SIZE
                val y = output[i][1] * originalHeight / INPUT_SIZE
                val w = output[i][2] * originalWidth / INPUT_SIZE
                val h = output[i][3] * originalHeight / INPUT_SIZE
                val classId = output[i][5].toInt()
                detections.add(Detection(
                    x = x - w/2,
                    y = y - h/2,
                    width = w,
                    height = h,
                    confidence = confidence,
                    classId = classId,
                    className = if (classId == 0) "product_name" else "price"
                ))
            }
        }
        return nonMaxSuppression(detections)
    }

    private fun nonMaxSuppression(detections: List<Detection>, iouThreshold: Float = 0.5f): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<Detection>()
        for (detection in sorted) {
            var shouldAdd = true
            for (s in selected) {
                if (calculateIOU(detection, s) > iouThreshold && detection.classId == s.classId) {
                    shouldAdd = false
                    break
                }
            }
            if (shouldAdd) selected.add(detection)
        }
        return selected
    }

    private fun calculateIOU(a: Detection, b: Detection): Float {
        val x1 = maxOf(a.x, b.x)
        val y1 = maxOf(a.y, b.y)
        val x2 = minOf(a.x + a.width, b.x + b.width)
        val y2 = minOf(a.y + a.height, b.y + b.height)
        val intersection = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val union = a.width * a.height + b.width * b.height - intersection
        return if (union == 0f) 0f else intersection / union
    }
}*/


data class Detection(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val classId: Int,
    val className: String
)

class YOLODetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val INPUT_SIZE = 640
    private val CONFIDENCE_THRESHOLD = 0.5f

    init {
        loadModel()
    }

    private fun loadModel() {
        val modelFile = loadModelFile("best_int8.tflite")
        val options = Interpreter.Options()
            .setNumThreads(4)
            .setUseNNAPI(false)
        interpreter = Interpreter(modelFile, options)
    }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val assetManager = context.assets
        val inputStream = assetManager.open(modelName)
        val modelBytes = inputStream.readBytes()
        val buffer = ByteBuffer.allocateDirect(modelBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(modelBytes)
        buffer.rewind()
        return buffer
    }

    fun detectObjects(bitmap: Bitmap): List<Detection> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputArray = preprocessBitmap(scaledBitmap)

        // Dynamically get output shape
        val outputTensor = interpreter!!.getOutputTensor(0)
        val shape = outputTensor.shape() // e.g., [1, 8, 8400]
        val output = Array(shape[0]) { Array(shape[1]) { FloatArray(shape[2]) } }
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = output

        Log.d("YOLOModelCheck", "Running inference with output shape: ${shape.joinToString()}")
        interpreter?.runForMultipleInputsOutputs(arrayOf(inputArray), outputMap)

        return parseOutput(output, bitmap.width, bitmap.height)
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = ((pixel shr 16 and 0xFF) / 255.0f)
            val g = ((pixel shr 8 and 0xFF) / 255.0f)
            val b = ((pixel and 0xFF) / 255.0f)
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        buffer.rewind()
        return buffer
    }

    private fun parseOutput(output: Array<Array<FloatArray>>, originalWidth: Int, originalHeight: Int): List<Detection> {
        val detections = mutableListOf<Detection>()
        val batch = output[0]
        val numBoxes = batch[0].size
        val numChannels = batch.size // e.g., 8

        Log.d("YOLOModelCheck","batch"+batch)
        Log.d("YOLOModelCheck","numBoxes"+numBoxes)
        Log.d("YOLOModelCheck","numChannels"+numChannels)

        for (i in 0 until numBoxes) {
            val confidence = batch[4][i] // 5th channel
            if (confidence > CONFIDENCE_THRESHOLD) {
                val x = batch[0][i] * originalWidth / INPUT_SIZE
                val y = batch[1][i] * originalHeight / INPUT_SIZE
                val w = batch[2][i] * originalWidth / INPUT_SIZE
                val h = batch[3][i] * originalHeight / INPUT_SIZE

                // classId = channel with max probability (from remaining channels)
                val classProbabilities = FloatArray(numChannels - 5) { c -> batch[5 + c][i] }
                val classId = classProbabilities.indices.maxByOrNull { classProbabilities[it] } ?: 0

                detections.add(
                    Detection(
                        x = x - w / 2,
                        y = y - h / 2,
                        width = w,
                        height = h,
                        confidence = confidence,
                        classId = classId,
                        className = if (classId == 0) "product_name" else "price"
                    )
                )
            }
        }

        return nonMaxSuppression(detections)
    }

    private fun nonMaxSuppression(detections: List<Detection>, iouThreshold: Float = 0.5f): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<Detection>()
        for (detection in sorted) {
            var shouldAdd = true
            for (s in selected) {
                if (calculateIOU(detection, s) > iouThreshold && detection.classId == s.classId) {
                    shouldAdd = false
                    break
                }
            }
            if (shouldAdd) selected.add(detection)
        }
        return selected
    }

    private fun calculateIOU(a: Detection, b: Detection): Float {
        val x1 = max(a.x, b.x)
        val y1 = max(a.y, b.y)
        val x2 = min(a.x + a.width, b.x + b.width)
        val y2 = min(a.y + a.height, b.y + b.height)
        val intersection = max(0f, x2 - x1) * max(0f, y2 - y1)
        val union = a.width * a.height + b.width * b.height - intersection
        return if (union == 0f) 0f else intersection / union
    }
}