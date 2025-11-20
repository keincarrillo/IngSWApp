package com.example.koalm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    tertiary = PrimaryLightColor,
    onTertiary = Color.Black,
    background = White,
    onBackground = Color.Black,
    surface = White,
    onSurface = Color.Black,
    surfaceVariant = TertiaryColor,
    onSurfaceVariant = Color.Black,
    surfaceTint = PrimaryColor
)

// Paleta oscura
// Paleta oscura
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    tertiary = PrimaryLightColor,
    onTertiary = Color.Black,

    background = DarkBackgroundColor,
    onBackground = Color.White,

    surface = DarkSurfaceColor,
    onSurface = Color.White,

    surfaceVariant = TertiaryColor,
    onSurfaceVariant = Color.White,
    surfaceTint = PrimaryColor
)


@Composable
fun KoalmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta si el sistema está en oscuro
    content: @Composable () -> Unit
) {
    // Elige la paleta automáticamente según el sistema
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme, // Aquí usas la variable dinámica
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}



