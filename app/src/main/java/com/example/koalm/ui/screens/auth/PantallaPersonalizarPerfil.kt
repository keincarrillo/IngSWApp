package com.example.koalm.ui.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.theme.BrandPrimaryColor
import com.example.koalm.ui.theme.LightErrorColor
import com.example.koalm.ui.theme.PrimaryColor
import com.example.koalm.ui.theme.TertiaryColor
import com.example.koalm.ui.theme.TertiaryMediumColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizarPerfil(navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechasec by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val opcionesGenero = listOf("Masculino", "Femenino", "Prefiero no decirlo")
    var username by remember { mutableStateOf("") }

    var perfilGuardado by remember { mutableStateOf(false) }

    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Perfil guardado correctamente!",
            onDismiss = {
                mostrarDialogoExito = false
                perfilGuardado = true
                navController.navigate("menu") {
                    popUpTo("personalizar") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val email = auth.currentUser?.email
    val db = FirebaseFirestore.getInstance()

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagenBase64 by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            val base64Comprimido = uriToCompressedBase64(context, it)

            if (base64Comprimido != null) {
                imagenBase64 = base64Comprimido
            } else {
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(email) {
        if (email != null) {
            db.collection("usuarios")
                .document(email)
                .get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username").orEmpty()
                    val u = doc.toObject(Usuario::class.java)
                    u?.let {
                        imagenBase64 = it.imagenBase64.orEmpty()
                        nombre = it.nombre.orEmpty()
                        apellidos = it.apellido.orEmpty()
                        fechasec = it.nacimiento.orEmpty()
                        peso = it.peso?.toString().orEmpty()
                        altura = it.altura?.toString().orEmpty()
                        generoSeleccionado = it.genero.orEmpty()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error cargando perfil: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    var validacionesDialogo by remember { mutableStateOf(false) }
    if (validacionesDialogo) {
        ValidacionesDialogoAnimado(
            mensaje = "Por favor, completa todos los campos para continuar",
            onDismiss = { validacionesDialogo = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personalizar perfil",
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) colorScheme.surface else TertiaryColor,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (perfilGuardado) {
                BarraNavegacionInferior(
                    navController = navController,
                    rutaActual = "personalizar"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ImagenUsuario(
                imagenBase64 = imagenBase64,
                onEditClick = { launcher.launch("image/*") },
                onDeleteClick = {
                    imagenUri = null
                    imagenBase64 = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CampoUsuario(username) { username = it }
            CampoNombre(nombre) { nombre = it }
            CampoApellidos(apellidos) { apellidos = it }

            CampoFechaNacimiento(fechasec) { showDatePicker = true }

            FechaNacimientoSelector(
                showDatePicker = showDatePicker,
                onDismiss = { showDatePicker = false },
                onValidDateSelected = { fechaSeleccionada ->
                    fechasec = fechaSeleccionada
                    showDatePicker = false
                }
            )

            CampoPeso(peso) { peso = it }
            CampoAltura(altura) { altura = it }
            SelectorGenero(opcionesGenero, generoSeleccionado) { generoSeleccionado = it }

            Spacer(modifier = Modifier.weight(1f))

            BotonGuardarPerfil {
                if (uid != null) {
                    if (email == null) {
                        Toast.makeText(
                            context,
                            "No se pudo obtener el correo",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@BotonGuardarPerfil
                    }

                    val regex = "^[a-zA-Z0-9_ ]*$".toRegex()

                    if (
                        username.isBlank() ||
                        !regex.matches(username) ||
                        username.trim().length < 3 ||
                        nombre.isBlank() ||
                        apellidos.isBlank() ||
                        fechasec.isEmpty() ||
                        peso.isEmpty() ||
                        peso.toFloatOrNull() == null ||
                        altura.isEmpty() ||
                        altura.toIntOrNull() == null ||
                        generoSeleccionado.isEmpty()
                    ) {
                        validacionesDialogo = true
                        return@BotonGuardarPerfil
                    }

                    val usuario = Usuario(
                        imagenBase64 = imagenBase64,
                        userId = uid,
                        email = email,
                        username = username,
                        nombre = nombre,
                        apellido = apellidos,
                        nacimiento = fechasec,
                        peso = String.format(
                            Locale.US,
                            "%.2f",
                            peso.toFloatOrNull() ?: 0f
                        ).toFloat(),
                        altura = altura.toIntOrNull()?.takeIf { it in 1..999 },
                        genero = generoSeleccionado
                    )

                    val fechaHoy: String = LocalDate
                        .now()
                        .format(
                            DateTimeFormatter.ofPattern(
                                "d MMMM, yyyy",
                                Locale("es", "MX")
                            )
                        )

                    val mapOriginal: Map<String, Any?> = usuario.toMap()
                    val dataUsuario: MutableMap<String, Any> = mapOriginal
                        .filterValues { it != null }
                        .mapValues { it.value as Any }
                        .toMutableMap()

                    dataUsuario["fechaCreacion"] = fechaHoy

                    db.collection("usuarios")
                        .document(email)
                        .set(dataUsuario, SetOptions.merge())
                        .addOnSuccessListener {
                            mostrarDialogoExito = true
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error al guardar: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------- Helpers visuales / campos ----------

fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ImagenUsuario(
    imagenBase64: String?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val tint = if (isDark) Color.White else BrandPrimaryColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentSize()
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
        ) {
            if (!imagenBase64.isNullOrEmpty()) {
                val bitmap = remember(imagenBase64) { base64ToBitmap(imagenBase64) }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Usuario",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Usuario",
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(tint)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Usuario",
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(tint)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Editar foto",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(LightErrorColor, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Eliminar foto",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun CampoUsuario(value: String, onValueChange: (String) -> Unit) {
    val regex = "^[a-zA-Z0-9_ ]*$".toRegex()
    val limpio = value.filter { it.code != 8203 }

    val valido = limpio.isNotBlank() &&
            limpio.trim().length >= 3 &&
            regex.matches(limpio)

    OutlinedTextField(
        value = value,
        onValueChange = { nuevoTexto ->
            if (nuevoTexto.matches(regex)) {
                onValueChange(nuevoTexto)
            }
        },
        label = { Text("Nombre de usuario *") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido) PrimaryColor else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) TertiaryMediumColor else Color.Red,
            focusedLabelColor = if (valido) PrimaryColor else Color.Red,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            when {
                value.isBlank() -> {
                    Text(
                        "El nombre no puede estar vacío o solo contener espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                value.trim().length < 3 -> {
                    Text(
                        "Debe tener al menos 3 caracteres (excluyendo espacios).",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                !regex.matches(value) -> {
                    Text(
                        "Solo se permiten letras, números, guion bajo y espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                else -> {
                    Text(
                        "Nombre de usuario válido.",
                        color = TertiaryMediumColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoNombre(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isLetter() || it.isWhitespace() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
        label = { Text("Nombre *") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoApellidos(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isLetter() || it.isWhitespace() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
        label = { Text("Apellidos *") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaNacimientoSelector(
    showDatePicker: Boolean,
    onDismiss: () -> Unit,
    onValidDateSelected: (String) -> Unit
) {
    if (!showDatePicker) return

    val datePickerState = rememberDatePickerState()

    var mostrarDialogoFallo by remember { mutableStateOf(false) }
    if (mostrarDialogoFallo) {
        FalloDialogoGuardadoAnimado(
            mensaje = "La edad calculada no es válida para continuar. Debes tener más de 12 años.",
            onDismiss = { mostrarDialogoFallo = false }
        )
    }

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val millisUTC = datePickerState.selectedDateMillis
                    if (millisUTC != null) {
                        val offset = TimeZone.getDefault().getOffset(millisUTC)
                        val correctedMillis = millisUTC + offset

                        val cal = Calendar.getInstance().apply { timeInMillis = correctedMillis }
                        val current = Calendar.getInstance()

                        val age = current.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
                        val cumpleEdad =
                            age > 12 || (age == 12 &&
                                    (cal.get(Calendar.MONTH) < current.get(Calendar.MONTH) ||
                                            (cal.get(Calendar.MONTH) == current.get(Calendar.MONTH) &&
                                                    cal.get(Calendar.DAY_OF_MONTH) <= current.get(
                                                Calendar.DAY_OF_MONTH
                                            )
                                                    )
                                            )
                                    )

                        if (cumpleEdad) {
                            val fecha = String.format(
                                Locale("es", "MX"),
                                "%02d/%02d/%04d",
                                cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.YEAR)
                            )
                            onValidDateSelected(fecha)
                            onDismiss()
                        } else {
                            mostrarDialogoFallo = true
                        }
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = PrimaryColor,
                todayDateBorderColor = PrimaryColor
            )
        )
    }
}

@Composable
fun CampoFechaNacimiento(value: String, onClick: () -> Unit) {
    val iconTint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text("Fecha de nacimiento *") },
        placeholder = { Text("MM/DD/YYYY") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Seleccionar fecha",
                tint = iconTint,
                modifier = Modifier.clickable { onClick() }
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoPeso(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val regex = Regex("^\\d{0,3}(\\.\\d{0,2})?$")
            if (newValue.isEmpty() || newValue.matches(regex)) {
                onValueChange(newValue)
            }
        },
        label = { Text("Peso *") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        trailingIcon = { Text("kg", color = TertiaryMediumColor) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoAltura(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isDigit() }.take(3)
    OutlinedTextField(
        value = filtered,
        onValueChange = {
            val newValue = it.filter { c -> c.isDigit() }.take(3)
            onValueChange(newValue)
        },
        label = { Text("Altura *") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        trailingIcon = { Text("cm", color = TertiaryMediumColor) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun BotonGuardarPerfil(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    ) {
        Text("Guardar", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun SelectorGenero(
    opciones: List<String>,
    seleccion: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Género *",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            opciones.forEach { opcionCompleta ->
                val abreviatura = when (opcionCompleta) {
                    "Masculino" -> "M"
                    "Femenino" -> "F"
                    "Prefiero no decirlo" -> "Prefiero no decirlo"
                    else -> opcionCompleta.firstOrNull()?.toString().orEmpty()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    RadioButton(
                        selected = opcionCompleta == seleccion,
                        onClick = { onSelect(opcionCompleta) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PrimaryColor,
                            unselectedColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = abreviatura,
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

fun uriToCompressedBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) return null

        val maxWidth = 600
        val maxHeight = 600
        val ratioBitmap = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        val resizedBitmap =
            Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)

        val outputStream = java.io.ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArrays = outputStream.toByteArray()

        Base64.encodeToString(byteArrays, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
