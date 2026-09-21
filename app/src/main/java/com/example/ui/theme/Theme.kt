package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = ChampagneGold,
  onPrimary = ObsidianBlack,
  primaryContainer = ChampagneGoldDark,
  onPrimaryContainer = ChampagneGoldLight,
  secondary = HauteTextSecondary,
  onSecondary = ObsidianBlack,
  secondaryContainer = CharcoalElevated,
  onSecondaryContainer = HauteTextPrimary,
  tertiary = AccentEmerald,
  onTertiary = Color.White,
  background = ObsidianBlack,
  onBackground = HauteTextPrimary,
  surface = CharcoalSurface,
  onSurface = HauteTextPrimary,
  surfaceVariant = CharcoalElevated,
  onSurfaceVariant = HauteTextSecondary,
  outline = CharcoalBorder,
  outlineVariant = CharcoalBorder.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
  primary = ChampagneGoldDark,
  onPrimary = Color.White,
  primaryContainer = ChampagneGoldLight,
  onPrimaryContainer = ObsidianBlack,
  secondary = LightTextSecondary,
  onSecondary = Color.White,
  secondaryContainer = EditorialIvoryElevated,
  onSecondaryContainer = LightTextPrimary,
  tertiary = AccentEmerald,
  onTertiary = Color.White,
  background = EditorialIvory,
  onBackground = LightTextPrimary,
  surface = EditorialIvorySurface,
  onSurface = LightTextPrimary,
  surfaceVariant = EditorialIvoryElevated,
  onSurfaceVariant = LightTextSecondary,
  outline = EditorialIvoryBorder,
  outlineVariant = EditorialIvoryBorder.copy(alpha = 0.5f)
)

@Composable
fun FashionEngineTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

