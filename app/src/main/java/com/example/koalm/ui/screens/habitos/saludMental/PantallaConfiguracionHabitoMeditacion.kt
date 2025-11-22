package com.example.koalm.ui.screens.habitos.saludMental

import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.DiasSeleccionadosResumen
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.example.koalm.utils.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val TAG = "PantallaConfiguracionHabitoMeditacion"

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PantallaConfiguracionHabitoMeditacion(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val esEdicion = habitoId != null

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

    var descripcion by remember { mutableStateOf("") }
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var horaRecordatorio by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    var duracionMin by remember { mutableStateOf(15f) }
    val rangoDuracion = 1f..180f

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }
    var habitoExistente by remember { mutableStateOf<Habito?>(null) }
    val currentUser = auth.currentUser

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    descripcion = habito.descripcion
                    diasSeleccionados = habito.diasSeleccionados
                    horaRecordatorio = try {
                        LocalTime.parse(habito.hora)
                    } catch (e: Exception) {
                        LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
                    }
                    duracionMin = habito.duracionMinutos.toFloat()
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
            val currentUserLocal = auth.currentUser
            if (currentUserLocal == null) {
                mensajeValidacion = "Debes iniciar sesión para crear un hábito"
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    val habito = Habito(
                        id = habitoId ?: "",
                        titulo = "Meditación",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.meditation_notification_default_text) },
                        clase = ClaseHabito.MENTAL,
                        tipo = TipoHabito.MEDITACION,
                        diasSeleccionados = diasSeleccionados,
                        hora = horaRecordatorio.format(DateTimeFormatter.ofPattern("HH:mm")),
                        duracionMinutos = duracionMin.toInt(),
                        userId = currentUserLocal.uid,
                        fechaCreacion = if (esEdicion) habitoExistente?.fechaCreacion else LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
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

                                val notificationService = MeditationNotificationService()
                                val notificationTime =
                                    LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaRecordatorio)

                                notificationService.cancelNotifications(context)
                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.meditation_notification_default_text) },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_meditation" to true,
                                        "is_reading" to false,
                                        "is_digital_disconnect" to false,
                                        "notas_habilitadas" to false,
                                        "sonidos_habilitados" to false,
                                        "ejercicio_respiracion" to false
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
                                    totalObjetivoDiario = duracionMin.toInt()
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
                        habitosRepository.crearHabito(habito).fold(
                            onSuccess = { nuevoHabitoId ->
                                Log.d(TAG, "Hábito creado exitosamente con ID: $nuevoHabitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = MeditationNotificationService()
                                val notificationTime =
                                    LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaRecordatorio)

                                Log.d(TAG, "Iniciando servicio de notificaciones")
                                context.startService(Intent(context, MeditationNotificationService::class.java))

                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.meditation_notification_default_text) },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_meditation" to true,
                                        "is_reading" to false,
                                        "is_digital_disconnect" to false,
                                        "notas_habilitadas" to false,
                                        "sonidos_habilitados" to false,
                                        "ejercicio_respiracion" to false
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
                                    totalObjetivoDiario = duracionMin.toInt()
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
                title = { Text(stringResource(R.string.titulo_config_meditacion)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_meditacion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_frecuencia),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
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
                                        diasSeleccionados = diasSeleccionados.toMutableList()
                                            .also { it[i] = !it[i] }
                                    }
                                )
                            }
                        }
                        DiasSeleccionadosResumen(diasSeleccionados)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_hora),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Recordatorio",
                            mensaje = "Establece una notificación personalizada para ayudarte a cumplir tu hábito."
                        )
                    }

                    HoraField(horaRecordatorio) { mostrarTimePicker = true }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_duracion_meditacion),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            TooltipDialogAyuda(
                                titulo = "Duración de la meditación",
                                mensaje = "Configura cuánto tiempo deseas meditar. Al llegar la hora establecida, recibirás una notificación para ayudarte a cumplir con tu hábito."
                            )
                        }
                        Text(
                            text = TimeUtils.formatearDuracion(duracionMin.roundToInt()),
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
                            text = "Selecciona el tiempo que quieres que dure tu meditación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            if (diasSeleccionados.any { it }) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                mensajeValidacion =
                                    "Por favor, selecciona al menos un día de la semana."
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
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .width(180.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                        ) {
                            Text(
                                text = stringResource(R.string.boton_cancelar_modificaciones),
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarTimePicker) {
        TimePickerDialogMeditacion(
            initialTime = horaRecordatorio,
            onTimePicked = { horaRecordatorio = it },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogMeditacion(
    initialTime: LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimePicked(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            TimePicker(state = state, modifier = Modifier.fillMaxWidth())
        }
    )
}
