package com.karthik.learnmyownway.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.karthik.learnmyownway.ui.screens.CourseScreen
import com.karthik.learnmyownway.ui.screens.InputScreen
import com.karthik.learnmyownway.ui.screens.PhotoAnalysisScreen

sealed class Screen(val route: String) {
    object Input : Screen("input")
    object PhotoAnalysis : Screen("photo_analysis/{imageUri}/{analogyStyle}") {
        fun createRoute(imageUri: String, analogyStyle: String) = 
            "photo_analysis/$imageUri/$analogyStyle"
    }
    object Course : Screen("course/{topic}/{analogyStyle}") {
        fun createRoute(topic: String, analogyStyle: String) = 
            "course/$topic/$analogyStyle"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Input.route
    ) {
        composable(Screen.Input.route) {
            InputScreen(
                onNavigateToPhotoAnalysis = { imageUri, analogyStyle ->
                    val encodedUri = java.net.URLEncoder.encode(imageUri, "UTF-8")
                    val encodedStyle = java.net.URLEncoder.encode(analogyStyle, "UTF-8")
                    navController.navigate(Screen.PhotoAnalysis.createRoute(encodedUri, encodedStyle))
                },
                onNavigateToCourse = { topic, analogyStyle ->
                    val encodedTopic = java.net.URLEncoder.encode(topic, "UTF-8")
                    val encodedStyle = java.net.URLEncoder.encode(analogyStyle, "UTF-8")
                    navController.navigate(Screen.Course.createRoute(encodedTopic, encodedStyle))
                }
            )
        }
        
        composable(Screen.PhotoAnalysis.route) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val analogyStyle = backStackEntry.arguments?.getString("analogyStyle") ?: ""
            
            val decodedUri = java.net.URLDecoder.decode(imageUri, "UTF-8")
            val decodedStyle = java.net.URLDecoder.decode(analogyStyle, "UTF-8")
            
            PhotoAnalysisScreen(
                imageUri = decodedUri,
                analogyStyle = decodedStyle,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCourse = { topic, style ->
                    val encodedTopic = java.net.URLEncoder.encode(topic, "UTF-8")
                    val encodedStyleParam = java.net.URLEncoder.encode(style, "UTF-8")
                    navController.navigate(Screen.Course.createRoute(encodedTopic, encodedStyleParam))
                }
            )
        }
        
        composable(Screen.Course.route) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: ""
            val analogyStyle = backStackEntry.arguments?.getString("analogyStyle") ?: ""
            
            val decodedTopic = java.net.URLDecoder.decode(topic, "UTF-8")
            val decodedStyle = java.net.URLDecoder.decode(analogyStyle, "UTF-8")
            
            CourseScreen(
                topic = decodedTopic,
                analogyStyle = decodedStyle,
                onNavigateHome = { 
                    navController.navigate(Screen.Input.route) {
                        popUpTo(Screen.Input.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}