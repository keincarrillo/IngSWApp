package com.example.koalm.ui.screens.habitos.personalizados

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.Recordatorios
import com.example.koalm.services.notifications.NotificationScheduler
import com.example.koalm.services.timers.NotificationReceiverPers
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.SelectorDeIconoDialog
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.example.koalm.ui.screens.habitos.saludMental.HoraField
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.example.koalm.ui.theme.PrimaryColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigurarHabitoPersonalizado(
    navController: NavHostController,
    nombreHabitoEditar: String? = null
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Colores adaptados a tema claro/oscuro
    val cardContainerColor =
        if (isDark) MaterialTheme.colorScheme.surface else ContainerColor
    val cardBorderColor =
        if (isDark) MaterialTheme.colorScheme.outlineVariant else BorderColor

    val idHabitoOriginal = nombreHabitoEditar?.replace(" ", "_")
    val esEdicion = nombreHabitoEditar != null

    var nombreHabito by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var horaRecordatorio by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }
    var modoFecha by remember { mutableStateOf(true) }
    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var diasDuracion by remember { mutableStateOf("") }
    var mostrarSelectorIconos by remember { mutableStateOf(false) }
    var iconoSeleccionado by remember { mutableStateOf("") }

    var mostrarSelectorColor by remember { mutableStateOf(false) }
    var colorSeleccionado by remember { mutableStateOf(Color(0xFFF6FBF2)) }

    var recordatorioActivo by remember { mutableStateOf(false) }
    var frecuenciaActivo by remember { mutableStateOf(false) }
    var finalizarActivo by remember { mutableStateOf(false) }

    val horarios = remember { mutableStateListOf<LocalTime>() }
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }

    val habitoOriginal = remember { mutableStateOf<HabitoPersonalizado?>(null) }
    val progresoHabitoOriginal = remember { mutableStateOf<ProgresoDiario?>(null) }

    val esLunes = LocalDate.now().dayOfWeek == DayOfWeek.MONDAY
    val objetivoHabilitado = !esEdicion || esLunes

    var objetivoDiario by remember { mutableStateOf(1) }
    val objetivoDiarioOriginal = progresoHabitoOriginal.value?.totalObjetivoDiario ?: 1
    val diasOriginales = progresoHabitoOriginal.value?.frecuencia ?: List(7) { false }
    val modificoObjetivo = objetivoDiario != objetivoDiarioOriginal
    val modificoFrecuencia = diasSeleccionados != diasOriginales

    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito guardado con éxito!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigateUp()
            }
        )
    }

    var mostrarDialogoFalloFecha by remember { mutableStateOf(false) }
    if (mostrarDialogoFalloFecha) {
        FalloDialogoGuardadoAnimado(
            mensaje = "Selecciona una fecha válida.",
            onDismiss = {
                mostrarDialogoFalloFecha = false
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

    var validacionesDialogo by remember { mutableStateOf(false) }
    if (validacionesDialogo) {
        ValidacionesDialogoAnimado(
            mensaje = "¡Ups! Los campos Nombre del hábito, Objetivo diario y Frecuencia son obligatorios.\n" +
                    "Asegúrate de completarlos para continuar",
            onDismiss = {
                validacionesDialogo = false
            }
        )
    }

    var modoPersonalizado by remember { mutableStateOf(true) }

    // Color del icono: un poquito más oscuro que el color seleccionado
    val colorIcono = colorSeleccionado.darken(0.15f)
    var nombreError by remember { mutableStateOf(false) }

    // Si nombreHabito NO es null, editamos y cargamos los datos
    LaunchedEffect(nombreHabitoEditar) {
        if (nombreHabitoEditar == null) return@LaunchedEffect

        val habitoExistente = cargarHabitoPorNombre(nombreHabitoEditar)
        val progresoHabitoExistente = cargarProgresoHabitoPorNombre(nombreHabitoEditar)

        if (habitoExistente == null) {
            Toast.makeText(context, "No se encontró el hábito para editar", Toast.LENGTH_SHORT)
                .show()
            navController.navigateUp()
            return@LaunchedEffect
        }

        habitoOriginal.value = habitoExistente
        progresoHabitoOriginal.value = progresoHabitoExistente

        // Asignaciones seguras
        nombreHabito = habitoExistente.nombre
        descripcion = habitoExistente.descripcion
        iconoSeleccionado = habitoExistente.iconoEtiqueta
        colorSeleccionado = parseColorFromFirebase(habitoExistente.colorEtiqueta)

        // Objetivo
        objetivoDiario = habitoExistente.objetivoDiario

        // Frecuencia (días seleccionados)
        diasSeleccionados = habitoExistente.frecuencia ?: List(7) { false }
        frecuenciaActivo = habitoExistente.frecuencia?.any { it } == true

        // Recordatorios
        recordatorioActivo = !habitoExistente.recordatorios?.horas.isNullOrEmpty()
        horarios.clear()
        habitoExistente.recordatorios?.horas?.forEach { horaStr ->
            try {
                horarios.add(LocalTime.parse(horaStr))
            } catch (e: Exception) {
                Log.e("ParseError", "Hora inválida: $horaStr")
            }
        }

        // Modo fin y duración
        modoFecha = habitoExistente.modoFin == "calendario"
        if (modoFecha) {
            habitoExistente.fechaFin?.let {
                try {
                    fechaSeleccionada = LocalDate.parse(it)
                } catch (e: Exception) {
                    Log.e("ParseError", "FechaFin inválida: $it")
                }
            }
        } else {
            diasDuracion =
                if (!habitoExistente.fechaInicio.isNullOrBlank() && !habitoExistente.fechaFin.isNullOrBlank()) {
                    try {
                        val inicio = LocalDate.parse(habitoExistente.fechaInicio)
                        val fin = LocalDate.parse(habitoExistente.fechaFin)
                        ChronoUnit.DAYS.between(inicio, fin).toString()
                    } catch (e: Exception) {
                        Log.e("ParseError", "Error calculando duración")
                        ""
                    }
                } else ""
        }

        // Fecha de inicio (solo si no es modo calendario)
        if (!modoFecha) {
            habitoExistente.fechaInicio?.let {
                try {
                    fechaSeleccionada = LocalDate.parse(it)
                } catch (e: Exception) {
                    Log.e("ParseError", "FechaInicio inválida: $it")
                }
            }
        }

        finalizarActivo =
            habitoExistente.fechaFin != null && habitoExistente.fechaFin.lowercase() != "null"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_habito_personalizado)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "configurar_habito") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                border = BorderStroke(1.dp, cardBorderColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_vista_previa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nombreHabito,
                        onValueChange = { nuevoValor ->
                            val regex = Regex("^[a-zA-Z0-9][a-zA-Z0-9 ]*\$")

                            val valorFiltrado =
                                if (nuevoValor.length > 20) nuevoValor.take(20) else nuevoValor

                            val esValidoRegex =
                                valorFiltrado.isEmpty() || regex.matches(valorFiltrado)

                            if (esValidoRegex) {
                                nombreHabito = valorFiltrado
                            } else {
                                nombreError = true
                            }
                        },
                        label = { Text(stringResource(R.string.label_nombre_habito)) },
                        isError = nombreError,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !esEdicion,
                        supportingText = {
                            Text("${nombreHabito.length}/20 caracteres")
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mostrarSelectorColor = true },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Color")
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .background(colorSeleccionado, CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                            )
                        }

                        OutlinedButton(
                            onClick = { mostrarSelectorIconos = true },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Icono")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = obtenerIconoPorNombre(iconoSeleccionado),
                                contentDescription = null,
                                tint = colorIcono
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = {
                            Text(
                                "Ejemplo: Quiero ir al gimnasio para sentirme con más energía, mejorar mi salud y aumentar mi autoestima.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = stringResource(R.string.label_configuracion_habito),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (esEdicion && !esLunes) {
                        Text(
                            text = "La configuración del hábito solo se pueden modificar cada lunes para comenzar la semana con nuevos objetivos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp),
                            textAlign = TextAlign.Justify
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_objetivo_texto),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        TooltipDialogAyuda(
                            titulo = "Objetivo Diario",
                            mensaje = "Establece la cantidad de veces que quieres realizar este hábito cada día para ayudarte a mantener un seguimiento efectivo."
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (objetivoDiario == 0) "" else objetivoDiario.toString(),
                            onValueChange = { nuevoTexto ->
                                nuevoTexto.toIntOrNull()?.let {
                                    if (it > 0) objetivoDiario = it
                                }
                                if (nuevoTexto.isEmpty()) objetivoDiario = 0
                            },
                            enabled = objetivoHabilitado,
                            modifier = Modifier.width(90.dp),
                            singleLine = true,
                            label = { Text("Num.") },
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        AnimatedContent(
                            targetState = objetivoDiario,
                            label = "CambioTexto"
                        ) { valor ->
                            Text(
                                text = if (valor == 1)
                                    stringResource(R.string.label_objetivo_diarioDef)
                                else
                                    stringResource(R.string.label_objetivo_diario)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_frecuencia_P),
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("L", "M", "X", "J", "V", "S", "D").forEachIndexed { index, dia ->
                                DiaCircle(
                                    label = dia,
                                    selected = diasSeleccionados[index],
                                    enabled = !esEdicion || esLunes
                                ) {
                                    diasSeleccionados =
                                        diasSeleccionados.toMutableList().also { it[index] = !it[index] }
                                }
                            }
                        }
                        DiasSeleccionadosResumen(diasSeleccionados)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = stringResource(R.string.label_configuracion_adicional),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_switch_activar_recordatorio),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Recordatorios",
                            mensaje = "Al activar esta opción, puedes establecer notificaciones personalizadas o automáticas para ayudarte a cumplir tu hábito."
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = recordatorioActivo,
                            onCheckedChange = {
                                recordatorioActivo = it
                                if (!it) {
                                    horarios.clear()
                                    modoPersonalizado = true
                                }
                            },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    if (recordatorioActivo) {
                        Text(
                            text = stringResource(R.string.label_tipo_notificacion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { modoPersonalizado = true },
                                border = BorderStroke(1.dp, cardBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (modoPersonalizado) {
                                        MaterialTheme.colorScheme.primary
                                    } else Color.Transparent,
                                    contentColor = if (modoPersonalizado) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = stringResource(R.string.boton_Personalizado))
                            }

                            OutlinedButton(
                                onClick = { modoPersonalizado = false },
                                border = BorderStroke(1.dp, cardBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!modoPersonalizado) {
                                        MaterialTheme.colorScheme.primary
                                    } else Color.Transparent,
                                    contentColor = if (!modoPersonalizado) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = stringResource(R.string.boton_Automatico))
                            }
                        }

                        AnimatedVisibility(visible = modoPersonalizado) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Configura tus recordatorios a la hora que prefieras.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.widthIn(max = 300.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        AnimatedVisibility(visible = !modoPersonalizado) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "La aplicación te enviará recordatorios automáticamente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.widthIn(max = 300.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (modoPersonalizado) {
                            Text(
                                text = stringResource(R.string.label_notificacion_personalizada),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )

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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_finaliza_el),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Finalizar hábito",
                            mensaje = "Al activar esta opción, puedes establecer cuándo deseas finalizar este hábito."
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = finalizarActivo,
                            onCheckedChange = {
                                finalizarActivo = it
                                if (!it) {
                                    fechaSeleccionada = null
                                    diasDuracion = ""
                                    modoFecha = true
                                }
                            },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    if (finalizarActivo) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { modoFecha = true },
                                border = BorderStroke(1.dp, cardBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (modoFecha) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (modoFecha) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_fecha))
                            }

                            OutlinedButton(
                                onClick = { modoFecha = false },
                                border = BorderStroke(1.dp, cardBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!modoFecha) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!modoFecha) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_dias))
                            }
                        }

                        AnimatedVisibility(visible = modoFecha) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Selecciona una fecha específica para finalizar el hábito.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.widthIn(max = 300.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        AnimatedVisibility(visible = !modoFecha) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Indica cuántos días deseas mantener este hábito a partir de hoy.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.widthIn(max = 300.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (modoFecha) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.label_ultimo_dia),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { mostrarDatePicker = true },
                                    border = BorderStroke(1.dp, cardBorderColor),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        fechaSeleccionada?.format(
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                        )
                                            ?: stringResource(R.string.boton_seleccionar_fecha),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.label_despues_dias),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = diasDuracion,
                                    onValueChange = { input ->
                                        val number = input.toIntOrNull()
                                        if (number != null && number >= 1) {
                                            diasDuracion = input
                                        } else if (input.isEmpty()) {
                                            diasDuracion = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(60.dp),
                                    singleLine = true,
                                    label = { Text("Num.") },
                                    shape = RoundedCornerShape(50),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                AnimatedContent(
                                    targetState = diasDuracion.toIntOrNull() ?: 0,
                                    label = "CambioTexto"
                                ) { valor ->
                                    Text(
                                        text = if (valor == 1)
                                            stringResource(R.string.label_dia)
                                        else
                                            stringResource(R.string.label_dias)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // === BOTONES INFERIORES: MISMO TAMAÑO CUANDO ES EDICIÓN ===
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
                        // GUARDAR (izquierda)
                        Button(
                            onClick = {
                                val frecuenciaDias = diasSeleccionados.count { it }

                                if (nombreHabito.isBlank() || objetivoDiario <= 0 || frecuenciaDias <= 0) {
                                    validacionesDialogo = true
                                } else {
                                    val userEmail =
                                        FirebaseAuth.getInstance().currentUser?.email
                                    val db = FirebaseFirestore.getInstance()

                                    if (userEmail != null) {
                                        val userHabitsRef = db.collection("habitos")
                                            .document(userEmail)
                                            .collection("personalizados")

                                        // Control de racha
                                        if (esEdicion && modificoObjetivo && progresoHabitoOriginal.value?.completado != false) {
                                            habitoOriginal.value = habitoOriginal.value?.copy(
                                                rachaActual = maxOf(
                                                    0,
                                                    (habitoOriginal.value?.rachaActual ?: 1) - 1
                                                )
                                            )
                                        }

                                        if (esEdicion && modificoObjetivo && progresoHabitoOriginal.value?.completado != false) {
                                            habitoOriginal.value = habitoOriginal.value?.copy(
                                                rachaMaxima = maxOf(
                                                    0,
                                                    (habitoOriginal.value?.rachaMaxima ?: 1) - 1
                                                )
                                            )
                                        }

                                        val habitoPersonalizado = HabitoPersonalizado(
                                            nombre = nombreHabito,
                                            colorEtiqueta = colorSeleccionado.toString(),
                                            iconoEtiqueta = iconoSeleccionado.toString(),
                                            descripcion = descripcion,
                                            objetivoDiario = objetivoDiario,
                                            frecuencia = diasSeleccionados,
                                            recordatorios = Recordatorios(
                                                tipo = if (modoPersonalizado) "personalizado" else "automatico",
                                                horas = if (modoPersonalizado)
                                                    horarios.map {
                                                        it.format(
                                                            DateTimeFormatter.ofPattern(
                                                                "HH:mm"
                                                            )
                                                        )
                                                    }
                                                else
                                                    HabitoPersonalizado.generarHorasAutomaticas()
                                            ),
                                            fechaInicio = if (esEdicion) habitoOriginal.value?.fechaInicio
                                            else HabitoPersonalizado.calcularFechaInicio(),
                                            fechaFin = HabitoPersonalizado.calcularFechaFin(
                                                modoFecha,
                                                fechaSeleccionada.toString(), diasDuracion
                                            ),
                                            modoFin = if (modoFecha) "calendario" else "dias",
                                            rachaActual = if (!esEdicion) {
                                                0
                                            } else {
                                                habitoOriginal.value?.rachaActual ?: 0
                                            },
                                            rachaMaxima = if (!esEdicion) {
                                                0
                                            } else {
                                                habitoOriginal.value?.rachaMaxima ?: 0
                                            },
                                            ultimoDiaCompletado = if (esEdicion) habitoOriginal.value?.ultimoDiaCompletado else null,
                                            estaActivo = true
                                        )

                                        val habitoMap = habitoPersonalizado.toMap()

                                        val habitoId = if (idHabitoOriginal != null) {
                                            idHabitoOriginal
                                        } else {
                                            nombreHabito.replace(" ", "_")
                                        }

                                        val isEditing = (idHabitoOriginal != null)
                                        val habitIdToUse = idHabitoOriginal ?: habitoId

                                        // Cancelar recordatorios antiguos si es edición
                                        if (isEditing) {
                                            val originalReminderCount =
                                                habitoOriginal.value?.recordatorios?.horas?.size
                                                    ?: 0

                                            for (index in 0 until originalReminderCount) {
                                                NotificationScheduler.cancelHabitReminder(
                                                    context = context,
                                                    habitId = habitIdToUse,
                                                    reminderIndex = index,
                                                )
                                            }
                                        }

                                        userHabitsRef.document(habitIdToUse)
                                            .set(habitoMap)
                                            .addOnSuccessListener {
                                                mostrarDialogoExito = true

                                                val listaHorarios = if (modoPersonalizado) {
                                                    horarios
                                                } else {
                                                    HabitoPersonalizado.generarHorasAutomaticas()
                                                        .map {
                                                            LocalTime.parse(
                                                                it,
                                                                DateTimeFormatter.ofPattern("HH:mm")
                                                            )
                                                        }
                                                }
                                                listaHorarios.forEachIndexed { index, horario ->
                                                    NotificationScheduler.scheduleHabitReminder(
                                                        context = context,
                                                        habitId = habitIdToUse,
                                                        habitName = nombreHabito,
                                                        hour = horario.hour,
                                                        minute = horario.minute,
                                                        reminderIndex = index,
                                                        frecuencia = diasSeleccionados
                                                    )
                                                }

                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Error al guardar el hábito: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                navController.navigateUp()
                                            }

                                        val progreso = ProgresoDiario(
                                            realizados = if (!esEdicion) {
                                                0
                                            } else if (modificoObjetivo || modificoFrecuencia) {
                                                0
                                            } else {
                                                progresoHabitoOriginal.value?.realizados ?: 0
                                            },
                                            completado = if (!esEdicion) {
                                                false
                                            } else if (modificoObjetivo || modificoFrecuencia) {
                                                false
                                            } else {
                                                progresoHabitoOriginal.value?.completado ?: false
                                            },
                                            totalObjetivoDiario = objetivoDiario,
                                            fecha = if (esEdicion) progresoHabitoOriginal.value?.fecha
                                                ?: LocalDate.now()
                                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            else LocalDate.now()
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                            frecuencia = diasSeleccionados
                                        )

                                        val progresoRef = userHabitsRef.document(habitoId)
                                            .collection("progreso")
                                            .document(
                                                LocalDate.now()
                                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            )

                                        progresoRef.set(progreso.toMap())
                                            .addOnSuccessListener {
                                                // OK, ya se guarda el progreso
                                            }
                                            .addOnFailureListener { _ ->
                                                navController.navigateUp()
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Usuario no autenticado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(.5f)
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

                        // CANCELAR (derecha, mismo tamaño)
                        Button(
                            onClick = {
                                navController.navigateUp()
                            },
                            modifier = Modifier
                                .weight(.5f)
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                        ) {
                            Text(
                                stringResource(R.string.boton_cancelar_modificaciones),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // MODO CREACIÓN: solo botón Guardar a lo ancho
                        Button(
                            onClick = {
                                val frecuenciaDias = diasSeleccionados.count { it }

                                if (nombreHabito.isBlank() || objetivoDiario <= 0 || frecuenciaDias <= 0) {
                                    validacionesDialogo = true
                                } else {
                                    val userEmail =
                                        FirebaseAuth.getInstance().currentUser?.email
                                    val db = FirebaseFirestore.getInstance()

                                    if (userEmail != null) {
                                        val userHabitsRef = db.collection("habitos")
                                            .document(userEmail)
                                            .collection("personalizados")

                                        val habitoPersonalizado = HabitoPersonalizado(
                                            nombre = nombreHabito,
                                            colorEtiqueta = colorSeleccionado.toString(),
                                            iconoEtiqueta = iconoSeleccionado.toString(),
                                            descripcion = descripcion,
                                            objetivoDiario = objetivoDiario,
                                            frecuencia = diasSeleccionados,
                                            recordatorios = Recordatorios(
                                                tipo = if (modoPersonalizado) "personalizado" else "automatico",
                                                horas = if (modoPersonalizado)
                                                    horarios.map {
                                                        it.format(
                                                            DateTimeFormatter.ofPattern(
                                                                "HH:mm"
                                                            )
                                                        )
                                                    }
                                                else
                                                    HabitoPersonalizado.generarHorasAutomaticas()
                                            ),
                                            fechaInicio = HabitoPersonalizado.calcularFechaInicio(),
                                            fechaFin = HabitoPersonalizado.calcularFechaFin(
                                                modoFecha,
                                                fechaSeleccionada.toString(), diasDuracion
                                            ),
                                            modoFin = if (modoFecha) "calendario" else "dias",
                                            rachaActual = 0,
                                            rachaMaxima = 0,
                                            ultimoDiaCompletado = null,
                                            estaActivo = true
                                        )

                                        val habitoMap = habitoPersonalizado.toMap()

                                        val habitoId = nombreHabito.replace(" ", "_")
                                        val habitIdToUse = habitoId

                                        userHabitsRef.document(habitIdToUse)
                                            .set(habitoMap)
                                            .addOnSuccessListener {
                                                mostrarDialogoExito = true

                                                val listaHorarios = if (modoPersonalizado) {
                                                    horarios
                                                } else {
                                                    HabitoPersonalizado.generarHorasAutomaticas()
                                                        .map {
                                                            LocalTime.parse(
                                                                it,
                                                                DateTimeFormatter.ofPattern("HH:mm")
                                                            )
                                                        }
                                                }
                                                listaHorarios.forEachIndexed { index, horario ->
                                                    NotificationScheduler.scheduleHabitReminder(
                                                        context = context,
                                                        habitId = habitIdToUse,
                                                        habitName = nombreHabito,
                                                        hour = horario.hour,
                                                        minute = horario.minute,
                                                        reminderIndex = index,
                                                        frecuencia = diasSeleccionados
                                                    )
                                                }

                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Error al guardar el hábito: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                navController.navigateUp()
                                            }

                                        val progreso = ProgresoDiario(
                                            realizados = 0,
                                            completado = false,
                                            totalObjetivoDiario = objetivoDiario,
                                            fecha = LocalDate.now()
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                            frecuencia = diasSeleccionados
                                        )

                                        val progresoRef = userHabitsRef.document(habitoId)
                                            .collection("progreso")
                                            .document(
                                                LocalDate.now()
                                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            )

                                        progresoRef.set(progreso.toMap())
                                            .addOnSuccessListener {
                                                // OK
                                            }
                                            .addOnFailureListener { _ ->
                                                navController.navigateUp()
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Usuario no autenticado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }
                                }
                            },
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

    if (mostrarDatePicker) {
        val localeContext = remember { context.withLocale(Locale("es", "MX")) }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val millisHoy = today.timeInMillis

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = millisHoy
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (selected.timeInMillis >= millisHoy) {
                            val localDate =
                                LocalDate.ofEpochDay(millis / 86_400_000)
                            fechaSeleccionada = localDate
                            mostrarDatePicker = false
                        } else {
                            mostrarDialogoFalloFecha = true
                        }
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    if (mostrarSelectorIconos) {
        SelectorDeIconoDialog(
            iconoSeleccionadoNombre = iconoSeleccionado,
            onSeleccionar = { iconoSeleccionado = it },
            onCerrar = { mostrarSelectorIconos = false }
        )
    }

    if (mostrarSelectorColor) {
        val colores = listOf(
            Color(0xFFA5D6A7),
            Color(0xFF90CAF9),
            Color(0xFFFFCC80),
            Color(0xFFEF9A9A),
            Color(0xCEB39DDB),
            Color(0xFF80CBC4),
            Color(0xFFFFF59D),
            Color(0xFFD1C4E9),
            Color(0xFFB2EBF2),
            Color(0xFFFFAB91),
            Color(0xFFC5E1A5),
            Color(0xFF9FA8DA),
            Color(0xFFF0F4C3),
            Color(0xFFD7CCC8),
            Color(0xFFCFD8DC),
            Color(0xFFF8BBD0),
            Color(0xFFDCEDC8),
            Color(0xFFE1BEE7),
            Color(0xFFEF5350),
            Color(0xFF64B5F6)
        )

        AlertDialog(
            onDismissRequest = { mostrarSelectorColor = false },
            title = { Text("Selecciona un color") },
            text = {
                Column {
                    colores.chunked(5).forEach { fila ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            fila.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(color, CircleShape)
                                        .clickable {
                                            colorSeleccionado = color
                                            mostrarSelectorColor = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// Extensión para oscurecer el color
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Función para hacer el parseo de color desde FB
fun parseColorFromFirebase(
    colorString: String,
    darken: Boolean = false,
    darkenFactor: Float = 0.15f
): Color {
    val regex = Regex("""Color\(([\d.]+), ([\d.]+), ([\d.]+), ([\d.]+),.*\)""")
    val match = regex.find(colorString)
    return if (match != null) {
        val (r, g, b, a) = match.destructured
        val baseColor = Color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        if (darken) baseColor.darken(darkenFactor) else baseColor
    } else {
        Log.e("ColorParse", "No se pudo parsear el color: $colorString")
        Color.Gray
    }
}

fun Context.withLocale(locale: Locale): Context {
    val config = resources.configuration
    config.setLocale(locale)
    return createConfigurationContext(config)
}

suspend fun cargarHabitoPorNombre(nombreHabito: String): HabitoPersonalizado? {
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email ?: return null
    val idDocumento = nombreHabito.replace(" ", "_")
    val db = FirebaseFirestore.getInstance()

    return try {
        val snapshot = db.collection("habitos")
            .document(usuarioEmail)
            .collection("personalizados")
            .document(idDocumento)
            .get()
            .await()

        if (snapshot.exists()) {
            snapshot.toObject(HabitoPersonalizado::class.java)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error cargando hábito: ${e.message}")
        null
    }
}

suspend fun cargarProgresoHabitoPorNombre(nombreHabito: String): ProgresoDiario? {
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email ?: return null
    val idDocumento = nombreHabito.replace(" ", "_")
    val db = FirebaseFirestore.getInstance()
    val fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    return try {
        val progresoSnapshot = db.collection("habitos")
            .document(usuarioEmail)
            .collection("personalizados")
            .document(idDocumento)
            .collection("progreso")
            .document(fechaHoy)
            .get()
            .await()

        progresoSnapshot.toObject(ProgresoDiario::class.java)
    } catch (e: Exception) {
        Log.e("Firestore", "Error cargando progreso del hábito: ${e.message}")
        null
    }
}

@Composable
fun TooltipDialogAyuda(
    titulo: String,
    mensaje: String,
    icon: ImageVector = Icons.Default.Info
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    IconButton(onClick = { mostrarDialogo = true }) {
        Icon(
            imageVector = icon,
            contentDescription = "Ayuda",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            },
            text = {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogo = false },
                    shape = RoundedCornerShape(50),
                ) {
                    Text("Entendido")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

@Composable
fun DiasSeleccionadosResumen(diasSeleccionados: List<Boolean>) {
    val seleccionadosCount = diasSeleccionados.count { it }
    val totalDias = diasSeleccionados.size
    Text(
        text = "$seleccionadosCount/$totalDias días activos",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun DiaCircle(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val disabledBg = MaterialTheme.colorScheme.surfaceVariant
    val disabledBorder = MaterialTheme.colorScheme.outlineVariant
    val disabledText = MaterialTheme.colorScheme.onSurfaceVariant

    val targetBgColor = when {
        !enabled -> disabledBg
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val targetBorderColor = when {
        !enabled -> disabledBorder
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val textColor = when {
        !enabled -> disabledText
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        label = "backgroundColorAnimation"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        label = "borderColorAnimation"
    )

    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, animatedBorderColor, CircleShape)
            .background(animatedBgColor)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun HorarioItem(hora: String, onEditar: () -> Unit) {
    val accent = Color(0xFF4CAF50)

    androidx.compose.material3.Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .widthIn(max = 180.dp)
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = accent,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onEditar)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = hora,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Hora",
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
