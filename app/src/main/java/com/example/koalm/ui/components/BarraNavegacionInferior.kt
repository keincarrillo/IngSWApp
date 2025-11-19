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
    NavigationBar(
        containerColor = TertiaryColor,
        contentColor = TertiaryMediumColor,
        tonalElevation = 8.dp
    ) {
        listOf(
            Triple("Inicio", Icons.Default.Home, "menu"),
            Triple("Hábitos", Icons.AutoMirrored.Filled.List, "tipos_habitos"),
            Triple("Salud", Icons.Default.Favorite, "estadisticas"),
            Triple("Perfil", Icons.Default.Person, "personalizar")
        ).forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = rutaActual == route,
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
                        tint = if (rutaActual == route) PrimaryColor else TertiaryMediumColor
                    )
                },
                label = { 
                    Text(
                        text = label,
                        color = if (rutaActual == route) PrimaryColor else TertiaryMediumColor
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = TertiaryMediumColor,
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = TertiaryMediumColor,
                    indicatorColor = PrimaryColor.copy(alpha = 0.1f)
                )
            )
        }
    }
} 