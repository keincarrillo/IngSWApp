package com.example.koalm.ui.screens.estaditicas

import androidx.compose.animation.*
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior // <--- IMPORTANTE
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.screens.habitos.personalizados.parseColorFromFirebase
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import android.graphics.Typeface
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.Animatable
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticasHabitoPersonalizado(
    navController: NavHostController
) {
    val habitos = remember { mutableStateListOf<HabitoPersonalizado>() }
    val progresoPorHabito = remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()

    // Cargar hábitos desde Firestore
    LaunchedEffect(Unit) {
        Log.d("Graficador", "Iniciando carga de hábitos personalizados")
        val habitosSnapshot = userEmail?.let {
            db.collection("habitos")
                .document(it)
                .collection("personalizados")
                .get()
                .await()
        }
        val listaHabitos = habitosSnapshot?.documents?.mapNotNull { doc ->
            doc.toObject(HabitoPersonalizado::class.java)?.copy(nombre = doc.getString("nombre") ?: "")
        }
        habitos.clear()
        if (listaHabitos != null) {
            habitos.addAll(listaHabitos)
        }

        // Para cada hábito, cargar su progreso diario
        listaHabitos?.forEach { habito ->
            val idDoc = habito.nombre.replace(" ", "_")
            val progresoSnapshot = db.collection("habitos")
                .document(userEmail)
                .collection("personalizados")
                .document(idDoc)
                .collection("progreso")
                .get().await()

            val progresoMap = progresoSnapshot.documents.mapNotNull { doc ->
                val fechaStr = doc.getString("fecha") ?: return@mapNotNull null
                val progreso = doc.toObject(ProgresoDiario::class.java) ?: return@mapNotNull null
                try {
                    val fecha = LocalDate.parse(fechaStr)
                    fecha to progreso
                } catch (e: Exception) {
                    null
                }
            }.toMap()

            progresoPorHabito[habito.nombre] = progresoMap
        }
    }

    if (habitos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando hábitos...")
        }
        return
    }

    val habitoActual = habitos.getOrNull(selectedIndex.value) ?: habitos.first()
    val progresoActual = progresoPorHabito[habitoActual.nombre] ?: emptyMap()

    // Usamos PrimaryColor para que las barras sean azules y consistentes con las otras pantallas
    val colorHabito = PrimaryColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos personalizados") },
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
            return habitoActual.frecuencia
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

            // ---------- FILA DEL PINGÜINO Y EL SELECTOR (MEJORADA) ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDark = isSystemInDarkTheme()

                // 1. PINGÜINO (Más grande, con fondo en modo oscuro)
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

                // 2. SELECTOR INFINITO
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                ) {
                    SelectorHabitosCentradoInfinitoPersonalizado(
                        habitos = habitos,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { nuevoIndice ->
                            val nuevoHabito = habitos[nuevoIndice]
                            val fechaInicio = nuevoHabito.fechaInicio?.let {
                                LocalDate.parse(it).with(DayOfWeek.MONDAY)
                            } ?: LocalDate.now().with(DayOfWeek.MONDAY)

                            // Resetear semana visible al cambiar de hábito
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
                frecuenciaPorDefecto = habitoActual.frecuencia,
                colorHabito = colorHabito,
                fechaInicioHabito = habitoActual.fechaInicio,
                semanaReferencia = semanaVisible,
                onSemanaChange = { nuevaSemana -> semanaVisible = nuevaSemana }
            )

            Spacer(modifier = Modifier.height(15.dp))

            // 3. BOTÓN AZUL
            Button(
                onClick = { navController.navigate("gestion_habitos_personalizados") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor, // Ahora es azul
                    contentColor = Color.White
                )
            ) {
                Text("Gestionar hábitos")
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

// ---------------- COMPONENTES AUXILIARES ----------------

@Composable
fun IndicadorCircular(titulo: String, valor: Int, maximo: Int) {
    val progreso = if (maximo == 0) 0f else valor.toFloat() / maximo.toFloat()
    val colorProgreso = if (progreso == 0f) MaterialTheme.colorScheme.outlineVariant else PrimaryColor
    val isDark = isSystemInDarkTheme()
    val trackColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else TertiaryColor

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp)
        ) {
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxSize(),
                color = colorProgreso,
                strokeWidth = 6.dp,
                trackColor = trackColor,
            )
            Text(
                "$valor días",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            titulo,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SelectorHabitosCentradoInfinitoPersonalizado(
    habitos: List<HabitoPersonalizado>,
    selectedIndex: MutableState<Int>,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (habitos.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    val titulos = remember(habitos) { habitos.map { it.nombre } }

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

    LaunchedEffect(Unit) {
        val base = 50_000
        val startIndex = base - (base % titulos.size) + selectedIndex.value
        listState.scrollToItem(startIndex)
    }

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
                        color = colorScheme.onSurface.copy(alpha = alpha)
                    )
                }
            }
        }

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

// ---------------- FUNCIONES DE GRÁFICA Y SWIPE ----------------

// NOTA: Esta función 'GraficadorProgreso' DEBE reemplazar a la anterior.
// Ya incluye la corrección del fondo transparente para modo oscuro.
@Composable
public fun GraficadorProgreso(
    valores: List<Pair<Float, Float>>,
    etiquetas: List<String>,
    colorHabito: Color,
    meta: Int,
    labelEjeY: String = "Objetivo",
    labelEjeX: String = "Días activos del hábito"
){
    val isDark = isSystemInDarkTheme()

    // FONDO TRANSPARENTE EN MODO OSCURO (para que se vea limpio)
    val backgroundColor = if (isDark) Color.Transparent else Color(0xFFF0F0F0)

    val axisTextColor = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
    val labelTextColor = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(start = 43.dp, end = 16.dp, top = 14.dp, bottom = 34.dp)
    ) {
        if (valores.isEmpty() || etiquetas.isEmpty()) return@Canvas

        val barWidth = size.width / valores.size
        val nivelMaximo = meta.coerceAtLeast(1)
        val stepLabel = when {
            nivelMaximo <= 10 -> 1
            nivelMaximo <= 30 -> 5
            nivelMaximo <= 100 -> 10
            else -> 20
        }
        val stepY = size.height / nivelMaximo.toFloat()

        val paintY = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.RIGHT
            textSize = 24f
            color = labelTextColor
            isAntiAlias = true
        }

        // Título Y
        drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(-90f, 0f, size.height / 2)
            drawText(
                labelEjeY,
                -size.height / 12,
                70f,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f
                    color = axisTextColor
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            )
            restore()
        }

        // Líneas guía
        for (i in 0..nivelMaximo step stepLabel) {
            val y = size.height - i * stepY
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f),0f)
            )
            drawContext.canvas.nativeCanvas.drawText("$i", -30f, y + 8f, paintY)
        }

        // Barras
        valores.forEachIndexed { index, (realizados, total) ->
            val barHeight = realizados * stepY
            val barCenter = index * barWidth + barWidth / 2
            val barThickness = 8.dp.toPx()
            val barLeft = barCenter - barThickness / 2
            val barRight = barCenter + barThickness / 2
            val barTop = size.height - barHeight
            val barColor = if (realizados >= total) colorHabito else Color.LightGray

            drawRoundRect(
                color = barColor,
                topLeft = Offset(barLeft, barTop),
                size = Size(barRight - barLeft, barHeight),
                cornerRadius = CornerRadius(x = 12f, y = 12f)
            )
        }

        // Etiquetas X
        val paintX = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 28f
            color = axisTextColor
            isAntiAlias = true
        }

        etiquetas.forEachIndexed { index, label ->
            val x = (index * barWidth) + barWidth / 2
            drawContext.canvas.nativeCanvas.drawText(label, x, size.height + 50f, paintX)
        }

        // Título X
        drawContext.canvas.nativeCanvas.drawText(
            labelEjeX,
            size.width / 2,
            size.height + 130f,
            android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 32f
                color = axisTextColor
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )
    }
}

