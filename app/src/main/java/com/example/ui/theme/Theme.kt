package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SophisticatedGoldPrimary,
    secondary = SophisticatedGoldSecondary,
    tertiary = SophisticatedMutedGold,
    background = SophisticatedBg,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onPrimary = Color(0xFF0F0F0F), // Dark charcoal text on golden button accents
    onBackground = SophisticatedWhiteMuted,
    onSurface = SophisticatedWhiteMuted,
    outline = SophisticatedBorder,
    error = SophisticatedError
  )

private val LightColorScheme = DarkColorScheme // Standardize on immersive dark gold design

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark layout
  // Dynamic color is disabled to prevent dynamic system accents from breaking luxury gold design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
