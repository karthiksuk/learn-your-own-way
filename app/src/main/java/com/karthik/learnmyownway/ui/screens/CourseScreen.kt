package com.karthik.learnmyownway.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.karthik.learnmyownway.data.repository.CourseRepository
import com.karthik.learnmyownway.ai.AIRepository
import com.karthik.learnmyownway.ai.ModelDownloadState
import com.karthik.learnmyownway.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    topic: String,
    analogyStyle: String,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var courseContent by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val aiRepository = remember { AIRepository.getInstance(context) }
    val downloadState by aiRepository.downloadState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(topic, analogyStyle) {
        scope.launch {
            try {
                isLoading = true
                error = null
                courseContent = ""
                
                // First ensure AI is initialized (this will trigger download if needed)
                val initResult = aiRepository.initializeAI()
                if (initResult.isFailure) {
                    val errorMessage = initResult.exceptionOrNull()?.message ?: "Unknown error"
                    
                    // If it's a memory error, the UI will show the special error screen
                    // and we'll continue to use MockAiService for content generation
                    if (errorMessage.contains("Not enough memory", ignoreCase = true)) {
                        Log.i("CourseScreen", "Memory error detected, will use demo content after showing error")
                        // Don't return here - let the UI show error screen first, then continue
                    } else {
                        error = "AI initialization failed: $errorMessage"
                        isLoading = false
                        return@launch
                    }
                }
                
                // Start streaming content immediately
                isLoading = false
                
                aiRepository.generateEducationalContent(topic, analogyStyle).collect { content ->
                    if (content.startsWith("Error:")) {
                        error = content
                    } else {
                        courseContent += content
                    }
                }
            } catch (e: Exception) {
                error = "Failed to generate course: ${e.message}"
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamLight)
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    text = if (courseContent.isNotEmpty()) "Learn: $topic" else "Loading...",
                    color = BrownDark,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrownDark
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateHome) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = BrownDark
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CreamLight
            )
        )
        
        when {
            downloadState.isDownloading -> {
                ModelDownloadContent(downloadState = downloadState)
            }
            downloadState.error != null -> {
                val errorMessage = downloadState.error
                if (errorMessage?.contains("Not enough memory", ignoreCase = true) == true) {
                    OutOfMemoryErrorContent()
                } else {
                    ErrorCourseContent(error = "Model download failed: $errorMessage")
                }
            }
            isLoading -> {
                LoadingCourseContent()
            }
            error != null -> {
                ErrorCourseContent(error = error!!)
            }
            else -> {
                BlogStyleCourseContent(
                    topic = topic,
                    analogyStyle = analogyStyle,
                    content = courseContent,
                    isStreaming = true
                )
            }
        }
    }
}

@Composable
private fun LoadingCourseContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BrownMedium,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Generating your personalized course...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = BrownDark,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Creating chapters tailored to your learning style",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                textAlign = TextAlign.Center
            )
        }
    }
}





@Composable
private fun ErrorCourseContent(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Course Generation Failed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AccentRed,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun ModelDownloadContent(downloadState: ModelDownloadState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📥",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Downloading AI Model",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = downloadState.modelName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = { downloadState.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrownMedium,
                    trackColor = CreamPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${downloadState.progress.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This may take a few minutes. Please wait...",
                    style = MaterialTheme.typography.bodySmall.copy(color = BrownMedium),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OutOfMemoryErrorContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Not Enough Memory",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AccentRed,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "The AI model (3GB) requires more memory than available. To fix this:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = BrownDark),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "• Increase emulator RAM to 8GB+",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "• Use a physical device with more RAM",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "• Close other apps to free memory",
                        style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "For now, using demo content generator...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrownMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun BlogStyleCourseContent(
    topic: String,
    analogyStyle: String,
    content: String,
    isStreaming: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val courseRepository = remember { CourseRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var isSaved by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Enhanced Course Header with Gradient Background and Save Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box {
                // Background gradient effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    CreamPrimary.copy(alpha = 0.4f),
                                    BeigeWarm.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
                
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "✨ $topic",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = BrownDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "🎯 Explained using $analogyStyle analogies",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = BrownMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        
                        // Save Button
                        if (!isStreaming && content.isNotEmpty()) {
                            if (isSaved) {
                                // Saved indicator
                                Surface(
                                    color = AccentGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Saved",
                                            tint = AccentGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Saved",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = AccentGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Save button
                                Button(
                                    onClick = {
                                        if (!isSaving && content.trim().isNotEmpty()) {
                                            scope.launch {
                                                isSaving = true
                                                saveError = null
                                                try {
                                                    val result = courseRepository.saveCourse(topic, analogyStyle, content)
                                                    if (result.isSuccess) {
                                                        isSaved = true
                                                    } else {
                                                        saveError = "Failed to save course"
                                                    }
                                                } catch (e: Exception) {
                                                    saveError = "Error: ${e.message}"
                                                } finally {
                                                    isSaving = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isSaving && content.trim().isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrownMedium,
                                        contentColor = White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .height(36.dp)
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            color = White,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FavoriteBorder,
                                            contentDescription = "Save",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Save",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Streaming indicator
                    if (isStreaming) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = BrownMedium,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Content is being generated...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrownMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                    
                    // Save error
                    if (saveError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = saveError!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AccentRed,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Enhanced Content Display with Better Streaming UX
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                if (content.isNotEmpty()) {
                    // Content with enhanced typography
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = BrownDark,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.7,
                            letterSpacing = 0.2.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Streaming cursor/indicator
                    if (isStreaming) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Animated typing cursor
                            Box(
                                modifier = Modifier
                                    .size(width = 2.dp, height = 20.dp)
                                    .background(
                                        BrownMedium,
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generating content...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrownMedium.copy(alpha = 0.7f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    } else if (content.isNotEmpty()) {
                        // Course completion info
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(
                            color = CreamPrimary,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 ${content.trim().split("\\s+".toRegex()).size} words",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrownMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            
                            Text(
                                text = "⏱️ ~${(content.trim().split("\\s+".toRegex()).size / 200) + 1} min read",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrownMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Complete",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = BrownMedium,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Preparing your learning content...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BrownMedium,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}






