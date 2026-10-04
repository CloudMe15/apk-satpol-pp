package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun satpolTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF000000),       // Jet Black 100% solid
    unfocusedTextColor = Color(0xFF000000),     // Jet Black 100% solid
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Color(0xFF082B66),
    focusedBorderColor = Color(0xFF082B66),     // Solid Deep Navy Blue
    unfocusedBorderColor = Color(0xFF64748B),   // Solid Dark Slate 500
    focusedLabelColor = Color(0xFF082B66),      // Solid Deep Navy Blue
    unfocusedLabelColor = Color(0xFF1E293B),    // Solid Dark Charcoal
    focusedPlaceholderColor = Color(0xFF64748B),
    unfocusedPlaceholderColor = Color(0xFF64748B),
    disabledTextColor = Color(0xFF334155),
    errorTextColor = Color(0xFFDC2626)
)

private val DarkColorScheme =
  darkColorScheme(
    primary = SatpolBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SatpolBlueLight,
    onPrimaryContainer = SatpolBlueDark,
    secondary = SatpolGold,
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = Color(0xFF452B04),
    onSecondaryContainer = SatpolGoldLight,
    tertiary = SatpolGreen,
    background = Color(0xFF0F172A),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF000000), // Pure Black text
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
  darkTheme: Boolean = false,
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
