package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Gold,
    secondary = PurpleGrey40,
    tertiary = Gold,
    background = Background,
    surface = Surface,
    onPrimary = DarkBg,
    onSecondary = TextPrimary,
    onTertiary = DarkBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

private val LightColorScheme =
  darkColorScheme( // Use DarkColorScheme values for Light too, to force the Elegant Dark mode consistently!
    primary = Gold,
    secondary = PurpleGrey40,
    tertiary = Gold,
    background = Background,
    surface = Surface,
    onPrimary = DarkBg,
    onSecondary = TextPrimary,
    onTertiary = DarkBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors to preserve brand-specific Navy/Gold identity
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
