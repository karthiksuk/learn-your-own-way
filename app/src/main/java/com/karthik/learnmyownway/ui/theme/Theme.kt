package com.karthik.learnmyownway.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrownMedium,
    secondary = BeigeWarm,
    tertiary = CreamPrimary,
    background = BrownDark,
    surface = BrownMedium,
    onPrimary = White,
    onSecondary = White,
    onTertiary = BrownDark,
    onBackground = CreamLight,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = BrownMedium,
    secondary = BeigeWarm,
    tertiary = CreamPrimary,
    background = CreamLight,
    surface = White,
    onPrimary = White,
    onSecondary = BrownDark,
    onTertiary = BrownDark,
    onBackground = BrownDark,
    onSurface = BrownDark
)

@Composable
fun LearnMyOwnWayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}