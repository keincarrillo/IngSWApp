package com.example.koalm.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun Logo(
    @DrawableRes logoRes: Int,
    contentDescription: String,
) {
    val isDark = isSystemInDarkTheme()
    val colorFondo = if (isDark) Color.LightGray else Color.Transparent

    Image(
        painter = painterResource(id = logoRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(200.dp) // Tamaño total
            .clip(CircleShape) // 1. Recortamos en forma de círculo (o RoundedCornerShape(16.dp))
            .background(colorFondo) // 2. Pintamos el fondo (solo se verá en modo oscuro)
            .padding(16.dp) // 3. Margen interno: Esto aleja al Koala del borde del fondo

    )
}
