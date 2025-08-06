package com.karthik.learnmyownway.data.repository

import android.content.Context
import android.util.Log
import com.karthik.learnmyownway.data.models.SavedCourse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID

@Serializable
private data class SavedCourseData(
    val id: String,
    val topic: String,
    val analogyStyle: String,
    val content: String,
    val savedTimestamp: Long,
    val isComplete: Boolean = true,
    val wordCount: Int = 0
)

class CourseRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "CourseRepository"
        private const val SAVED_COURSES_DIR = "saved_courses"
        
        @Volatile
        private var INSTANCE: CourseRepository? = null
        
        fun getInstance(context: Context): CourseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CourseRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private fun getSavedCoursesDir(): File {
        val dir = File(context.filesDir, SAVED_COURSES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    suspend fun saveCourse(
        topic: String, 
        analogyStyle: String, 
        content: String
    ): Result<SavedCourse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving course: $topic")
            
            val courseId = UUID.randomUUID().toString()
            val wordCount = content.trim().split("\\s+".toRegex()).size
            
            val savedCourse = SavedCourse(
                id = courseId,
                topic = topic,
                analogyStyle = analogyStyle,
                content = content,
                savedTimestamp = System.currentTimeMillis(),
                isComplete = true,
                wordCount = wordCount
            )
            
            val courseData = SavedCourseData(
                id = savedCourse.id,
                topic = savedCourse.topic,
                analogyStyle = savedCourse.analogyStyle,
                content = savedCourse.content,
                savedTimestamp = savedCourse.savedTimestamp,
                isComplete = savedCourse.isComplete,
                wordCount = savedCourse.wordCount
            )
            
            val courseFile = File(getSavedCoursesDir(), "${courseId}.json")
            val jsonString = json.encodeToString(courseData)
            courseFile.writeText(jsonString)
            
            Log.d(TAG, "Course saved successfully: ${savedCourse.id}")
            Result.success(savedCourse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving course", e)
            Result.failure(e)
        }
    }
    
    suspend fun getSavedCourses(): Result<List<SavedCourse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading saved courses")
            
            val coursesDir = getSavedCoursesDir()
            val courseFiles = coursesDir.listFiles { file -> file.extension == "json" } ?: emptyArray()
            
            val courses = courseFiles.mapNotNull { file ->
                try {
                    val jsonString = file.readText()
                    val courseData = json.decodeFromString<SavedCourseData>(jsonString)
                    SavedCourse(
                        id = courseData.id,
                        topic = courseData.topic,
                        analogyStyle = courseData.analogyStyle,
                        content = courseData.content,
                        savedTimestamp = courseData.savedTimestamp,
                        isComplete = courseData.isComplete,
                        wordCount = courseData.wordCount
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error loading course file: ${file.name}", e)
                    null
                }
            }.sortedByDescending { it.savedTimestamp }
            
            Log.d(TAG, "Loaded ${courses.size} saved courses")
            Result.success(courses)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved courses", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteCourse(courseId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting course: $courseId")
            
            val courseFile = File(getSavedCoursesDir(), "${courseId}.json")
            val deleted = courseFile.delete()
            
            if (deleted) {
                Log.d(TAG, "Course deleted successfully: $courseId")
                Result.success(true)
            } else {
                Log.w(TAG, "Course file not found: $courseId")
                Result.success(false)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting course: $courseId", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCourseById(courseId: String): Result<SavedCourse?> = withContext(Dispatchers.IO) {
        try {
            val courseFile = File(getSavedCoursesDir(), "${courseId}.json")
            if (!courseFile.exists()) {
                return@withContext Result.success(null)
            }
            
            val jsonString = courseFile.readText()
            val courseData = json.decodeFromString<SavedCourseData>(jsonString)
            
            val course = SavedCourse(
                id = courseData.id,
                topic = courseData.topic,
                analogyStyle = courseData.analogyStyle,
                content = courseData.content,
                savedTimestamp = courseData.savedTimestamp,
                isComplete = courseData.isComplete,
                wordCount = courseData.wordCount
            )
            
            Result.success(course)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading course: $courseId", e)
            Result.failure(e)
        }
    }
}