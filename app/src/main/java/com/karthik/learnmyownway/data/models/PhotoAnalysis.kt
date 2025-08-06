package com.karthik.learnmyownway.data.models

import android.net.Uri

data class PhotoAnalysis(
    val id: String,
    val imageUri: Uri,
    val detectedObjects: List<String>,
    val mainConcept: String,
    val explanation: LearningExplanation,
    val confidence: Float,
    val processingTimeMs: Long
)

data class PhotoUploadState(
    val isUploading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val result: PhotoAnalysis? = null
)