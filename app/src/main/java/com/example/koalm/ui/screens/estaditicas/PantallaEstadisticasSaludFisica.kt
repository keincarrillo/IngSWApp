package com.example.koalm.ui.screens.estaditicas

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.koalm.ui.theme.TertiaryDarkColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Habito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.ContainerColor
import com.example.koalm.ui.theme.DarkSurfaceColor
import com.example.koalm.ui.theme.PrimaryColor
import com.example.koalm.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticasSaludFisica(
    navController: NavHostController
) {
    val progresoPorHabito =
        remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val db = FirebaseFirestore.getInstance()
    val habitoRepository = remember { HabitoRepository() }
    val habitosFisicos = remember { mutableStateListOf<Habito>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val contexto = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    var isLoading by remember { mutableStateOf(true) }

    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // ---------- CARGA DE DATOS ----------
    LaunchedEffect(userEmail) {
        if (userEmail == null) return@LaunchedEffect

        try {
            Log.d("Graficador", "Iniciando carga de hábitos físicos")

            val resultado = habitoRepository.obtenerHabitosFisicosKary(userEmail)
            if (resultado.isSuccess) {
                val listaHabitos = resultado.getOrNull().orEmpty()
                habitosFisicos.clear()
                habitosFisicos.addAll(listaHabitos)

                Log.d("Graficador", "Hábitos físicos cargados (${listaHabitos.size}):")
                listaHabitos.forEach {
                    Log.d("Graficador", " - ${it.titulo} (${it.id})")
                }

                progresoPorHabito.clear()

                listaHabitos.forEach { habito ->
                    val progresoSnapshot = db.collection("habitos")
                        .document(userEmail)
                        .collection("predeterminados")
                        .document(habito.id)
                        .collection("progreso")
                        .get()
                        .await()

                    val progresoMap = progresoSnapshot.documents.mapNotNull { doc ->
                        val fechaStr = doc.id
                        val progreso = doc.toObject(ProgresoDiario::class.java)
                        if (progreso != null && fechaStr.isNotBlank()) {
                            try {
                                val fecha = LocalDate.parse(fechaStr)
                                Log.d(
                                    "Graficador",
                                    "Progreso cargado para ${habito.titulo} en $fechaStr"
                                )
                                fecha to progreso
                            } catch (e: Exception) {
                                Log.e("Graficador", "Error al parsear fecha: $fechaStr", e)
                                null
                            }
                        } else {
                            null
                        }
                    }.toMap()

                    progresoPorHabito[habito.titulo] = progresoMap
                }
            } else {
                Log.e(
                    "Graficador",
                    "Error cargando hábitos físicos: ${resultado.exceptionOrNull()?.message}"
                )
            }

        } catch (e: Exception) {
            Log.e("Graficador", "Error inesperado al cargar hábitos físicos: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    if (habitosFisicos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No hay hábitos físicos disponibles",
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val habitoActual = habitosFisicos[selectedIndex.value.coerceIn(habitosFisicos.indices)]
    val progresoActual = progresoPorHabito[habitoActual.titulo] ?: emptyMap()

    val colorHabito = if (isDark) DarkSurfaceColor else PrimaryColor

    Log.d("Graficador", "progresoActual size: ${progresoActual.size}")

    // ---------- UI ----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos físicos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("menu") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { paddingValues ->
        var semanaVisible by remember {
            mutableStateOf(LocalDate.now().with(DayOfWeek.MONDAY))
        }

        val inicioSemana = semanaVisible.with(DayOfWeek.MONDAY)
        val finSemana = inicioSemana.plusDays(6)

        val progresoSemanaActual = progresoActual.filterKeys { fecha ->
            fecha in inicioSemana..finSemana
        }

        val diasSemana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

        fun frecuenciaParaDia(fecha: LocalDate): List<Boolean>? {
            progresoSemanaActual[fecha]?.frecuencia?.let { return it }

            val fechasAnteriores = progresoActual.keys
                .filter { it < fecha }
                .sortedDescending()

            for (fechaAnterior in fechasAnteriores) {
                progresoActual[fechaAnterior]?.frecuencia?.let { return it }
            }
            return habitoActual.diasSeleccionados
        }

        val diasPlaneados = diasSemana.count { dia ->
            val frecuencia = frecuenciaParaDia(dia)
            if (frecuencia != null) {
                val diaSemanaIndex = (dia.dayOfWeek.value + 6) % 7
                frecuencia.getOrNull(diaSemanaIndex) == true
            } else false
        }

        val diasRegistrados = progresoSemanaActual.count { it.value.completado }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ---------- RACHAS ----------
            val rachaActual = habitoActual.rachaActual
            val rachaMaxima = habitoActual.rachaMaxima

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IndicadorCircular("Racha actual", rachaActual, rachaMaxima)
                IndicadorCircular("Racha máxima", rachaMaxima, rachaMaxima)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- PINGÜINO + SELECTOR ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDarkTheme = isSystemInDarkTheme()

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDarkTheme) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                        Image(
                            painter = painterResource(id = R.drawable.habitosperestadisticas),
                            contentDescription = null,
                            modifier = Modifier.size(90.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.habitosperestadisticas),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                ) {
                    SelectorHabitosCentradoEstadisticas(
                        habitos = habitosFisicos,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { nuevoIndice: Int ->
                            val nuevoHabito = habitosFisicos[nuevoIndice]
                            val fechaInicio = nuevoHabito.fechaCreacion?.let {
                                LocalDate.parse(it).with(DayOfWeek.MONDAY)
                            } ?: LocalDate.now().with(DayOfWeek.MONDAY)

                            val lunesActual = LocalDate.now().with(DayOfWeek.MONDAY)
                            semanaVisible =
                                if (fechaInicio.isAfter(lunesActual)) fechaInicio else lunesActual
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- RESUMEN SEMANAL ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$diasRegistrados/$diasPlaneados días",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Gráfico",
                    tint = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Log.d("Graficador", "==== LLAVES DEL MAPA progresoPorDia ====")
            progresoActual.keys.forEach { fecha ->
                Log.d("Graficador", "Fecha en progresoPorDia: $fecha")
            }

            GraficadorProgresoHabitoSwipe(
                progresoPorDia = progresoActual,
                frecuenciaPorDefecto = habitoActual.diasSeleccionados,
                colorHabito = colorHabito,
                fechaInicioHabito = habitoActual.fechaCreacion,
                semanaReferencia = semanaVisible,
                onSemanaChange = { nuevaSemana -> semanaVisible = nuevaSemana }
            )

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = { navController.navigate("salud_fisica") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = White
                )
            ) {
                Text("Gestionar hábitos")
            }
        }
    }
}

/* ---------- SELECTOR INFINITO DE HÁBITOS (AZUL, SIN VERDE) ---------- */

@Composable
private fun SelectorHabitosCentradoEstadisticas(
    habitos: List<Habito>,
    selectedIndex: MutableState<Int>,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (habitos.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    val titulos: List<String> = remember(habitos) { habitos.map { it.titulo } }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centerSelectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                selectedIndex.value
            } else {
                val center = layoutInfo.viewportStartOffset +
                        (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull { info ->
                    kotlin.math.abs((info.offset + info.size / 2) - center)
                }
                val index = closestItem?.index ?: 0
                index % titulos.size
            }
        }
    }

    // Posición inicial alta para simular scroll infinito
    LaunchedEffect(Unit) {
        val base = 50_000
        val startIndex = base - (base % titulos.size) + selectedIndex.value
        listState.scrollToItem(startIndex)
    }

    // Notificar al exterior cuando cambie el ítem centrado
    LaunchedEffect(centerSelectedIndex) {
        if (centerSelectedIndex != selectedIndex.value) {
            selectedIndex.value = centerSelectedIndex
            onSelectedIndexChange(centerSelectedIndex)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            items(count = 100_000) { index: Int ->
                val realIndex = index % titulos.size
                val isSelected = realIndex == centerSelectedIndex

                val alpha = if (isSelected) 1f else 0.4f
                val fontSize = if (isSelected) 18.sp else 14.sp
                val itemHeight = if (isSelected) 40.dp else 30.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = titulos[realIndex],
                        fontSize = fontSize,
                        fontWeight = if (isSelected) FontWeight.SemiBold
                        else FontWeight.Normal,
                        color = colorScheme.onSurface.copy(alpha = alpha)
                    )
                }
            }
        }

        // Gradiente superior/inferior usando el background del tema
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.background,
                            Color.Transparent,
                            Color.Transparent,
                            colorScheme.background
                        )
                    )
                )
        )
    }
}
