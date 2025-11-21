/* PantallaNosotros.kt */
package com.example.koalm.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNosotros(navController: NavHostController) {

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    val cardBackground = if (isDark) {
        colorScheme.surface
    } else {
        ContainerColor
    }

    val cardBorderColor = if (isDark) {
        colorScheme.outlineVariant
    } else {
        BorderColor
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Acerca de Nosotros",
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "tipos_habitos"
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorderColor),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackground
                )
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("PinguBalance es una aplicación desarrollada como proyecto integrador de la materia de Ingeniería de Software. Nuestro objetivo es aplicar de forma práctica los conceptos vistos en clase para construir una solución real que apoye el bienestar de las personas.\n\n")
                            append("A lo largo del desarrollo seguimos las etapas del proceso de software: levantamiento y análisis de requisitos, modelado, diseño de arquitectura, implementación, pruebas y documentación. Utilizamos buenas prácticas como control de versiones, trabajo colaborativo y diseño centrado en el usuario.\n\n")
                            append("PinguBalance busca ayudarte a llevar un control equilibrado de tus hábitos físicos y de salud mental: sueño, alimentación, hidratación, actividad, emociones y metas personales. Cada pantalla está pensada para que la experiencia sea clara, amigable y motivadora.\n\n")
                            append("Como estudiantes de Ingeniería en Sistemas Computacionales, creemos que la Ingeniería de Software cobra sentido cuando se traduce en herramientas que mejoran la vida diaria. PinguBalance es nuestro primer paso para unir la teoría con una aplicación que acompaña a los usuarios en su camino hacia hábitos más sanos.")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            ListaIntegrantesNosotros()

            Image(
                painter = painterResource(id = R.drawable.pinguino_feliz),
                contentDescription = "Equipo Koalm",
                modifier = Modifier.size(300.dp)
            )
        }
    }
}

@Composable
fun ListaIntegrantesNosotros() {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Integrantes",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("• Ayala Fuentes Sunem Gizeth", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Carrillo Rodriguez Kein Isaac", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Crisostomo Aguilar Ricardo", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Flores Lopez Agustin", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Gasca Fragoso Pedro", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Melo Jimenez Jesus Uriel", color = colorScheme.onSurface, fontSize = 14.sp)
                Text("• Najera Ramirez Sharon Leonardo", color = colorScheme.onSurface, fontSize = 14.sp)
            }
        }
    }
}
