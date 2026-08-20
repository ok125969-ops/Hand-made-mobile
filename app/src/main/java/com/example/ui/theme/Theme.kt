package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MyraaColorScheme = darkColorScheme(
  primary = CyanGlow,
  onPrimary = ObsidianDark,
  primaryContainer = CyanSurface,
  onPrimaryContainer = CyanGlow,
  secondary = ElectricBlue,
  onSecondary = Color.White,
  secondaryContainer = ObsidianCard,
  onSecondaryContainer = TextPrimary,
  tertiary = NeonEmerald,
  onTertiary = ObsidianDark,
  error = NeonCoral,
  onError = Color.White,
  background = ObsidianDark,
  onBackground = TextPrimary,
  surface = ObsidianCard,
  onSurface = TextPrimary,
  surfaceVariant = ObsidianBorder,
  onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = MyraaColorScheme,
    typography = Typography,
    content = content
  )
}
