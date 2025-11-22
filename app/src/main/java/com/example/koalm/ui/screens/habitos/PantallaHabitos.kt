package com.example.koalm.ui.screens.habitos
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.components.BarraNavegacionInferior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHabitos(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tipos de hábitos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "tipos_habitos"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HabitoCategoriaCard(
                titulo = "Salud física",
                descripcion = "Control de sueño, alimentación e hidratación.",
                icono = Icons.Default.FitnessCenter,
                onClick = { navController.navigate("salud_fisica") }
            )
            
            HabitoCategoriaCard(
                titulo = "Salud mental",
                descripcion = "Control de meditación, lectura, desconexión digital y escritura.",
                icono = Icons.Default.Psychology,
                onClick = { navController.navigate("salud_mental") }
            )
            
            HabitoCategoriaCard(
                titulo = "Personalizado",
                descripcion = "Crea y administra tus propias ideas para mejorar tu vida.",
                icono = Icons.Default.Edit,
                onClick = { navController.navigate("gestion_habitos_personalizados") }
            )
        }
    }
}

@Composable
fun HabitoCategoriaCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // mismos criterios que en PantallaSaludFisica
    val borderColor = if (isDark) colorScheme.outlineVariant else BorderColor
    val cardColor = if (isDark) colorScheme.surface else ContainerColor

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    // igual que en las cards de Salud Física
                    color = TertiaryMediumColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
