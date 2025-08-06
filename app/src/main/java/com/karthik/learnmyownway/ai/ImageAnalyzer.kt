package com.karthik.learnmyownway.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.abs

class ImageAnalyzer(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageAnalyzer"
    }
    
    suspend fun analyzeImage(imageUri: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting honest image analysis for URI: $imageUri")
            
            // Load bitmap from URI
            val bitmap = loadBitmapFromUri(imageUri) ?: return@withContext Result.failure(
                Exception("Failed to load image from URI")
            )
            
            Log.d(TAG, "Successfully loaded bitmap: ${bitmap.width}x${bitmap.height}")
            
            // Create an honest description of what we can actually detect
            val description = createHonestDescription(bitmap)
            
            Log.d(TAG, "Generated honest description: $description")
            Result.success(description)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            Result.failure(e)
        }
    }
    
    private fun loadBitmapFromUri(imageUri: String): Bitmap? {
        return try {
            val uri = imageUri.toUri()
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URI", e)
            null
        }
    }
    
    private suspend fun createHonestDescription(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        
        // Sample pixels to understand the image characteristics
        val sampleResults = sampleImageCharacteristics(bitmap)
        
        val description = buildString {
            append("I can see a user-uploaded image ")
            append("(${width}×${height} pixels) ")
            
            // Add what we can actually determine
            when {
                sampleResults.isTextLike -> {
                    append("that appears to contain text or documents based on pixel patterns. ")
                    append("This could be educational material like textbooks, articles, or written content. ")
                }
                sampleResults.hasHighContrast -> {
                    append("with high contrast areas suggesting it might contain diagrams, charts, or structured visual content. ")
                    append("This could be educational diagrams, graphs, or technical illustrations. ")
                }
                sampleResults.hasUniformAreas -> {
                    append("with large uniform color areas suggesting it might be a simple diagram, presentation slide, or minimalist design. ")
                }
                else -> {
                    append("with varied colors and patterns. ")
                }
            }
            
            // Add color information
            append("The image has predominantly ${sampleResults.dominantColorDescription} colors. ")
            
            // Add brightness information
            when {
                sampleResults.averageBrightness > 0.7f -> append("It appears to be a bright image, possibly a document with light background. ")
                sampleResults.averageBrightness < 0.3f -> append("It appears to be a darker image. ")
                else -> append("It has moderate brightness levels. ")
            }
            
            // Be honest about our limitations
            append("Note: I can only analyze basic visual properties like colors, brightness, and patterns. ")
            append("I cannot identify specific objects, read text content, or recognize faces/people in images. ")
            append("This analysis is based solely on pixel-level characteristics.")
        }
        
        return@withContext description
    }
    
    private data class SampleResults(
        val isTextLike: Boolean,
        val hasHighContrast: Boolean,
        val hasUniformAreas: Boolean,
        val dominantColorDescription: String,
        val averageBrightness: Float
    )
    
    private fun sampleImageCharacteristics(bitmap: Bitmap): SampleResults {
        val width = bitmap.width
        val height = bitmap.height
        
        // Sample points across the image
        val samplePoints = 400 // Sample 400 points for analysis
        val step = maxOf(1, (width * height) / samplePoints)
        
        val brightnesses = mutableListOf<Float>()
        val colors = mutableListOf<Int>()
        
        var sampleCount = 0
        for (y in 0 until height step maxOf(1, height / 20)) {
            for (x in 0 until width step maxOf(1, width / 20)) {
                try {
                    val pixel = bitmap.getPixel(x, y)
                    colors.add(pixel)
                    brightnesses.add(Color.luminance(pixel))
                    sampleCount++
                    if (sampleCount >= samplePoints) break
                } catch (e: Exception) {
                    // Skip invalid pixels
                }
            }
            if (sampleCount >= samplePoints) break
        }
        
        if (brightnesses.isEmpty()) {
            return SampleResults(false, false, false, "mixed", 0.5f)
        }
        
        val avgBrightness = brightnesses.average().toFloat()
        
        // Check for text-like patterns (frequent brightness changes)
        val brightnessChanges = brightnesses.zipWithNext { a, b -> abs(a - b) }
        val avgChange = brightnessChanges.average()
        val isTextLike = avgChange > 0.3 && brightnesses.size > 50
        
        // Check for high contrast
        val maxBrightness = brightnesses.maxOrNull() ?: 0f
        val minBrightness = brightnesses.minOrNull() ?: 0f
        val hasHighContrast = (maxBrightness - minBrightness) > 0.6f
        
        // Check for uniform areas (low variance in brightness)
        val brightnessVariance = brightnesses.map { (it - avgBrightness) * (it - avgBrightness) }.average()
        val hasUniformAreas = brightnessVariance < 0.1
        
        // Determine dominant color
        val avgRed = colors.map { Color.red(it) }.average()
        val avgGreen = colors.map { Color.green(it) }.average()
        val avgBlue = colors.map { Color.blue(it) }.average()
        
        val dominantColorDescription = when {
            avgBrightness > 0.8 -> "very light/white"
            avgBrightness < 0.2 -> "very dark/black"
            avgRed > avgGreen + 20 && avgRed > avgBlue + 20 -> "warm/reddish"
            avgGreen > avgRed + 20 && avgGreen > avgBlue + 20 -> "green/natural"
            avgBlue > avgRed + 20 && avgBlue > avgGreen + 20 -> "cool/bluish"
            else -> "balanced/neutral"
        }
        
        return SampleResults(
            isTextLike = isTextLike,
            hasHighContrast = hasHighContrast,
            hasUniformAreas = hasUniformAreas,
            dominantColorDescription = dominantColorDescription,
            averageBrightness = avgBrightness
        )
    }
    
    fun cleanup() {
        // Nothing to cleanup for basic bitmap analysis
    }
}