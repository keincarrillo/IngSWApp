package com.example.koalm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.dp
import com.example.koalm.ui.theme.*

@Composable
fun BarraNavegacionInferior(
    navController: NavHostController,
    rutaActual: String
) {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colorScheme.surface,          // Se adapta a claro/oscuro
        contentColor = colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        listOf(
            Triple("Inicio", Icons.Default.Home, "menu"),
            Triple("Hábitos", Icons.AutoMirrored.Filled.List, "tipos_habitos"),
            Triple("Salud", Icons.Default.Favorite, "estadisticas"),
            Triple("Perfil", Icons.Default.Person, "personalizar")
        ).forEach { (label, icon, route) ->
            val selected = rutaActual == route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (rutaActual != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected)
                            PrimaryColor
                        else
                            colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (selected)
                            PrimaryColor
                        else
                            colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.7f),
                    indicatorColor = PrimaryColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}
