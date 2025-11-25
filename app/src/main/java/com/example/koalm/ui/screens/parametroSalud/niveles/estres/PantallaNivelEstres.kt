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
import com.example.koalm.ui.components.snapshotsAsState
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

private const val MAX_PUNTAJE_ESTRES = 21

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstres(
    navController: NavHostController
) {
    val correo = FirebaseAuth.getInstance().currentUser?.email

    val ansiedad = remember(correo) {
        Firebase.firestore.collection("resultadosAnsiedad")
            .document(correo ?: "")
    }

    val puntajesState = remember { mutableStateOf<List<Int>>(emptyList()) }
    val promedio = remember { mutableStateOf(0.0) }

    LaunchedEffect(correo) {
        if (!correo.isNullOrBlank()) {
            val puntajes = ObtenerPuntajes(correo)
            Log.d("ESTRES_DEBUG", "Puntajes obtenidos: $puntajes")
            puntajesState.value = puntajes
            promedio.value = if (puntajes.isNotEmpty()) puntajes.average() else 0.0
        } else {
            Log.w("ESTRES_DEBUG", "Correo nulo o vacío, no se puede obtener puntajes.")
        }
    }

    val promedioInt = promedio.value.toInt()
    val promedioNivel = obtenerResultadoAnsiedad(promedioInt)

    val minScoreForColors = puntajesState.value.minOrNull()
    val promedioColor = colorForAnsiedadScore(promedioInt, minScoreForColors)

    val resAnsiedad by ansiedad.snapshotsAsState { snap ->
        snap?.getString("nivel")?.toString()
    }

    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = if (isDark) colorScheme.outlineVariant else BorderColor
    val cardColor = if (isDark) colorScheme.surface else ContainerColor

    val maxPuntaje = puntajesState.value.maxOrNull()
    val mayorNivel = maxPuntaje?.let { obtenerResultadoAnsiedad(it) } ?: "-"

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SentimentNeutral,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = resAnsiedad ?: "Sin resultados",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor,
                    contentColor = colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val puntajes = puntajesState.value

                        val valores: List<Float>
                        val colores: List<Color>

                        if (puntajes.isNotEmpty()) {
                            val maxEscala = MAX_PUNTAJE_ESTRES.toFloat()
                            valores = puntajes.map { p ->
                                p.coerceIn(0, MAX_PUNTAJE_ESTRES).toFloat() / maxEscala
                            }

                            colores = puntajes.map { score ->
                                colorForAnsiedadScore(score, minScoreForColors)
                            }
                        } else {
                            valores = listOf(0f)
                            colores = listOf(colorScheme.surfaceVariant)
                        }

                        GraficaEstres(
                            valores = valores,
                            colores = colores
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 52.dp, end = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                        Text("12", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                        Text("24", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(22.dp))

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
                                        .background(promedioColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = promedioNivel,
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
                            Text(
                                text = mayorNivel,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { navController.navigate("test_de_ansiedad") },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Realizar test", fontSize = 16.sp)
            }
        }
    }
}

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
            if (pSingle != null) {
                Log.d("ESTRES_DEBUG", "Puntaje único desde doc principal: $pSingle")
                return listOf(pSingle)
            }

            val listaCruda = doc.get("puntajes") as? List<*>
            if (listaCruda != null) {
                val lista = listaCruda.mapNotNull {
                    when (it) {
                        is Long -> it.toInt()
                        is Int -> it
                        else -> null
                    }
                }
                if (lista.isNotEmpty()) {
                    Log.d("ESTRES_DEBUG", "Puntajes lista desde doc principal: $lista")
                    return lista
                }
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
        in 0..4 -> "Ansiedad Mínima"
        in 5..9 -> "Ansiedad Leve"
        in 10..14 -> "Ansiedad Moderada"
        else -> "Ansiedad Severa"
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
