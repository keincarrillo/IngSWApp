package com.example.koalm.ui.screens.habitos.saludFisica

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.HydrationNotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val TAG = "PantallaConfigHidratacion"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoHidratacion(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val cardContainerColor = if (isDark) colorScheme.surface else ContainerColor
    val cardBorderColor = if (isDark) colorScheme.outlineVariant else BorderColor
    val textoColor = colorScheme.onSurface

    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    var descripcion by remember { mutableStateOf("") }
    val esEdicion = habitoId != null

    var cantLitros by remember { mutableStateOf(2) }
    val rangoLitros = 0.5f..10f

    val horarios = remember { mutableStateListOf<LocalTime>() }
    var horaIni by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var mostrarTimePickerIni by remember { mutableStateOf(false) }
    var horaFin by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var mostrarTimePickerFin by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val habitosRepository = remember { HabitoRepository() }

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = {
                mensajeValidacion = null
            }
        )
    }

    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito configurado correctamente!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("salud_fisica") {
                    popUpTo("salud_fisica") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    var mostrarDialogoFalloHora by remember { mutableStateOf(false) }
    if (mostrarDialogoFalloHora) {
        FalloDialogoGuardadoAnimado(
            mensaje = "Ya agregaste esta hora.",
            onDismiss = {
                mostrarDialogoFalloHora = false
            }
        )
    }

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }
    var habitoExistente by remember { mutableStateOf<Habito?>(null) }
    val currentUser = auth.currentUser

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    cantLitros = habito.objetivoPaginas
                    descripcion = habito.descripcion
                    horarios.clear()
                    try {
                        horarios.addAll(habito.horarios.map { LocalTime.parse(it) })
                    } catch (e: Exception) {
                        horarios.add(LocalTime.now().plusMinutes(1).withSecond(0).withNano(0))
                    }
                },
                onFailure = {
                    Log.e(TAG, "No se pudo cargar el hábito con ID: $habitoId")
                }
            )
        }
    }

    LaunchedEffect(esEdicion, habitoId) {
        if (esEdicion && habitoId != null && currentUser != null) {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("habitos")
                .document(currentUser.uid)
                .collection("predeterminados")
                .document(habitoId)
                .get()
                .await()

            habitoExistente = snapshot.toObject(Habito::class.java)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                mensajeValidacion = "Debes iniciar sesión para crear un hábito"
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    val habito = Habito(
                        id = habitoId ?: "",
                        titulo = "Hidratación",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.hidratacionn_notification_default_text) },
                        diasSeleccionados = List(7) { true },
                        clase = ClaseHabito.FISICO,
                        tipo = TipoHabito.HIDRATACION,
                        horarios = horarios.map {
                            it.format(
                                DateTimeFormatter.ofPattern(
                                    "HH:mm"
                                )
                            )
                        },
                        objetivoPaginas = cantLitros,
                        userId = currentUser.uid,
                        fechaCreacion = if (esEdicion) habitoExistente?.fechaCreacion else LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        metricasEspecificas = MetricasHabito(),
                        rachaActual = if (!esEdicion) {
                            0
                        } else {
                            habitoExistente?.rachaActual ?: 0
                        },
                        rachaMaxima = if (!esEdicion) {
                            0
                        } else {
                            habitoExistente?.rachaMaxima ?: 0
                        },
                        ultimoDiaCompletado = if (esEdicion) habitoExistente?.ultimoDiaCompletado else null
                    )

                    if (habitoId != null) {
                        habitosRepository.actualizarHabitoO(habitoId, habito).fold(
                            onSuccess = {
                                Log.d(TAG, "Hábito actualizado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = HydrationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_hydration" to true
                                    )
                                )

                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantLitros
                                )

                                val progresoRef = userHabitsRef?.document(habitoId)
                                    ?.collection("progreso")
                                    ?.document(
                                        LocalDate.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    )

                                progresoRef?.set(progreso.toMap())?.addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "Guardando progreso para hábito ID: $habitoId, fecha: ${
                                            LocalDate.now().format(
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                            )
                                        }"
                                    )
                                }?.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error al guardar el progreso: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("salud_fisica") {
                                        popUpTo("salud_fisica") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }

                                mostrarDialogoExito = true
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Error al actualizar hábito: ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "Error al actualizar hábito: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } else {
                        habitosRepository.crearHabito(habito).fold(
                            onSuccess = { nuevoHabitoId ->
                                Log.d(TAG, "Hábito creado exitosamente con ID: $nuevoHabitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = HydrationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_hydration" to true
                                    )
                                )

                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantLitros
                                )

                                val progresoRef = userHabitsRef?.document(nuevoHabitoId)
                                    ?.collection("progreso")
                                    ?.document(
                                        LocalDate.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    )

                                progresoRef?.set(progreso.toMap())?.addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "Guardando progreso para hábito ID: $nuevoHabitoId, fecha: ${
                                            LocalDate.now().format(
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                            )
                                        }"
                                    )
                                }?.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error al guardar el progreso: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("salud_mental") {
                                        popUpTo("salud_mental") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }

                                mostrarDialogoExito = true
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Error al crear el hábito: ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "Error al crear el hábito: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error inesperado: ${e.message}", e)
                    Toast.makeText(
                        context,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            mensajeValidacion = "Se requieren permisos de notificaciones"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_hidratacion)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "configurar_habito")
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorderColor),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_hidratacion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_cantidad_litros),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatearLitros(cantLitros.toDouble()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSliderHidra(
                            value = cantLitros,
                            onValueChange = { cantLitros = it },
                            valueRange = rangoLitros,
                            tickEvery = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Selecciona la cantidad de litros diarios a tomar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Horario de recordatorios: *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Horario de recordatorios",
                            mensaje = "Configura los horarios en los que deseas recibir recordatorios para tomar agua. Recibirás una notificación en cada hora que elijas para ayudarte a mantener una buena hidratación."
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        horarios.forEachIndexed { index, hora ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    HoraField(hora = hora) {
                                        mostrarTimePicker = true
                                        horaAEditarIndex = index
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        horarios.removeAt(index)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar horario",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                horaAEditarIndex = null
                                mostrarTimePicker = true
                            }
                            .padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Agregar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Agregar hora.",
                            fontSize = 14.sp,
                            color = textoColor
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (horarios.isNotEmpty()) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                mensajeValidacion = "Por favor, selecciona al menos un recordatorio."
                                return@Button
                            }
                        },
                        modifier = Modifier
                            .width(180.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            stringResource(R.string.boton_guardar),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (esEdicion) {
                        Button(
                            onClick = {
                                navController.navigateUp()
                            },
                            modifier = Modifier
                                .width(180.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                        ) {
                            Text(
                                stringResource(R.string.boton_cancelar_modificaciones),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        if (mostrarTimePicker) {
            TimePickerDialog(
                initialTime = LocalTime.now(),
                onTimePicked = { nuevaHora ->
                    val horaSinSegundos = nuevaHora.withSecond(0).withNano(0)

                    if (horaAEditarIndex != null) {
                        horarios[horaAEditarIndex!!] = horaSinSegundos
                    } else {
                        if (!horarios.contains(horaSinSegundos)) {
                            horarios.add(horaSinSegundos)
                        } else {
                            mostrarDialogoFalloHora = true
                        }
                    }

                    mostrarTimePicker = false
                },
                onDismiss = { mostrarTimePicker = false }
            )
        }
    }

    if (mostrarTimePickerIni) {
        TimePickerDialog(
            initialTime = horaIni,
            onTimePicked = { horaIni = it },
            onDismiss = { mostrarTimePickerIni = false }
        )
    }

    if (mostrarTimePickerFin) {
        TimePickerDialog(
            initialTime = horaFin,
            onTimePicked = { horaFin = it },
            onDismiss = { mostrarTimePickerFin = false }
        )
    }
}

fun formatearLitros(cantidad: Double): String = when {
    cantidad < 1.0 -> "${(cantidad * 1000).toInt()} ml"
    cantidad == 1.0 -> "1 litro"
    cantidad % 1.0 == 0.0 -> "${cantidad.toInt()} litros"
    else -> "%.1f litros".format(cantidad)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DurationSliderHidra(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    tickEvery: Int,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 16.dp,
    thumbWidth: Dp = 4.dp,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight + 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Center)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tickCount =
                    ((valueRange.endInclusive - valueRange.start) / tickEvery).toInt() + 1
                repeat(tickCount) {
                    Box(
                        modifier = Modifier
                            .size(trackHeight * 0.3f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    )
                }
            }

            Slider(
                value = value.toFloat(),
                onValueChange = {
                    val rounded = it.roundToInt()
                    onValueChange(rounded)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = (valueRange.endInclusive - valueRange.start).toInt() - 1,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )

            val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val thumbOffset = with(density) { (progress * maxWidthPx).toDp() }

            Box(
                modifier = Modifier
                    .width(thumbWidth)
                    .height(trackHeight + 24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .clip(RoundedCornerShape(thumbWidth))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
