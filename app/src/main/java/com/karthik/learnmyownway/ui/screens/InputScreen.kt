package com.karthik.learnmyownway.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.learnmyownway.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onNavigateToPhotoAnalysis: (String, String) -> Unit,
    onNavigateToCourse: (String, String) -> Unit
) {
    var topicInput by remember { mutableStateOf("") }
    var styleInput by remember { mutableStateOf("") }
    var isTopicFocused by remember { mutableStateOf(false) }
    var isStyleFocused by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (styleInput.isNotBlank()) {
                onNavigateToPhotoAnalysis(it.toString(), styleInput)
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && styleInput.isNotBlank()) {
            // Handle camera capture result
            // For now, we'll use a placeholder
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamLight)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = "Learn Your Own Way",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = BrownDark
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Transform complex concepts into easy-to-understand explanations",
            style = MaterialTheme.typography.bodyLarge.copy(color = BrownMedium),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Interactive Sentence Card
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
                    text = "I want to learn about",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Topic Input
                InteractiveInput(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    placeholder = "quantum physics",
                    isFocused = isTopicFocused,
                    onFocusChange = { isTopicFocused = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "but explain it in",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Style Input
                InteractiveInput(
                    value = styleInput,
                    onValueChange = { styleInput = it },
                    placeholder = "chef",
                    isFocused = isStyleFocused,
                    onFocusChange = { isStyleFocused = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "terms",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Create Button
                Button(
                    onClick = {
                        if (topicInput.isNotBlank() && styleInput.isNotBlank()) {
                            onNavigateToCourse(topicInput.trim(), styleInput.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Black,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = topicInput.isNotBlank() && styleInput.isNotBlank()
                ) {
                    Text(
                        text = "Create Course",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f), color = BeigeWarm)
            Text(
                text = "  OR  ",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Divider(modifier = Modifier.weight(1f), color = BeigeWarm)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Photo Section
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
                    text = "📸",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Learn from a Photo",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = BrownDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Upload an image and get AI-powered explanations",
                    style = MaterialTheme.typography.bodyMedium.copy(color = BrownMedium),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrownMedium
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrownMedium)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = styleInput.isNotBlank()
                    ) {
                        Text("Select Photo")
                    }
                    
                    OutlinedButton(
                        onClick = { /* Handle camera capture */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrownMedium
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrownMedium)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = styleInput.isNotBlank()
                    ) {
                        Text("Take Photo")
                    }
                }
                
                if (styleInput.isBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please specify explanation style first",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentOrange
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InteractiveInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) CreamPrimary.copy(alpha = 0.3f)
                else GrayLight
            )
            .clickable { onFocusChange(true) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = GrayMedium,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = BrownDark,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}