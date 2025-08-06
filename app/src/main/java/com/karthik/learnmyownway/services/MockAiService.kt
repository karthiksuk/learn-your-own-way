package com.karthik.learnmyownway.services

import android.net.Uri
import com.karthik.learnmyownway.data.models.*
import kotlinx.coroutines.delay
import kotlin.random.Random

class MockAiService {
    
    suspend fun generateExplanation(topic: String, analogyStyle: String): LearningExplanation {
        delay(2000) // Simulate API call
        
        val explanation = when (analogyStyle.lowercase()) {
            "chef" -> generateChefExplanation(topic)
            "mechanic" -> generateMechanicExplanation(topic)
            "musician" -> generateMusicianExplanation(topic)
            "gardener" -> generateGardenerExplanation(topic)
            "builder" -> generateBuilderExplanation(topic)
            "artist" -> generateArtistExplanation(topic)
            "athlete" -> generateAthleteExplanation(topic)
            "teacher" -> generateTeacherExplanation(topic)
            else -> generateGenericExplanation(topic, analogyStyle)
        }
        
        return LearningExplanation(
            id = generateId(),
            topic = topic,
            explanation = explanation,
            analogyStyle = analogyStyle,
            wordCount = explanation.split(" ").size,
            keyPoints = extractKeyPoints(explanation)
        )
    }
    
    suspend fun analyzePhoto(imageUri: Uri, analogyStyle: String): PhotoAnalysis {
        delay(3000) // Simulate image processing
        
        val mockConcepts = listOf(
            "photosynthesis", "quantum mechanics", "neural networks", "cellular respiration",
            "electromagnetic waves", "molecular structure", "ecosystem dynamics", "genetic code"
        )
        
        val concept = mockConcepts.random()
        val explanation = generateExplanation(concept, analogyStyle)
        
        return PhotoAnalysis(
            id = generateId(),
            imageUri = imageUri,
            detectedObjects = listOf("organism", "structure", "pattern"),
            mainConcept = concept,
            explanation = explanation,
            confidence = 0.85f + Random.nextFloat() * 0.15f,
            processingTimeMs = 2500 + Random.nextLong(1000)
        )
    }
    
    suspend fun generateCourse(topic: String, analogyStyle: String): Course {
        delay(1500) // Simulate course generation
        
        val chapters = generateChapters(topic, analogyStyle)
        val totalDuration = chapters.sumOf { it.estimatedDuration }
        
        return Course(
            id = generateId(),
            title = "Mastering $topic",
            description = "A comprehensive course on $topic explained through $analogyStyle analogies",
            topic = topic,
            analogyStyle = analogyStyle,
            chapters = chapters,
            estimatedDuration = totalDuration,
            language = "English"
        )
    }
    
    private fun generateChapters(topic: String, analogyStyle: String): List<Chapter> {
        val chapterTemplates = listOf(
            "Introduction to" to ChapterDifficulty.BEGINNER,
            "Fundamentals of" to ChapterDifficulty.BEGINNER,
            "Practical Applications of" to ChapterDifficulty.INTERMEDIATE,
            "Advanced Concepts in" to ChapterDifficulty.ADVANCED,
            "Mastering" to ChapterDifficulty.ADVANCED
        )
        
        return chapterTemplates.mapIndexed { index, (prefix, difficulty) ->
            Chapter(
                id = generateId(),
                title = "$prefix $topic",
                description = "Learn $topic through $analogyStyle perspectives",
                difficulty = difficulty,
                estimatedDuration = when (difficulty) {
                    ChapterDifficulty.BEGINNER -> 15
                    ChapterDifficulty.INTERMEDIATE -> 25
                    ChapterDifficulty.ADVANCED -> 35
                },
                order = index + 1
            )
        }
    }
    
    private fun generateChefExplanation(topic: String): String {
        return "Think of $topic like preparing a complex dish. Just as a chef needs to understand ingredients, timing, and technique, mastering $topic requires understanding its core components and how they work together. The process is like following a recipe - you need the right ingredients (knowledge), proper preparation (study), and careful execution (practice). Each element must be balanced perfectly, just like seasoning a dish. When you rush the process, like overcooking, you might miss crucial details that make the difference between good and exceptional results."
    }
    
