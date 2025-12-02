package com.example.koalm.ui.screens.parametroSalud

/* ----------  IMPORTS  ---------- */
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.services.obtenerMinutosLocales
import com.example.koalm.data.StepCounterRepository
import com.example.koalm.ui.components.snapshotsAsState
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.Logo
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import kotlinx.coroutines.tasks.await

/* ----------  UI PRINCIPAL  ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametrosSalud(
    navController: NavHostController
) {

    val context = LocalContext.current
    val correo = FirebaseAuth.getInstance().currentUser?.email

    val ansiedad = remember(correo) {
        Firebase.firestore.collection("resultadosAnsiedad")
            .document(correo ?: "")
    }
    val metas = remember(correo) {
        Firebase.firestore.collection("usuarios")
            .document(correo ?: "")
            .collection("metasSalud")
            .document("valores")
    }
    val peso = remember { mutableStateOf(0f) }

    // Recuperar el peso al iniciar la pantalla
    LaunchedEffect(Unit) {
        peso.value = recuperarPesoFirestore()
    }

    val metaPasos by metas.snapshotsAsState { it?.getLong("metaPasos")?.toInt() ?: 10000 }
    val metaMinutos by metas.snapshotsAsState { it?.getLong("metaMinutos")?.toInt() ?: 100 }
    val metaCalorias by metas.snapshotsAsState { it?.getLong("metaCalorias")?.toInt() ?: 500 }
    val ResAnsiedad by ansiedad.snapshotsAsState { it?.getString("nivel")?.toString() }

    val pasos by StepCounterRepository.steps.collectAsState()
    val calorias = (pasos * peso.value * 0.0007).toInt()
    val minutos = remember { obtenerMinutosLocales(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parámetros de salud") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "estadisticas"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🐧 MISMO COMPORTAMIENTO QUE EN PANTALLA INICIAR SESIÓN:
                // Logo se encarga del fondo circular gris en modo oscuro, sin fondo en claro
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(y = (-10).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Logo(
                        logoRes = R.drawable.pinguino_training,
                        contentDescription = "Koala salud"
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloYBarra("Pasos", pasos.toFloat() / metaPasos.coerceAtLeast(1))
                    TituloYBarra("Tiempo Activo", minutos.toFloat() / metaMinutos.coerceAtLeast(1))
                    TituloYBarra("Calorías", calorias.toFloat() / metaCalorias.coerceAtLeast(1))
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                InfoMiniCard("Pasos", "$pasos/$metaPasos", Icons.AutoMirrored.Filled.DirectionsWalk)
                InfoMiniCard("Tiempo Activo", "$minutos/$metaMinutos min", Icons.Default.AccessTime)
                InfoMiniCard("Calorías", "$calorias kcal/$metaCalorias kcal", Icons.Default.LocalFireDepartment)
            }

            // 🔻 Eliminadas las cards de "Sueño" y "Ritmo Cardíaco"

            InfoCard("Estrés", "${ResAnsiedad}", Icons.Default.PsychologyAlt, 0.6f) {
                navController.navigate("nivel-de-estres")
            }
            InfoCard("Peso", "${peso.value} Kg", Icons.Default.MonitorWeight, 0.5f) {
                navController.navigate("control-peso")
            }
            InfoCard("Actividad diaria", "Completada", Icons.AutoMirrored.Filled.DirectionsRun) {
                navController.navigate("actividad-diaria")
            }

            Spacer(Modifier.height(70.dp))
        }
    }
}

/* ----------  BARRA CON TÍTULO ---------- */
@Composable
fun TituloYBarra(titulo: String, progreso: Float) {
    val isDark = isSystemInDarkTheme()
    val trackColor = if (isDark) Color.DarkGray else TertiaryColor

    Column {
        Text(
            titulo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        LinearProgressIndicator(
            progress = { progreso.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (progreso > 0f) PrimaryColor else trackColor,
            trackColor = trackColor
        )
    }
}

/* ----------  COMPONENTES AUXILIARES  ---------- */
@Composable
fun InfoMiniCard(titulo: String, dato: String, icono: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(2.dp))
            Text(titulo, style = MaterialTheme.typography.labelSmall)
        }
        Text(dato, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun InfoCard(
    titulo: String,
    dato: String,
    icono: ImageVector,
    progreso: Float? = null,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // 🔹 Mismos criterios visuales que HabitoCategoriaCard (PantallaHabitos)
    val borderColor = if (isDark) colorScheme.outlineVariant else BorderColor
    val cardColor = if (isDark) colorScheme.surface else ContainerColor
    val titleColor = colorScheme.onSurface
    val bodyColor = TertiaryMediumColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        color = cardColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
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
                        contentDescription = titulo,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    style = MaterialTheme.typography.titleMedium
                )
                if (dato.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        dato,
                        color = bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

suspend fun recuperarPesoFirestore(): Float {
    val correo = FirebaseAuth.getInstance().currentUser?.email
    val firestore = Firebase.firestore

    return try {
        if (correo != null) {
            val snapshot = firestore.collection("usuarios")
                .document(correo)
                .get()
                .await()

            val peso = snapshot.getDouble("peso")?.toFloat() ?: 0f
            Log.d("DEBUG_PESO", "Peso recuperado: $peso")
            peso
        } else {
            0f
        }
    } catch (e: Exception) {
        Log.e("DEBUG_PESO", "Error al recuperar peso", e)
        0f
    }
}
