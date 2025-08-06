package com.karthik.learnmyownway.data.models

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val topic: String,
    val analogyStyle: String,
    val chapters: List<Chapter>,
    val estimatedDuration: Int, // in minutes
    val language: String = "English",
    val progress: Int = 0 // percentage completed
)

data class Chapter(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: ChapterDifficulty,
    val estimatedDuration: Int, // in minutes
    val content: String = "",
    val isCompleted: Boolean = false,
    val order: Int
)

enum class ChapterDifficulty(val displayName: String, val color: String) {
    BEGINNER("Beginner", "#4CAF50"),
    INTERMEDIATE("Intermediate", "#FF9800"),
    ADVANCED("Advanced", "#F44336")
}

data class LearningExplanation(
    val id: String,
    val topic: String,
    val explanation: String,
    val analogyStyle: String,
    val wordCount: Int,
    val keyPoints: List<String> = emptyList()
)