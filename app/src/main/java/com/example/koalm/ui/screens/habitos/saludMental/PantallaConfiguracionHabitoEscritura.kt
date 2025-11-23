package com.example.koalm.ui.screens.habitos.saludMental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.WritingNotificationService
import com.example.koalm.services.timers.NotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.DiasSeleccionadosResumen
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoEscritura(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val TAG = "PantallaConfiguracionHabito"
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val esEdicion = habitoId != null
    val currentUser = auth.currentUser

    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val cardContainerColor = if (isDark) colorScheme.surface else ContainerColor
    val cardBorderColor = if (isDark) colorScheme.outlineVariant else BorderColor

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = { mensajeValidacion = null }
        )
    }

    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito configurado correctamente!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("salud_mental") {
                    popUpTo("salud_mental") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    // -----------------------------  State  ------------------------------
    var descripcion by remember { mutableStateOf("") }
    var notasHabilitadas by remember { mutableStateOf(false) }

    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var duracionMin by remember { mutableStateOf(15f) }      // 1-180 min
    val rangoDuracion = 1f..180f

    var objetivoPaginas by remember { mutableStateOf(1) }

    val sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
    LaunchedEffect(Unit) {
        val defaultDuration = 15L * 60 * 1000 // 15 minutos en milisegundos
        duracionMin = (sharedPreferences.getLong(
            "writing_timer_duration",
            defaultDuration
        ) / (60 * 1000)).toFloat()
    }

    var hora by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }
    var habitoExistente by remember { mutableStateOf<Habito?>(null) }

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    descripcion = habito.descripcion
                    diasSeleccionados = habito.diasSeleccionados
                    hora = try {
                        LocalTime.parse(habito.hora)
                    } catch (e: Exception) {
                        LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
                    }
                    duracionMin = habito.duracionMinutos.toFloat()
                    objetivoPaginas = habito.objetivoPaginas ?: 1
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

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val currentUserLocal = auth.currentUser
            if (currentUserLocal == null) {
                mensajeValidacion = "Debes iniciar sesión para crear un hábito"
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    val habito = Habito(
                        id = habitoId ?: "",
                        titulo = "Escritura",
                        descripcion = descripcion.ifEmpty {
                            context.getString(R.string.notification_default_text)
                        },
                        clase = ClaseHabito.MENTAL,
                        tipo = TipoHabito.ESCRITURA,
                        diasSeleccionados = diasSeleccionados,
                        hora = hora.format(DateTimeFormatter.ofPattern("HH:mm")),
                        duracionMinutos = duracionMin.toInt(),
                        userId = currentUserLocal.uid,
                        objetivoPaginas = objetivoPaginas,
                        metricasEspecificas = MetricasHabito(),
                        fechaCreacion = if (esEdicion) {
                            habitoExistente?.fechaCreacion
                        } else {
                            LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        },
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
                        ultimoDiaCompletado = if (esEdicion) {
                            habitoExistente?.ultimoDiaCompletado
                        } else {
                            null
                        }
                    )

                    if (habitoId != null) {
                        // EDICIÓN
                        habitosRepository.actualizarHabitoO(habitoId, habito).fold(
                            onSuccess = {
                                Log.d(TAG, "Hábito actualizado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = WritingNotificationService()
                                val notificationTime =
                                    LocalDateTime.of(
                                        LocalDateTime.now().toLocalDate(),
                                        hora
                                    )

                                notificationService.cancelNotifications(context)
                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty {
                                        context.getString(R.string.reading_notification_default_text)
                                    },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_meditation" to false,
                                        "is_reading" to false,
                                        "is_writing" to true,
                                        "is_digital_disconnect" to false
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
                                    totalObjetivoDiario = objetivoPaginas
                                )

                                val progresoRef = userHabitsRef?.document(habitoId)
                                    ?.collection("progreso")
                                    ?.document(
                                        LocalDate.now().format(
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        )
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
                                    navController.navigate("salud_mental") {
                                        popUpTo("salud_mental") { inclusive = true }
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
                        // CREACIÓN
                        habitosRepository.crearHabito(habito).fold(
                            onSuccess = { nuevoHabitoId ->
                                Log.d(TAG, "Hábito creado exitosamente con ID: $nuevoHabitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = WritingNotificationService()
                                val notificationTime =
                                    LocalDateTime.of(
                                        LocalDateTime.now().toLocalDate(),
                                        hora
                                    )

                                Log.d(TAG, "Iniciando servicio de notificaciones")
                                context.startService(
                                    Intent(
                                        context,
                                        NotificationService::class.java
                                    )
                                )

                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty {
                                        context.getString(R.string.reading_notification_default_text)
                                    },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_meditation" to false,
                                        "is_reading" to false,
                                        "is_writing" to true,
                                        "is_digital_disconnect" to false
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
                                    totalObjetivoDiario = objetivoPaginas
                                )

                                val progresoRef = userHabitsRef?.document(nuevoHabitoId)
                                    ?.collection("progreso")
                                    ?.document(
                                        LocalDate.now().format(
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        )
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
                                Log.e(TAG, "Error al crear hábito: ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "Error al crear hábito: ${error.message}",
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
            mensajeValidacion = context.getString(R.string.error_notification_permission)
        }
    }

    // Lambda para validaciones del botón Guardar (alineado con meditación)
    val onGuardarClick: () -> Unit = {
        when {
            !diasSeleccionados.any { it } ->
                mensajeValidacion = "Por favor, selecciona al menos un día de la semana."
            duracionMin <= 0f ->
                mensajeValidacion = "La duración debe ser mayor a 0 minutos."
            objetivoPaginas <= 0 ->
                mensajeValidacion =
                    "Por favor, establece cuántas páginas quieres escribir por día."
            auth.currentUser == null ->
                mensajeValidacion = "Debes iniciar sesión para crear un hábito."
            else ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ----------------------------------  UI  ----------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_escritura)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "configurar_habito") }
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Tarjeta principal
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

                    // Descripción del hábito
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Objetivo de páginas
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta("Objetivo de páginas: *")
                        OutlinedTextField(
                            value = if (objetivoPaginas == 0) "" else objetivoPaginas.toString(),
                            onValueChange = { nuevoTexto ->
                                nuevoTexto.toIntOrNull()?.let {
                                    if (it > 0) objetivoPaginas = it
                                }
                                if (nuevoTexto.isEmpty()) objetivoPaginas = 0
                            },
                            label = { Text("Páginas por día") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Establece cuántas páginas quieres escribir por día.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Selección de días
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Etiqueta(stringResource(R.string.label_frecuencia))
                        TooltipDialogAyuda(
                            titulo = "Frecuencia",
                            mensaje = "Selecciona los días de la semana en los que deseas mantener activo tu hábito."
                        )
                    }

                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            diasSemana.forEachIndexed { i, d ->
                                DiaCircle(
                                    label = d,
                                    selected = diasSeleccionados[i],
                                    onClick = {
                                        diasSeleccionados =
                                            diasSeleccionados.toMutableList()
                                                .also { it[i] = !it[i] }
                                    }
                                )
                            }
                        }
                        DiasSeleccionadosResumen(diasSeleccionados)
                    }

                    // Hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Etiqueta(stringResource(R.string.label_hora))
                        TooltipDialogAyuda(
                            titulo = "Recordatorio",
                            mensaje = "Establece una notificación personalizada para ayudarte a cumplir tu hábito."
                        )
                    }
                    HoraField(hora) { mostrarTimePicker = true }

                    // Duración
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Etiqueta(stringResource(R.string.label_duracion_escritura))
                            TooltipDialogAyuda(
                                titulo = "Duración de la escritura",
                                mensaje = "Configura cuánto tiempo deseas escribir. Al llegar la hora establecida, recibirás una notificación para ayudarte a cumplir con tu hábito."
                            )
                        }
                        Text(
                            text = formatearDuracion(duracionMin.roundToInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSlider(
                            value = duracionMin,
                            onValueChange = { duracionMin = it },
                            valueRange = rangoDuracion,
                            tickEvery = 15,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.hint_duracion),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botones Guardar / Cancelar alineados como en meditación
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (esEdicion) {
                        Button(
                            onClick = onGuardarClick,
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                stringResource(R.string.boton_guardar),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEC615B)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.boton_cancelar_modificaciones),
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Button(
                            onClick = onGuardarClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                stringResource(R.string.boton_guardar),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Time Picker
    if (mostrarTimePicker) {
        TimePickerDialog(
            initialTime = hora,
            onTimePicked = { hora = it },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

/* ─────────────────────────────────  COMPONENTES  ────────────────────────────── */

@Composable
private fun Etiqueta(texto: String) = Text(
    text = texto,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Medium
)

@Composable
fun DiaCircle(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun HoraField(hora: LocalTime, onClick: () -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = hora.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ──────────────────────────  SLIDER «PIXEL STYLE»  ─────────────────────────── */

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DurationSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
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
            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Center)
            )

            // Ticks
            Row(
                Modifier
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
                        Modifier
                            .size(trackHeight * 0.3f)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                    )
                }
            }

            // Slider transparente (para la lógica)
            Slider(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = 0,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )

            // Thumb visual
            val progress =
                (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val thumbOffset = with(density) { (progress * maxWidthPx).toDp() }

            Box(
                Modifier
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

/* ────────────────────────────  TIME PICKER  ──────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialTime.hour, initialTime.minute, is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimePicked(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text(stringResource(android.R.string.ok).uppercase()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel).uppercase())
            }
        },
        text = { TimePicker(state, modifier = Modifier.fillMaxWidth()) }
    )
}

/* ────────────────────────────  HELPERS  ─────────────────────────────────── */

private fun formatearDuracion(min: Int): String = when {
    min < 60 -> "$min min"
    min == 60 -> "1 hora"
    min % 60 == 0 -> "${min / 60} h"
    else -> "${min / 60} h ${min % 60} min"
}