// ----------------- LÓGICA DE DATOS Y SWIPE (SIN CAMBIOS ESTRUCTURALES) ----------------

public fun prepararDatosParaGrafica(
    progresoPorDia: Map<LocalDate, ProgresoDiario>,
    frecuenciaPorDefecto: List<Boolean>?,
    semanaReferencia: LocalDate
): Triple<List<LocalDate>, List<Pair<Float, Float>>, List<String>> {
    val inicioSemana = semanaReferencia
    val fechasSemana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

    val fechasActivas = fechasSemana.filter { fecha ->
        val index = fecha.dayOfWeek.ordinal
        val progresoDelDia = progresoPorDia[fecha]
        val frecuenciaDia: List<Boolean>? = when {
            progresoDelDia != null -> progresoDelDia.frecuencia
            else -> {
                val frecuenciaAnterior = progresoPorDia
                    .filterKeys { it.isBefore(fecha) }
                    .maxByOrNull { it.key }?.value?.frecuencia
                frecuenciaAnterior ?: frecuenciaPorDefecto
            }
        }
        frecuenciaDia?.getOrNull(index) == true
    }

    val valores = fechasActivas.map { fecha ->
        val progreso = progresoPorDia[fecha]
        val realizados = progreso?.realizados?.toFloat() ?: 0f
        val total = progreso?.totalObjetivoDiario?.toFloat() ?: 1f
        Pair(realizados, total)
    }

    val etiquetas = fechasActivas.map {
        when (it.dayOfWeek) {
            DayOfWeek.MONDAY -> "L"
            DayOfWeek.TUESDAY -> "M"
            DayOfWeek.WEDNESDAY -> "X"
            DayOfWeek.THURSDAY -> "J"
            DayOfWeek.FRIDAY -> "V"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "D"
        }
    }
    return Triple(fechasActivas, valores, etiquetas)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun GraficadorProgresoHabitoSwipe(
    progresoPorDia: Map<LocalDate, ProgresoDiario>,
    frecuenciaPorDefecto: List<Boolean>?,
    colorHabito: Color,
    fechaInicioHabito: String?,
    semanaReferencia: LocalDate,
    onSemanaChange: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("dd", Locale("es"))
    val formatterMesAnio = DateTimeFormatter.ofPattern("MMM yyyy", Locale("es"))

    val fechaInicio = fechaInicioHabito?.let {
        LocalDate.parse(it).with(DayOfWeek.MONDAY)
    } ?: LocalDate.MIN

    val semanaActual = LocalDate.now().with(DayOfWeek.MONDAY)
    var dragAccumulated by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    val inicioSemana = semanaReferencia
    val finSemana = semanaReferencia.plusDays(6)
    val tituloSemana = "Semana del ${inicioSemana.format(formatter)} al ${finSemana.format(formatter)} ${finSemana.format(formatterMesAnio)}"

    val metaMaxima = prepararDatosParaGrafica(
        progresoPorDia = progresoPorDia,
        frecuenciaPorDefecto = frecuenciaPorDefecto,
        semanaReferencia = semanaReferencia
    ).second.maxOfOrNull { it.second.toInt() }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(fechaInicio, semanaReferencia) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val siguienteSemana = semanaReferencia.plusWeeks(1)
                        val semanaAnteriorTemp = semanaReferencia.minusWeeks(1)

                        when {
                            dragAccumulated < -100f && !siguienteSemana.isAfter(semanaActual) -> {
                                onSemanaChange(siguienteSemana)
                            }
                            dragAccumulated > 100f && !semanaAnteriorTemp.isBefore(fechaInicio) -> {
                                onSemanaChange(semanaAnteriorTemp)
                            }
                            else -> {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                                }
                            }
                        }
                        dragAccumulated = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragAccumulated += dragAmount
                }
            }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onSemanaChange(semanaReferencia.minusWeeks(1)) },
                enabled = !semanaReferencia.minusWeeks(1).isBefore(fechaInicio)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Semana anterior")
            }

            Text(
                text = tituloSemana,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onSemanaChange(semanaReferencia.plusWeeks(1)) },
                enabled = semanaReferencia.isBefore(semanaActual)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Semana siguiente",
                    tint = if (semanaReferencia.isBefore(semanaActual)) LocalContentColor.current else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        GraficadorProgreso(
            valores = prepararDatosParaGrafica(progresoPorDia, frecuenciaPorDefecto, semanaReferencia).second,
            etiquetas = prepararDatosParaGrafica(progresoPorDia, frecuenciaPorDefecto, semanaReferencia).third,
            meta = metaMaxima,
            colorHabito = colorHabito
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Desliza o usa las flechas para cambiar de semana",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}