package com.karthik.learnmyownway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.karthik.learnmyownway.navigation.AppNavigation
import com.karthik.learnmyownway.ui.theme.CreamLight
import com.karthik.learnmyownway.ui.theme.LearnMyOwnWayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnMyOwnWayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CreamLight
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }

        }
    }
}