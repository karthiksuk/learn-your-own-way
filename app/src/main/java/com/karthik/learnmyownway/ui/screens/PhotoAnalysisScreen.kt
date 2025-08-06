package com.karthik.learnmyownway.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.karthik.learnmyownway.data.models.LearningExplanation
import com.karthik.learnmyownway.data.models.PhotoAnalysis
import com.karthik.learnmyownway.ai.AIRepository
import com.karthik.learnmyownway.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoAnalysisScreen(
    imageUri: String,
    analogyStyle: String,
    onNavigateBack: () -> Unit,
    onNavigateToCourse: (String, String) -> Unit
) {
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isComplete by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val aiRepository = remember { AIRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(imageUri, analogyStyle) {
        scope.launch {
            try {
                isAnalyzing = true
                error = null
                analysisText = ""
                isComplete = false
                
                // Initialize AI first
                val initResult = aiRepository.initializeAI()
                if (initResult.isFailure) {
                    val errorMessage = initResult.exceptionOrNull()?.message ?: "Unknown error"
                    if (errorMessage.contains("Not enough memory", ignoreCase = true)) {
                        android.util.Log.i("PhotoAnalysisScreen", "Memory error detected, will use demo content")
                    } else {
                        error = "AI initialization failed: $errorMessage"
                        isAnalyzing = false
                        return@launch
                    }
                }
                
                // Start streaming immediately
                isAnalyzing = false
                
                // Use the new method that actually analyzes the uploaded image
                aiRepository.analyzeImageFromUri(imageUri, analogyStyle).collect { content ->
                    if (content.startsWith("Error:")) {
                        error = content
                    } else {
                        analysisText += content
                    }
                }
                
                isComplete = true
            } catch (e: Exception) {
                error = "Failed to analyze image: ${e.message}"
                isAnalyzing = false
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
                    text = "Photo Analysis",
                    color = BrownDark,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = BrownDark
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CreamLight
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .build()
                        ),
                        contentDescription = "Uploaded image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Analysis Status/Results
            when {
                isAnalyzing -> {
                    LoadingAnalysisCard()
                }
                error != null -> {
                    ErrorCard(error = error!!)
                }
                analysisText.isNotEmpty() -> {
                    AIAnalysisResultCard(
                        analysisText = analysisText,
                        analogyStyle = analogyStyle,
                        onCreateCourse = { 
                            // Extract a topic from the analysis for course creation
                            val topic = extractTopicFromAnalysis(analysisText)
                            onNavigateToCourse(topic, analogyStyle)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            if (!isAnalyzing && error == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrownMedium
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrownMedium)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Try Another Photo")
                    }
                    
                    if (analysisText.isNotEmpty()) {
                        Button(
                            onClick = { 
                                val topic = extractTopicFromAnalysis(analysisText)
                                onNavigateToCourse(topic, analogyStyle)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Black,
                                contentColor = White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Course")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAnalysisCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BrownMedium,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Analyzing your image...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = BrownDark,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Our AI is identifying key concepts and preparing personalized explanations",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                text = "Analysis Failed",
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

@Composable
private fun AnalysisResultCard(
    analysis: PhotoAnalysis,
    onCreateCourse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analysis Complete",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "${(analysis.confidence * 100).toInt()}% confident",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AccentGreen,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main Concept
            Text(
                text = "Main Concept: ${analysis.mainConcept}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = BrownMedium,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CreamLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Explanation (${analysis.explanation.analogyStyle} style):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = BrownDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = analysis.explanation.explanation,
                        style = MaterialTheme.typography.bodyMedium.copy(color = BrownDark),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }
            }
            
            if (analysis.explanation.keyPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Key Points:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                analysis.explanation.keyPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium)
                        )
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Processing Info
            Text(
                text = "Processed in ${analysis.processingTimeMs}ms • ${analysis.explanation.wordCount} words",
                style = MaterialTheme.typography.bodySmall.copy(color = GrayMedium)
            )
        }
    }
}

@Composable
private fun AIAnalysisResultCard(
    analysisText: String,
    analogyStyle: String,
    onCreateCourse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Text(
                text = "AI Analysis Complete",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = BrownDark,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Analysis Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CreamLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Analysis (${analogyStyle} style):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = BrownDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = analysisText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = BrownDark),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )
                }
            }
        }
    }
}

private fun extractTopicFromAnalysis(analysisText: String): String {
    // Extract meaningful educational topics from the honest image analysis
    val text = analysisText.lowercase()
    
    // Look for specific content type indicators from our honest analysis
    return when {
        text.contains("text or documents") || text.contains("textbooks") || text.contains("written content") -> "Reading and Text Analysis"
        text.contains("diagrams") || text.contains("charts") || text.contains("technical illustrations") -> "Visual Diagrams and Technical Communication"
        text.contains("high contrast") && text.contains("structured visual content") -> "Data Visualization and Graphics"
        text.contains("presentation slide") || text.contains("minimalist design") -> "Presentation Design and Visual Communication"
        text.contains("bright image") && text.contains("light background") -> "Document Analysis and Information Processing"
        text.contains("warm/reddish") -> "Color Psychology and Visual Design"
        text.contains("cool/bluish") -> "Digital Design and Technology"
        text.contains("green/natural") -> "Environmental Science and Nature Studies"
        text.contains("very light/white") -> "Typography and Document Design"
        text.contains("very dark/black") -> "Photography and Visual Contrast"
        text.contains("balanced/neutral") -> "Visual Composition and Design Principles"
        text.contains("varied colors and patterns") -> "Art and Visual Perception"
        text.contains("pixel patterns") -> "Digital Media and Computer Graphics"
        else -> {
            // Extract the most relevant educational focus based on image characteristics
            when {
                text.contains("educational material") -> "Educational Content Analysis"
                text.contains("visual properties") -> "Visual Analysis and Perception"
                text.contains("brightness") || text.contains("colors") -> "Color Theory and Visual Design"
                text.contains("patterns") -> "Pattern Recognition and Analysis"
                else -> "Visual Learning and Media Analysis"
            }
        }
    }
}