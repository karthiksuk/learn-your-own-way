package com.karthik.learnmyownway.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ModelManager {
    
    private const val TAG = "ModelManager"
    
    data class ModelInfo(
        val name: String,
        val fileName: String,
        val downloadUrl: String,
        val sizeInMB: Int,
        val description: String
    )
    
    val availableModels = listOf(
        ModelInfo(
            name = "Gemma3N 2B",
            fileName = "gemma-3n-E2B-it-int4.task",
            downloadUrl = "google/gemma-3n-E2B-it-int4.task", //Change this to Hugging Face
            sizeInMB = 2990,
            description = "Compact model optimized for on-device learning experiences"
        )
    )
    
    fun getModelPath(context: Context, modelInfo: ModelInfo): String {
        // First check if model exists in system temp location and is readable
        val systemTempPath = File("/data/local/tmp/llm", modelInfo.fileName)
        if (systemTempPath.exists() && systemTempPath.canRead() && systemTempPath.length() > 0) {
            Log.d(TAG, "Found model in system temp location: ${systemTempPath.absolutePath}")
            return systemTempPath.absolutePath
        }
        
        // Fall back to app's internal directory
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return File(modelsDir, modelInfo.fileName).absolutePath
    }
    
    fun isModelDownloaded(context: Context, modelInfo: ModelInfo): Boolean {
        // Check system temp location first
        val systemTempPath = File("/data/local/tmp/llm", modelInfo.fileName)
        Log.d(TAG, "Checking system temp path: ${systemTempPath.absolutePath}")
        Log.d(TAG, "System temp path exists: ${systemTempPath.exists()}")
        Log.d(TAG, "System temp path readable: ${systemTempPath.canRead()}")
        
        if (systemTempPath.exists() && systemTempPath.canRead() && systemTempPath.length() > 0) {
            Log.d(TAG, "Model found in system temp: ${systemTempPath.absolutePath} (${systemTempPath.length()} bytes)")
            return true
        }
        
        // Check app's internal directory
        val modelsDir = File(context.filesDir, "models")
        val appModelFile = File(modelsDir, modelInfo.fileName)
        val exists = appModelFile.exists() && appModelFile.length() > 0
        
        if (exists) {
            Log.d(TAG, "Model found in app directory: ${appModelFile.absolutePath} (${appModelFile.length()} bytes)")
        } else {
            Log.d(TAG, "Model not found in either location. Checked: ${systemTempPath.absolutePath} and ${appModelFile.absolutePath}")
        }
        
        return exists
    }
    
    suspend fun downloadModel(
        context: Context,
        modelInfo: ModelInfo,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelPath = getModelPath(context, modelInfo)
            val modelFile = File(modelPath)
            
            if (modelFile.exists()) {
                Log.d(TAG, "Model already exists: $modelPath")
                return@withContext Result.success(modelPath)
            }
            
            Log.d(TAG, "Starting download: ${modelInfo.downloadUrl}")
            
            val url = URL(modelInfo.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    Exception("HTTP error: ${connection.responseCode}")
                )
            }
            
            val fileLength = connection.contentLength
            val input: InputStream = connection.inputStream
            val output = FileOutputStream(modelFile)
            
            val buffer = ByteArray(8192)
            var total: Long = 0
            var count: Int
            
            while (input.read(buffer).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    onProgress((total * 100 / fileLength).toFloat())
                }
                output.write(buffer, 0, count)
            }
            
            output.close()
            input.close()
            connection.disconnect()
            
            Log.d(TAG, "Download completed: $modelPath")
            Result.success(modelPath)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }
    
    fun getRecommendedModel(): ModelInfo {
        return availableModels.first()
    }
    
    fun deleteModel(context: Context, modelInfo: ModelInfo): Boolean {
        val modelFile = File(getModelPath(context, modelInfo))
        return if (modelFile.exists()) {
            modelFile.delete()
        } else {
            true
        }
    }
    
    fun getModelSize(context: Context, modelInfo: ModelInfo): Long {
        val modelFile = File(getModelPath(context, modelInfo))
        return if (modelFile.exists()) {
            modelFile.length()
        } else {
            0L
        }
    }
    
    fun getAllDownloadedModels(context: Context): List<ModelInfo> {
        return availableModels.filter { isModelDownloaded(context, it) }
    }
    
    /**
     * Copy model from system temp location to app's accessible directory
     */
    suspend fun copyModelFromSystemTemp(context: Context, modelInfo: ModelInfo): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemTempPath = File("/data/local/tmp/llm", modelInfo.fileName)
            
            if (!systemTempPath.exists() || !systemTempPath.canRead()) {
                return@withContext Result.failure(
                    Exception("Model not found or not readable in system temp: ${systemTempPath.absolutePath}")
                )
            }
            
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                modelsDir.mkdirs()
            }
            
            val appModelPath = File(modelsDir, modelInfo.fileName)
            
            if (appModelPath.exists()) {
                Log.d(TAG, "Model already exists in app directory: ${appModelPath.absolutePath}")
                return@withContext Result.success(appModelPath.absolutePath)
            }
            
            Log.d(TAG, "Copying model from ${systemTempPath.absolutePath} to ${appModelPath.absolutePath}")
            
            val input = FileInputStream(systemTempPath)
            val output = FileOutputStream(appModelPath)
            
            val buffer = ByteArray(8192)
            var totalBytes = 0L
            var bytesRead: Int
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalBytes += bytesRead
            }
            
            input.close()
            output.close()
            
            Log.d(TAG, "Model copy completed: ${appModelPath.absolutePath} ($totalBytes bytes)")
            Result.success(appModelPath.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy model from system temp", e)
            Result.failure(e)
        }
    }
}
