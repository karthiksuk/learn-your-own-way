package com.karthik.learnmyownway.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import com.karthik.learnmyownway.services.MockAiService

data class ModelDownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val modelName: String = "",
    val error: String? = null
)

class AIRepository(private val context: Context) {
    
    private val aiService = AIService(context)
    private val mockAiService = MockAiService()
    private val imageAnalyzer = ImageAnalyzer(context)
    private var isInitialized = false
    
    private val _downloadState = MutableStateFlow(ModelDownloadState())
    val downloadState: StateFlow<ModelDownloadState> = _downloadState
    
    companion object {
        private const val TAG = "AIRepository"
        
        @Volatile
        private var INSTANCE: AIRepository? = null
        
        fun getInstance(context: Context): AIRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun initializeAI(): Result<Unit> {
        if (isInitialized) {
            return Result.success(Unit)
        }
        
        // Check if model exists in system temp - try to use directly first
        val modelInfo = ModelManager.availableModels.first()
        val systemTempPath = java.io.File("/data/local/tmp/llm", modelInfo.fileName)
        
        if (systemTempPath.exists() && systemTempPath.canRead()) {
            Log.d(TAG, "Found readable model in system temp, attempting to use directly...")
            try {
                val config = AIService.AIConfig(
                    modelPath = systemTempPath.absolutePath,
                    maxTokens = 1024,
                    topK = 40,
                    temperature = 0.7f
                )
                
                val result = aiService.initialize(config)
                if (result.isSuccess) {
                    isInitialized = true
                    Log.d(TAG, "AI service initialized successfully with system temp model: ${modelInfo.name}")
                    return result
                } else {
                    Log.w(TAG, "Failed to initialize with system temp model: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error using model from system temp directly", e)
            }
        }
        
        // Fallback: try to copy model only if direct usage failed
        if (systemTempPath.exists()) {
            Log.d(TAG, "Direct usage failed, attempting to copy model...")
            try {
                val copyResult = ModelManager.copyModelFromSystemTemp(context, modelInfo)
                if (copyResult.isFailure) {
                    Log.w(TAG, "Failed to copy model from system temp: ${copyResult.exceptionOrNull()?.message}")
                    // Don't return failure here, continue to check for existing models
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error copying model from system temp", e)
            }
        }
        
        var availableModels = ModelManager.getAllDownloadedModels(context)
        Log.d(TAG, "Available models count: ${availableModels.size}")
        
        if (availableModels.isEmpty()) {
            Log.i(TAG, "No models found. Starting automatic download...")
            val recommendedModel = ModelManager.availableModels.first()
            
            try {
                // Update download state - starting download
                _downloadState.value = ModelDownloadState(
                    isDownloading = true,
                    progress = 0f,
                    modelName = recommendedModel.name,
                    error = null
                )
                
                val downloadResult = ModelManager.downloadModel(context, recommendedModel) { progress ->
                    Log.d(TAG, "Download progress: ${progress.toInt()}%")
                    _downloadState.value = _downloadState.value.copy(progress = progress)
                }
                
                if (downloadResult.isSuccess) {
                    Log.i(TAG, "Model downloaded successfully: ${downloadResult.getOrNull()}")
                    // Update download state - completed
                    _downloadState.value = ModelDownloadState(
                        isDownloading = false,
                        progress = 100f,
                        modelName = recommendedModel.name,
                        error = null
                    )
                    availableModels = ModelManager.getAllDownloadedModels(context)
                } else {
                    Log.e(TAG, "Model download failed: ${downloadResult.exceptionOrNull()?.message}")
                    // Update download state - error
                    _downloadState.value = ModelDownloadState(
                        isDownloading = false,
                        progress = 0f,
                        modelName = recommendedModel.name,
                        error = downloadResult.exceptionOrNull()?.message ?: "Download failed"
                    )
                    return Result.failure(Exception("Failed to download AI model: ${downloadResult.exceptionOrNull()?.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during automatic model download", e)
                // Update download state - error
                _downloadState.value = ModelDownloadState(
                    isDownloading = false,
                    progress = 0f,
                    modelName = recommendedModel.name,
                    error = e.message ?: "Download error"
                )
                return Result.failure(Exception("Failed to download AI model: ${e.message}"))
            }
        }
        
        if (availableModels.isEmpty()) {
            Log.e(TAG, "No models available after download attempt.")
            return Result.failure(Exception("No AI models available after download."))
        }
        
        val modelToUse = availableModels.first()
        val modelPath = ModelManager.getModelPath(context, modelToUse)
        
        val config = AIService.AIConfig(
            modelPath = modelPath,
            maxTokens = 1024,
            topK = 40,
            temperature = 0.7f
        )
        
        return aiService.initialize(config).also { result ->
            if (result.isSuccess) {
                isInitialized = true
                Log.d(TAG, "AI service initialized successfully with model: ${modelToUse.name}")
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to initialize AI service", error)
                
                // Check if it's an out of memory error
                if (error?.message?.contains("Out of memory", ignoreCase = true) == true || 
                    error?.message?.contains("Failed to map", ignoreCase = true) == true) {
                    Log.e(TAG, "Out of memory error detected. The model (${modelToUse.sizeInMB}MB) is too large for available RAM.")
                    return Result.failure(Exception(
                        "Not enough memory to load AI model (${modelToUse.sizeInMB}MB). " +
                        "Please increase emulator RAM or use a physical device with more memory."
                    ))
                }
            }
        }
    }
    
    fun generateEducationalContent(topic: String, analogyStyle: String): Flow<String> = flow {
        // Assume AI is already initialized by caller
        if (!isInitialized) {
            Log.w(TAG, "AI not initialized, please call initializeAI() first")
            try {
                val mockExplanation = mockAiService.generateExplanation(topic, analogyStyle)
                emit("⚠️ Using demo content generator:\n\n${mockExplanation.explanation}")
            } catch (e: Exception) {
                emit("Error: Unable to generate content. ${e.message}")
            }
            return@flow
        }
        
        aiService.generateEducationalContent(topic, analogyStyle).collect { result ->
            result.fold(
                onSuccess = { content -> emit(content) },
                onFailure = { error -> 
                    Log.e(TAG, "Error generating educational content", error)
                    // Try fallback to mock service
                    try {
                        val mockExplanation = mockAiService.generateExplanation(topic, analogyStyle)
                        emit("⚠️ Using fallback content generator:\n\n${mockExplanation.explanation}")
                    } catch (e: Exception) {
                        emit("Error generating content: ${error.message}")
                    }
                }
            )
        }
    }

    fun generatePage(topic: String, analogyStyle: String, pageType: String): Flow<String> = flow {
        if (!isInitialized) {
            Log.w(TAG, "AI not initialized, using mock content for page: $pageType")
            try {
                val mockExplanation = mockAiService.generateExplanation(topic, analogyStyle)
                emit("⚠️ Demo Page ($pageType):\n\n${mockExplanation.explanation}")
            } catch (e: Exception) {
                emit("Error: Unable to generate page content. ${e.message}")
            }
            return@flow
        }
        
        aiService.generatePage(topic, analogyStyle, pageType).collect { result ->
            result.fold(
                onSuccess = { content -> emit(content) },
                onFailure = { error -> 
                    Log.e(TAG, "Error generating page: $pageType", error)
                    // Try fallback to mock service
                    try {
                        val mockExplanation = mockAiService.generateExplanation(topic, analogyStyle)
                        emit("⚠️ Fallback Page ($pageType):\n\n${mockExplanation.explanation}")
                    } catch (e: Exception) {
                        emit("Error generating page: ${error.message}")
                    }
                }
            )
        }
    }

    fun getConceptsForTopic(topic: String): List<String> {
        return aiService.getConceptsForTopic(topic)
    }

    fun generateConceptExplanation(concept: String, analogyStyle: String): Flow<String> = flow {
        if (!isInitialized) {
            Log.w(TAG, "AI not initialized, using mock content for concept: $concept")
            try {
                val mockExplanation = mockAiService.generateExplanation(concept, analogyStyle)
                // Extract just first 1-2 sentences for brevity
                val briefExplanation = mockExplanation.explanation.split(". ").take(2).joinToString(". ").let {
                    if (it.endsWith(".")) it else "$it."
                }
                emit("⚠️ Demo: $briefExplanation")
            } catch (e: Exception) {
                emit("Error: Unable to generate concept explanation. ${e.message}")
            }
            return@flow
        }
        
        aiService.generateConceptExplanation(concept, analogyStyle).collect { result ->
            result.fold(
                onSuccess = { content -> emit(content) },
                onFailure = { error -> 
                    Log.e(TAG, "Error generating concept explanation: $concept", error)
                    // Try fallback to mock service
                    try {
                        val mockExplanation = mockAiService.generateExplanation(concept, analogyStyle)
                        val briefExplanation = mockExplanation.explanation.split(". ").take(2).joinToString(". ").let {
                            if (it.endsWith(".")) it else "$it."
                        }
                        emit("⚠️ Fallback: $briefExplanation")
                    } catch (e: Exception) {
                        emit("Error generating concept: ${error.message}")
                    }
                }
            )
        }
    }
    
    fun analyzeImageFromUri(imageUri: String, analogyStyle: String): Flow<String> = flow {
        Log.d(TAG, "Starting image analysis for URI: $imageUri")
        
        try {
            // Step 1: Analyze the actual image to get description
            emit("🔍 Analyzing image content...")
            
            val imageAnalysisResult = imageAnalyzer.analyzeImage(imageUri)
            if (imageAnalysisResult.isFailure) {
                emit("Error: Failed to analyze image - ${imageAnalysisResult.exceptionOrNull()?.message}")
                return@flow
            }
            
            val imageDescription = imageAnalysisResult.getOrNull() ?: "Unable to analyze image content"
            Log.d(TAG, "Image analysis completed: $imageDescription")
            
            // Step 2: Generate AI explanation using the image description
            emit("✨ Generating explanation...")
            
            // Initialize AI if needed
            if (!isInitialized) {
                val initResult = initializeAI()
                if (initResult.isFailure) {
                    Log.i(TAG, "AI initialization failed, using mock explanation")
                    try {
                        val mockExplanation = mockAiService.generateExplanation(imageDescription, analogyStyle)
                        emit("📸 Image Analysis:\n\n## What I understand from this image\n$imageDescription\n\n## Explanation using $analogyStyle analogies\n${mockExplanation.explanation}")
                    } catch (e: Exception) {
                        emit("Error: Unable to generate explanation. ${e.message}")
                    }
                    return@flow
                }
            }
            
            // Step 3: Use AI service to generate explanation
            aiService.analyzeImageWithText(imageDescription, analogyStyle).collect { result ->
                result.fold(
                    onSuccess = { content -> emit(content) },
                    onFailure = { error -> 
                        Log.e(TAG, "Error generating AI explanation", error)
                        // Fallback to mock service with the real image description
                        try {
                            val mockExplanation = mockAiService.generateExplanation(imageDescription, analogyStyle)
                            emit("📸 Image Analysis:\n\n## What I understand from this image\n$imageDescription\n\n## Explanation using $analogyStyle analogies\n${mockExplanation.explanation}")
                        } catch (e: Exception) {
                            emit("Error generating explanation: ${error.message}")
                        }
                    }
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in image analysis flow", e)
            emit("Error: ${e.message}")
        }
    }

    fun analyzeImageContent(imageDescription: String, analogyStyle: String): Flow<String> = flow {
        // Always try to initialize AI first (this will trigger download if needed)
        if (!isInitialized) {
            val initResult = initializeAI()
            if (initResult.isFailure) {
                // Fallback to mock service if real AI fails
                Log.i(TAG, "Real AI initialization failed, falling back to mock service")
                try {
                    val mockExplanation = mockAiService.generateExplanation(imageDescription, analogyStyle)
                    emit("📸 Image Analysis (Demo Mode):\n\n${mockExplanation.explanation}")
                } catch (e: Exception) {
                    emit("Error: Unable to analyze image. ${e.message}")
                }
                return@flow
            }
        }
        
        aiService.analyzeImageWithText(imageDescription, analogyStyle).collect { result ->
            result.fold(
                onSuccess = { content -> emit(content) },
                onFailure = { error -> 
                    Log.e(TAG, "Error analyzing image content", error)
                    // Try fallback to mock service
                    try {
                        val mockExplanation = mockAiService.generateExplanation(imageDescription, analogyStyle)
                        emit("⚠️ Using fallback image analyzer:\n\n${mockExplanation.explanation}")
                    } catch (e: Exception) {
                        emit("Error analyzing image: ${error.message}")
                    }
                }
            )
        }
    }
    
    suspend fun generateResponse(prompt: String): String {
        if (!isInitialized) {
            val initResult = initializeAI()
            if (initResult.isFailure) {
                return "Error: AI service not available. Please ensure a model is downloaded and try again."
            }
        }
        
        return aiService.generateResponse(prompt).fold(
            onSuccess = { it },
            onFailure = { error ->
                Log.e(TAG, "Error generating response", error)
                "Error: ${error.message}"
            }
        )
    }
    
    fun isModelAvailable(): Boolean {
        return ModelManager.getAllDownloadedModels(context).isNotEmpty()
    }
    
    fun getAvailableModels(): List<ModelManager.ModelInfo> {
        return ModelManager.availableModels
    }
    
    fun getDownloadedModels(): List<ModelManager.ModelInfo> {
        return ModelManager.getAllDownloadedModels(context)
    }
    
    suspend fun downloadModel(
        modelInfo: ModelManager.ModelInfo,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        return ModelManager.downloadModel(context, modelInfo, onProgress)
    }
    
    fun isReady(): Boolean = isInitialized && aiService.isReady()
    
    fun cleanup() {
        aiService.cleanup()
        isInitialized = false
    }
}