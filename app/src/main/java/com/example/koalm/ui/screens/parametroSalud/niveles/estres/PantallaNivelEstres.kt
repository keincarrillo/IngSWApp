package com.example.koalm.ui.screens.parametroSalud.niveles.estres

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.GraficaEstres
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.example.koalm.ui.theme.PrimaryColor
import com.example.koalm.ui.theme.SuccessColor
import com.example.koalm.ui.theme.WarningColor
import com.example.koalm.ui.theme.AlertColor
import com.example.koalm.ui.theme.CriticalColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Máximo teórico del test
private const val MAX_PUNTAJE_ESTRES = 21

// ------------------------- UI STATE -------------------------
private sealed class EstresUiState {
    object Loading : EstresUiState()
    data class NoData(val message: String, val nivelGuardado: String?) : EstresUiState()
    data class Error(val message: String) : EstresUiState()
    data class Data(
        val puntajes: List<Int>,
        val promedio: Double,
        val nivelPromedio: String,
        val mayorNivel: String,
        val nivelGuardado: String?
    ) : EstresUiState()
}

// ------------------------- PANTALLA -------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstres(
    navController: NavHostController
) {
    val correo = FirebaseAuth.getInstance().currentUser?.email
    val uiState = remember { mutableStateOf<EstresUiState>(EstresUiState.Loading) }

    // Cargar datos UNA sola vez para esta pantalla
    LaunchedEffect(correo) {
        if (correo.isNullOrBlank()) {
            uiState.value = EstresUiState.NoData(
                message = "Inicia sesión para ver tu nivel de estrés.",
                nivelGuardado = null
            )
            return@LaunchedEffect
        }

        try {
            val firestore = Firebase.firestore

            // 1) Puntajes (historial o doc principal)
            val puntajes = ObtenerPuntajes(correo)
            Log.d("ESTRES_DEBUG", "Puntajes obtenidos: $puntajes")

            // 2) Info general del doc principal (por ejemplo, campo "nivel")
            val doc = firestore
                .collection("resultadosAnsiedad")
                .document(correo)
                .get()
                .await()

            val nivelGuardado = if (doc.exists()) doc.getString("nivel") else null

            if (puntajes.isEmpty()) {
                uiState.value = EstresUiState.NoData(
                    message = "Aún no hay resultados registrados.\nRealiza el test para ver tu nivel de estrés.",
                    nivelGuardado = nivelGuardado
                )
            } else {
                val promedio = puntajes.average()
                val promedioInt = promedio.toInt()
                val nivelPromedio = obtenerResultadoAnsiedad(promedioInt)

                val maxPuntaje = puntajes.maxOrNull()
                val mayorNivel = if (maxPuntaje != null) {
                    obtenerResultadoAnsiedad(maxPuntaje)
                } else {
                    ""
                }

                uiState.value = EstresUiState.Data(
                    puntajes = puntajes,
                    promedio = promedio,
                    nivelPromedio = nivelPromedio,
                    mayorNivel = mayorNivel,
                    nivelGuardado = nivelGuardado
                )
            }
        } catch (e: Exception) {
            Log.e("ESTRES_DEBUG", "Error al cargar datos de estrés", e)
            uiState.value = EstresUiState.Error(
                "Ocurrió un error al cargar tus datos.\nInténtalo de nuevo más tarde."
            )
        }
    }

    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = if (isDark) colorScheme.outlineVariant else BorderColor
    val cardColor = if (isDark) colorScheme.surface else ContainerColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nivel de estrés",
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
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
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Encabezado: texto principal con el "nivel" si lo tenemos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SentimentNeutral,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                val titulo = when (val state = uiState.value) {
                    is EstresUiState.Data ->
                        state.nivelGuardado ?: state.nivelPromedio
                    is EstresUiState.NoData ->
                        state.nivelGuardado ?: "Sin resultados"
                    is EstresUiState.Error ->
                        "Error"
                    EstresUiState.Loading ->
                        "Cargando..."
                }

                Text(
                    text = titulo,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState.value) {
                EstresUiState.Loading -> {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                is EstresUiState.Error -> {
                    TextoMensajePrincipal(
                        mensaje = state.message,
                        colorScheme = colorScheme
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BotonIrAlTest(navController)
                }

                is EstresUiState.NoData -> {
                    TextoMensajePrincipal(
                        mensaje = state.message,
                        colorScheme = colorScheme
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BotonIrAlTest(navController)
                }

                is EstresUiState.Data -> {
                    // ======== TARJETA CON GRÁFICA Y RESÚMENES (SOLO SI HAY PUNTAJES) ========
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor,
                            contentColor = colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            // ---------- GRÁFICA ----------
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                val puntajes = state.puntajes
                                val maxEscala = MAX_PUNTAJE_ESTRES.toFloat()
                                val minScore = puntajes.minOrNull()

                                // Normalizamos 0..MAX_PUNTAJE_ESTRES -> 0f..1f
                                val valores: List<Float> = puntajes.map { p ->
                                    p.coerceIn(0, MAX_PUNTAJE_ESTRES)
                                        .toFloat() / maxEscala
                                }

                                val colores: List<Color> = puntajes.map { score ->
                                    colorForAnsiedadScore(score, minScore)
                                }

                                // Aquí sólo llegamos si puntajes NO está vacío
                                GraficaEstres(
                                    valores = valores,
                                    colores = colores
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // ---------- MARCAS DEL EJE ----------
                            val lowTick = 0
                            val midTick = MAX_PUNTAJE_ESTRES / 2
                            val highTick = MAX_PUNTAJE_ESTRES

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 52.dp, end = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    lowTick.toString(),
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    midTick.toString(),
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    highTick.toString(),
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            // ---------- RESUMENES ----------
                            val minScore = state.puntajes.minOrNull()
                            val colorPromedio = colorForAnsiedadScore(
                                score = state.promedio.toInt(),
                                minScore = minScore
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column {
                                    Text(
                                        "Promedio de estrés",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(colorPromedio, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = state.nivelPromedio,
                                            color = colorScheme.onSurface
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        "Mayor estrés",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (state.mayorNivel.isNotEmpty()) {
                                        Text(
                                            text = state.mayorNivel,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ---------- LEYENDA DE COLORES ----------
                            Text(
                                text = "Leyenda de niveles:",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LeyendaColor("Mínima", SuccessColor)
                            LeyendaColor("Leve", WarningColor)
                            LeyendaColor("Moderada", AlertColor)
                            LeyendaColor("Severa", CriticalColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    BotonIrAlTest(navController)
                }
            }
        }
    }
}

// ------------------------- SUBCOMPONENTES -------------------------
@Composable
private fun TextoMensajePrincipal(
    mensaje: String,
    colorScheme: ColorScheme
) {
    Text(
        text = mensaje,
        color = colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun BotonIrAlTest(navController: NavHostController) {
    Button(
        onClick = { navController.navigate("test_de_ansiedad") },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .height(40.dp)
            .width(170.dp)
    ) {
        Text("Realizar test", fontSize = 16.sp)
    }
}

@Composable
private fun LeyendaColor(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(12.dp))
    }
}

// ------------------------- LÓGICA FIRESTORE -------------------------
suspend fun ObtenerPuntajes(userEmail: String): List<Int> {
    val firestore = Firebase.firestore
    return try {
        val historialRef = firestore
            .collection("resultadosAnsiedad")
            .document(userEmail)
            .collection("historial")

        val snapshot = historialRef.get().await()
        Log.d("ESTRES_DEBUG", "Historial size=${snapshot.size()} para $userEmail")

        val desdeHistorial = snapshot.documents.mapNotNull { it.getLong("puntaje")?.toInt() }

        if (desdeHistorial.isNotEmpty()) {
            Log.d("ESTRES_DEBUG", "Puntajes desde historial: $desdeHistorial")
            return desdeHistorial
        }

        val doc = firestore
            .collection("resultadosAnsiedad")
            .document(userEmail)
            .get()
            .await()

        if (doc.exists()) {
            val pSingle = doc.getLong("puntaje")?.toInt()
            if (pSingle != null) return listOf(pSingle)

            val listaCruda = doc.get("puntajes") as? List<*>
            if (listaCruda != null) {
                val lista = listaCruda.mapNotNull {
                    when (it) {
                        is Long -> it.toInt()
                        is Int -> it
                        else -> null
                    }
                }
                if (lista.isNotEmpty()) return lista
            }
        }

        Log.w("ESTRES_DEBUG", "No se encontraron puntajes para $userEmail")
        emptyList()
    } catch (e: Exception) {
        Log.e("Firebase", "Error al obtener puntuación", e)
        emptyList()
    }
}

fun obtenerResultadoAnsiedad(puntaje: Int): String {
    return when (puntaje) {
        in 0..4 -> "Ansiedad mínima"
        in 5..9 -> "Ansiedad leve"
        in 10..14 -> "Ansiedad moderada"
        else -> "Ansiedad severa"
    }
}

fun colorForAnsiedadScore(score: Int, minScore: Int?): Color {
    val effectiveMin = minScore ?: 0
    val clampedScore = score.coerceIn(effectiveMin, MAX_PUNTAJE_ESTRES)
    val range = (MAX_PUNTAJE_ESTRES - effectiveMin).coerceAtLeast(1)
    val ratio = (clampedScore - effectiveMin).toFloat() / range

    return when {
        ratio <= 0.25f -> SuccessColor
        ratio <= 0.5f  -> WarningColor
        ratio <= 0.75f -> AlertColor
        else           -> CriticalColor
    }
}
