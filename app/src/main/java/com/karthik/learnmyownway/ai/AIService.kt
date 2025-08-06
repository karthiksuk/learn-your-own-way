package com.karthik.learnmyownway.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class AIService(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    
    data class AIConfig(
        val modelPath: String,
        val maxTokens: Int = 256,
        val topK: Int = 40,
        val temperature: Float = 0.9f,
        val randomSeed: Int = 0
    )
    
    suspend fun initialize(config: AIConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                return@withContext Result.success(Unit)
            }
            
            val modelFile = File(config.modelPath)
            if (!modelFile.exists()) {
                return@withContext Result.failure(
                    Exception("Model file not found at: ${config.modelPath}")
                )
            }
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(config.modelPath)
                .setMaxTopK(config.topK)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inference = llmInference ?: return@withContext Result.failure(
                Exception("AI service not initialized. Call initialize() first.")
            )
            
            val response = inference.generateResponse(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun generateResponseStream(prompt: String): Flow<Result<String>> = callbackFlow {
        try {
            val inference = llmInference ?: throw Exception(
                "AI service not initialized. Call initialize() first."
            )

            inference.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult.isNotEmpty()) {
                    trySend(Result.success(partialResult))
                }
                if (done) {
                    close()
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
        
        awaitClose { }
    }.flowOn(Dispatchers.IO)
    
    fun generateEducationalContent(
        topic: String, 
        analogyStyle: String = "simple"
    ): Flow<Result<String>> = callbackFlow {
        val prompt = buildBlogStylePrompt(topic, analogyStyle)
        
        android.util.Log.d("AIService", "Generating blog-style content for: $topic with analogy: $analogyStyle")
        android.util.Log.d("AIService", "Generated prompt: $prompt")

        try {
            val inference = llmInference ?: throw Exception(
                "AI service not initialized. Call initialize() first."
            )
            
            inference.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult.isNotEmpty()) {
                    trySend(Result.success(partialResult))
                }
                if (done) {
                    android.util.Log.d("AIService", "Blog-style content completed")
                    close()
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AIService", "Exception during generation", e)
            trySend(Result.failure(e))
            close(e)
        }
        
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    private fun buildBlogStylePrompt(topic: String, analogyStyle: String): String {
        return """
Write about "$topic" using $analogyStyle analogies:
## Introduction
Brief intro with $analogyStyle analogy (2 sentences)

## Key Concepts
5 concepts, each: heading + 1-2 sentences using $analogyStyle analogies

Keep under 300 words total.
        """.trimIndent()
    }

    private fun buildConceptPrompt(concept: String, analogyStyle: String): String {
        return when (analogyStyle.lowercase()) {
            "simple" -> "Explain $concept in 1-2 sentences using a simple everyday analogy."
            "professional" -> "Explain $concept in 1-2 sentences using a business analogy."
            "creative" -> "Explain $concept in 1-2 sentences using a creative or fun analogy."
            else -> "Explain $concept in 1-2 sentences using $analogyStyle analogies."
        }
    }

    fun generateConceptExplanation(
        concept: String,
        analogyStyle: String = "simple"
    ): Flow<Result<String>> = callbackFlow {
        android.util.Log.d("AIService", "generateConceptExplanation called with concept: $concept, analogyStyle: $analogyStyle")
        val prompt = buildConceptPrompt(concept, analogyStyle)
        
        android.util.Log.d("AIService", "Generated prompt: $prompt")

        try {
            val inference = llmInference ?: throw Exception(
                "AI service not initialized. Call initialize() first."
            )
            
            inference.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult.isNotEmpty()) {
                    trySend(Result.success(partialResult))
                }
                if (done) {
                    android.util.Log.d("AIService", "Concept explanation completed for: $concept")
                    close()
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AIService", "Exception during concept generation", e)
            trySend(Result.failure(e))
            close(e)
        }
        
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    fun getConceptsForTopic(topic: String): List<String> {
        // Return a predefined list of common concepts for any topic
        // This avoids waiting for AI to generate the list
        return listOf(
            "What is $topic?",
            "Key features of $topic",
            "How $topic works",
            "Benefits of $topic",
            "Common uses of $topic",
            "Examples of $topic",
            "Getting started with $topic"
        )
    }

    fun generatePage(
        topic: String,
        analogyStyle: String,
        pageType: String
    ): Flow<Result<String>> = callbackFlow {
        val prompt = buildConceptPrompt("$pageType about $topic", analogyStyle)
        
        android.util.Log.d("AIService", "Generating $pageType page for: $topic")

        try {
            val inference = llmInference ?: throw Exception(
                "AI service not initialized. Call initialize() first."
            )
            
            inference.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult.isNotEmpty()) {
                    trySend(Result.success(partialResult))
                }
                if (done) {
                    android.util.Log.d("AIService", "$pageType page completed")
                    close()
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AIService", "Exception during page generation", e)
            trySend(Result.failure(e))
            close(e)
        }
        
        awaitClose { }
    }.flowOn(Dispatchers.IO)
    
    fun analyzeImageWithText(
        imageDescription: String,
        analogyStyle: String = "simple"
    ): Flow<Result<String>> = callbackFlow {
        val prompt = buildImageAnalysisPrompt(imageDescription, analogyStyle)

        try {
            val inference = llmInference ?: throw Exception(
                "AI service not initialized. Call initialize() first."
            )

            inference.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult.isNotEmpty()) {
                    trySend(Result.success(partialResult))
                }
                if (done) {
                    close()
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
        
        awaitClose { }
    }.flowOn(Dispatchers.IO)
    
    private fun buildEducationalPrompt(topic: String, analogyStyle: String): String {
        return when (analogyStyle.lowercase()) {
            "simple" -> """
Create a comprehensive learning guide about "$topic" using simple analogies and everyday examples.

Please structure your response as follows:
1. **Introduction**: Brief overview using a relatable analogy
2. **Key Concepts**: Break down main ideas with simple comparisons
3. **Step-by-Step Explanation**: Use everyday examples to explain processes
4. **Real-World Applications**: Show how this applies in daily life
5. **Quick Summary**: Memorable takeaways

Use conversational language and make complex ideas accessible through familiar comparisons.
            """.trimIndent()
            
            "professional" -> """
Provide a structured educational analysis of "$topic" with professional analogies and business contexts.

Include:
1. **Executive Summary**: Professional overview with industry analogies
2. **Core Principles**: Key concepts with business/professional comparisons
3. **Implementation**: Practical steps using workplace examples
4. **Strategic Applications**: Professional use cases and scenarios
5. **Best Practices**: Industry-standard approaches

Use professional terminology while maintaining clarity through relevant analogies.
            """.trimIndent()
            
            "creative" -> """
Explain "$topic" using creative, imaginative analogies and storytelling approaches.

Structure:
1. **Story Introduction**: Begin with a creative narrative or metaphor
2. **Character-Based Concepts**: Use characters or scenarios to explain ideas
3. **Adventure Through Learning**: Take the reader on a journey of discovery
4. **Creative Applications**: Unusual but memorable use cases
5. **Story Conclusion**: Wrap up with a memorable creative summary

Be imaginative, use vivid metaphors, and make learning feel like an adventure.
            """.trimIndent()
            
            else -> buildEducationalPrompt(topic, "simple")
        }
    }
    
    private fun buildImageAnalysisPrompt(imageDescription: String, analogyStyle: String): String {
        return """
Analyze image: $imageDescription

## What I understand from this image
First describe what you see clearly (2-3 sentences)

## Explanation using $analogyStyle analogies  
Now explain the key concepts using $analogyStyle analogies (3-4 sentences)

Keep under 150 words total.
        """.trimIndent()
    }
    
    fun isReady(): Boolean = isInitialized && llmInference != null
    
    fun cleanup() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }
}