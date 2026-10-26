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
    primary = PharmacyGreenPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = PharmacyGreenDark,
    onPrimaryContainer = PharmacyGreenLight,
    secondary = MedicalTealSecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = MedicalTealDark,
    onSecondaryContainer = MedicalTealLight,
    tertiary = AlertAmber,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PharmacyGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = PharmacyGreenLight,
    onPrimaryContainer = PharmacyGreenDark,
    secondary = MedicalTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedicalTealLight,
    onSecondaryContainer = MedicalTealDark,
    tertiary = AlertAmber,
    background = SoftBackground,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = AlertRed,
    errorContainer = AlertRedLight,
    onError = Color.White,
    onErrorContainer = AlertRed
  )

@Composable
fun MAKPharmaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep true brand healthcare green colors by default
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