    private fun generateMechanicExplanation(topic: String): String {
        return "Understanding $topic is like diagnosing and fixing an engine. You need to know how all the parts work together - each component has a specific function, and when one fails, it affects the whole system. Just like a mechanic uses diagnostic tools to identify problems, learning $topic requires breaking it down into manageable parts. You start with the basics (like checking fluid levels), then move to more complex systems. Regular maintenance and understanding prevents major breakdowns, just like consistent study prevents knowledge gaps."
    }
    
    private fun generateMusicianExplanation(topic: String): String {
        return "Learning $topic is like mastering a musical composition. Each concept is like a note that must harmonize with others to create beautiful music. Just as musicians practice scales before performing symphonies, you need to master the fundamentals before tackling complex pieces. The rhythm of learning requires consistent practice, and like a conductor coordinates an orchestra, you must coordinate different aspects of $topic. Each practice session builds muscle memory, making complex performances feel natural over time."
    }
    
    private fun generateGardenerExplanation(topic: String): String {
        return "Growing your understanding of $topic is like cultivating a garden. You start by preparing the soil (foundation knowledge), plant seeds (new concepts), and nurture them with regular care (practice). Some ideas bloom quickly like annuals, while others take time to develop like perennial plants. Just as gardens need different nutrients, learning $topic requires diverse approaches. Patience is essential - forcing growth leads to weak plants, but steady cultivation creates robust, deep-rooted understanding that flourishes season after season."
    }
    
    private fun generateBuilderExplanation(topic: String): String {
        return "Mastering $topic is like constructing a solid building. You begin with a strong foundation of basic principles, then frame the structure with core concepts. Each new piece of knowledge is like adding another component - walls, electrical, plumbing - all interconnected and supporting the whole. Just as builders follow blueprints and building codes, learning $topic requires following proven methods and best practices. Rushing construction leads to structural problems, but taking time to build properly creates something that stands the test of time."
    }
    
    private fun generateArtistExplanation(topic: String): String {
        return "Understanding $topic is like creating a masterpiece painting. You start with a blank canvas (your current knowledge) and begin with basic sketches (fundamental concepts). Each new layer of understanding adds depth and richness, like building up colors and textures. Different techniques serve different purposes - some broad strokes establish the overall composition, while fine details bring the work to life. Mistakes aren't failures but opportunities to learn new techniques. The creative process requires both technical skill and intuitive understanding."
    }
    
    private fun generateAthleteExplanation(topic: String): String {
        return "Training to understand $topic is like preparing for athletic competition. You need a structured training regimen, starting with basic conditioning (fundamentals) and progressing to sport-specific skills (advanced concepts). Consistent practice builds muscle memory and confidence. Just as athletes study game film, you must review and analyze your understanding regularly. Some days training feels harder than others, but persistence and proper technique lead to breakthrough performances. Mental preparation is as important as physical training."
    }
    
    private fun generateTeacherExplanation(topic: String): String {
        return "Learning $topic follows the same principles as effective teaching. You begin with clear learning objectives and assess prior knowledge. Break complex ideas into digestible lessons, using various methods to accommodate different learning styles. Regular assessment helps identify areas needing reinforcement. Just as teachers adapt their approach based on student needs, your learning strategy should evolve. Connecting new information to existing knowledge creates stronger neural pathways. Teaching others what you've learned is the ultimate test of understanding."
    }
    
    private fun generateGenericExplanation(topic: String, style: String): String {
        return "Understanding $topic through the lens of $style provides a unique perspective that makes complex concepts more relatable. By connecting abstract ideas to familiar $style experiences, you create mental bridges that enhance comprehension and retention. This approach transforms intimidating subjects into manageable, engaging learning experiences. Just as professionals in $style develop expertise through practice and experience, mastering $topic requires dedication, patience, and the right approach to break down complexity into understandable components."
    }
    
    private fun extractKeyPoints(explanation: String): List<String> {
        return explanation.split(". ")
            .take(3)
            .map { it.replace(Regex("[.,!?]"), "").trim() }
            .filter { it.isNotEmpty() }
    }
    
    private fun generateId(): String {
        return "id_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }
}