package com.karthik.learnmyownway.data.models

data class SavedCourse(
    val id: String,
    val topic: String,
    val analogyStyle: String,
    val content: String,
    val savedTimestamp: Long,
    val isComplete: Boolean = true,
    val wordCount: Int = 0
)