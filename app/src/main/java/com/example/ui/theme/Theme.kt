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
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF082B66),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFE3EDFA),
    secondary = SatpolGold,
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = Color(0xFF452B04),
    onSecondaryContainer = SatpolGoldLight,
    tertiary = SatpolGreen,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = Color(0xFFF87171)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SatpolBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SatpolBlueLight,
    onPrimaryContainer = SatpolBlueDark,
    secondary = SatpolGold,
    onSecondary = Color.White,
    secondaryContainer = SatpolGoldLight,
    onSecondaryContainer = SatpolGoldDark,
    tertiary = SatpolGreen,
    background = SurfaceLight,
    surface = Color.White,
    onBackground = Slate800,
    onSurface = Slate800,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Slate600,
    outline = Slate400,
    error = SatpolRedAlert
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep distinctive Satpol PP blue/gold styling consistent
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
