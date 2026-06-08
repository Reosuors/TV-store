package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TVStudioColorScheme = darkColorScheme(
    primary = GreenNeon,
    onPrimary = Color.Black,
    primaryContainer = GreenDarkSurface,
    onPrimaryContainer = GreenNeon,
    
    secondary = YellowFlash,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2C2E1B),
    onSecondaryContainer = YellowFlash,
    
    tertiary = GreenNeonAccent,
    onTertiary = Color.Black,
    
    background = DarkBackground,
    onBackground = TextPrimaryGreen,
    
    surface = DarkSurface,
    onSurface = TextPrimaryGreen,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryGreen,
    
    outline = GreenNeonAccent
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TVStudioColorScheme,
        typography = Typography,
        content = content
    )
}
