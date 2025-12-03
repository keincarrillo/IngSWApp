package com.example.koalm.ui.screens.estaditicas

import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior // <--- IMPORTANTE PARA SCROLL INFINITO
//import androidx.compose.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Habito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import android.graphics.Typeface
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadísticasSaludMental(
    navController: NavHostController
) {
    val progresoPorHabito = remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val db = FirebaseFirestore.getInstance()
    val habitoRepository = remember { HabitoRepository() }
    val habitosMentales = remember { mutableStateListOf<Habito>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    // Estado de la UI
    var isLoading by remember { mutableStateOf(true) }

    // Cargar hábitos desde Firestore
    LaunchedEffect(userEmail) {
        if (userEmail == null) return@LaunchedEffect

        try {
            // 1. Obtener hábitos mentales
            val resultado = habitoRepository.obtenerHabitosMentalesKary(userEmail)
            if (resultado.isSuccess) {
                val listaHabitos = resultado.getOrNull().orEmpty()
                habitosMentales.clear()
                habitosMentales.addAll(listaHabitos)

                // 2. Obtener progreso por cada hábito
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
                                fecha to progreso
                            } catch (e: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }.toMap()

                    progresoPorHabito[habito.titulo] = progresoMap
                }
            }
        } catch (e: Exception) {
            Log.e("Graficador", "Error inesperado: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (habitosMentales.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay hábitos mentales disponibles")
        }
        return
    }

    val habitoActual = habitosMentales[selectedIndex.value.coerceIn(habitosMentales.indices)]
    val progresoActual = progresoPorHabito[habitoActual.titulo] ?: emptyMap()

    // Color de las barras (Azul igual que en física)
    val colorHabito = PrimaryColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos mentales") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.navigate("menu")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { paddingValues ->
        var semanaVisible by remember { mutableStateOf(LocalDate.now().with(DayOfWeek.MONDAY)) }

        val inicioSemana = semanaVisible.with(DayOfWeek.MONDAY)
        val finSemana = inicioSemana.plusDays(6)

        val progresoSemanaActual = progresoActual.filterKeys { fecha ->
            fecha in inicioSemana..finSemana
        }

        val diasSemana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

        fun frecuenciaParaDia(fecha: LocalDate): List<Boolean>? {
            progresoSemanaActual[fecha]?.frecuencia?.let { return it }
            val fechasAnteriores = progresoActual.keys.filter { it < fecha }.sortedDescending()
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

        val diasRegistrados = progresoSemanaActual.count { it.value.completado}

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

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

            // ---------- FILA DEL PINGÜINO Y EL SELECTOR ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDark = isSystemInDarkTheme()

                // 1. PINGÜINO (Más grande, weight 0.4f)
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDark) {
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
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2. SELECTOR INFINITO (Menos espacio, weight 0.6f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                ) {
                    // Usamos el nuevo selector infinito
                    SelectorHabitosCentradoInfinito(
                        habitos = habitosMentales,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { nuevoIndice ->
                            val nuevoHabito = habitosMentales[nuevoIndice]
                            val fechaInicio = nuevoHabito.fechaCreacion?.let {
                                LocalDate.parse(it).with(DayOfWeek.MONDAY)
                            } ?: LocalDate.now().with(DayOfWeek.MONDAY)

                            // Resetear la semana visible al cambiar de hábito
                            val lunesActual = LocalDate.now().with(DayOfWeek.MONDAY)
                            semanaVisible = if (fechaInicio.isAfter(lunesActual)) fechaInicio else lunesActual
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$diasRegistrados/$diasPlaneados días",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.BarChart, contentDescription = "Gráfico")
            }

            Spacer(modifier = Modifier.height(12.dp))

            GraficadorProgresoHabitoSwipe(
                progresoPorDia = progresoActual,
                frecuenciaPorDefecto = habitoActual.diasSeleccionados,
                colorHabito = colorHabito,
                fechaInicioHabito = habitoActual.fechaCreacion,
                semanaReferencia = semanaVisible,
                onSemanaChange = { nuevaSemana -> semanaVisible = nuevaSemana }
            )

            Spacer(modifier = Modifier.height(15.dp))

            // 3. BOTÓN AZUL (PrimaryColor)
            Button(
                onClick = { navController.navigate("salud_mental") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor, // <--- CAMBIO: Ahora es azul (PrimaryColor)
                    contentColor = Color.White
                )
            ) {
                Text("Gestionar hábitos")
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

// ---------------- NUEVO COMPONENTES (COPIADO DE SALUD FÍSICA PARA UNIFICAR) ----------------

@Composable
fun SelectorHabitosCentradoInfinito(
    habitos: List<Habito>,
    selectedIndex: MutableState<Int>,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (habitos.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    val titulos = remember(habitos) { habitos.map { it.titulo } }

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

    // Notificar cambio
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
            items(count = 100_000) { index ->
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
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = colorScheme.onSurface.copy(alpha = alpha) // Color dinámico
                    )
                }
            }
        }

        // Gradiente superior/inferior para efecto de desvanecimiento
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