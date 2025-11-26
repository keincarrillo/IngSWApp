// PantallaActividadDiaria.kt
package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import com.example.koalm.viewmodels.ActividadDiariaViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.ui.draw.clip

data class ActividadDiaria(
    val tipo: String,
    val meta: Float,
    val datos: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividadDiaria(
    navController: NavHostController,
    viewModel: ActividadDiariaViewModel = viewModel()
) {
    val actividadesState by viewModel.actividades.collectAsState(initial = emptyList())
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    if (actividadesState.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    val tipos = actividadesState.map { it.tipo }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index?.rem(tipos.size) ?: 0
        }
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(50_000)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Actividad diaria",
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            val hoy = LocalDate.now()
            val lunesSemana = hoy.with(DayOfWeek.MONDAY)
            val fechasSemana = List(7) { lunesSemana.plusDays(it.toLong()) }
            val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
            val numerosDias = fechasSemana.map { it.dayOfMonth.toString() }
            val indiceHoy = hoy.dayOfWeek.value - 1

            var diaSeleccionado by remember { mutableStateOf(indiceHoy) }

            // ---------------- DÍAS DE LA SEMANA ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                letrasDias.forEachIndexed { index, letra ->
                    val esSeleccionado = index == diaSeleccionado
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { diaSeleccionado = index }
                    ) {
                        Text(
                            text = letra,
                            fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (esSeleccionado)
                                PrimaryColor
                            else
                                colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (esSeleccionado) {
                            // Pingüino pequeño con círculo gris en modo oscuro
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDark) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = colorScheme.surfaceVariant,
                                        tonalElevation = 0.dp
                                    ) {}
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.pinguino_corriendo),
                                    contentDescription = "Día seleccionado",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (!esSeleccionado) {
                            Text(
                                text = numerosDias[index],
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- PINGÜINO GRANDE + SELECTOR ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Pingüino grande con círculo gris en modo oscuro
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDark) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = colorScheme.surfaceVariant,
                            tonalElevation = 0.dp
                        ) {}
                    }
                    Image(
                        painter = painterResource(id = R.drawable.pinguino_corriendo),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = flingBehavior,
                        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(count = 100_000) { index ->
                            val actualIndex = index % tipos.size
                            val isSelectedTipo = actualIndex == selectedIndex

                            val alpha = if (isSelectedTipo) 1f else 0.4f
                            val fontSize = if (isSelectedTipo) 18.sp else 14.sp
                            val itemHeight = if (isSelectedTipo) 40.dp else 30.dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tipos[actualIndex],
                                    fontWeight = if (isSelectedTipo) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = fontSize,
                                    color = colorScheme.onSurface.copy(alpha = alpha)
                                )
                            }
                        }
                    }

                    // Gradiente sin blanco (usa background del tema)
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

            Spacer(modifier = Modifier.height(24.dp))

            GraficadorActividadDia(
                actividad = actividadesState[selectedIndex],
                indiceSeleccionado = diaSeleccionado,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val rutas = listOf(
                "meta-diaria-pasos",
                "meta-diaria-calorias",
                "meta-diaria-movimiento"
            )
            Button(
                onClick = { navController.navigate(rutas[selectedIndex]) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Editar objetivo")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GraficadorActividadDia(
    actividad: ActividadDiaria,
    indiceSeleccionado: Int,
) {
    val colorScheme = MaterialTheme.colorScheme
    val valorDia = actividad.datos.getOrNull(indiceSeleccionado) ?: 0f
    val meta = actividad.meta
    val proporcion = (valorDia / meta).coerceIn(0f, 1f)

    // 👉 Barra SIEMPRE del color del botón "Editar objetivo" (PrimaryColor)

    val graficoHeight = 180.dp
    val ejeYWidth = 40.dp
    val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
    val letraDia = letrasDias.getOrNull(indiceSeleccionado) ?: ""


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(graficoHeight),
        horizontalArrangement = Arrangement.Start // grupo un poco más a la izquierda
    ) {
        // Eje Y con porcentajes
        Box(
            modifier = Modifier
                .width(ejeYWidth)
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            val niveles = listOf("100%", "75%", "50%", "25%", "0%")
            val posiciones = listOf(1f, 0.75f, 0.5f, 0.25f, 0f)
            niveles.forEachIndexed { i, texto ->
                Text(
                    text = texto,
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -(posiciones[i] * (graficoHeight.value - 10)).dp)
                )
            }
        }

        // Zona de la gráfica
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Líneas guías punteadas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val heightPx = size.height
                val widthPx = size.width
                val lineY = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
                lineY.forEach { y ->
                    val yPos = heightPx * (1 - y)
                    drawLine(
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                        start = Offset(0f, yPos),
                        end = Offset(widthPx, yPos),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // Barra ligeramente hacia la izquierda, pero dentro de la zona de gráfica
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 20.dp), // mueve la barra un poco hacia el eje Y
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "${valorDia.toInt()} / ${meta.toInt()}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Track grueso: base cuadrada
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .width(32.dp)
                            .background(PrimaryColor),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(proporcion)
                                .align(Alignment.BottomCenter)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                                .background(PrimaryColor)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Letra del día bajo la barra
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = letraDia,
            fontSize = 10.sp,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(6.dp)
        )
    }
}
