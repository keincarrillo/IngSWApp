package com.example.koalm.ui.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.Logo
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.theme.PrimaryColor
import com.example.koalm.ui.theme.SecondaryColor
import com.example.koalm.ui.theme.TertiaryMediumColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    navController: NavController,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = { mensajeValidacion = null }
        )
    }

    // Validación de correo
    var isValidEmail by remember { mutableStateOf(true) }
    var yaExisteCorreo by remember { mutableStateOf(false) }

    val dominiosPermitidos = listOf(
        "gmail.com", "hotmail.com", "yahoo.com", "icloud.com",
        "live.com", "outlook.com", "proton.me", "protonmail.com",
        "aol.com", "mail.com", "zoho.com", "yandex.com"
    )

    fun validarCorreoExistente(correo: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("email", correo)
            .get()
            .addOnSuccessListener { docs ->
                yaExisteCorreo = !docs.isEmpty
            }
            .addOnFailureListener {
                yaExisteCorreo = false
            }
    }

    // Username
    var username by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Logo(
                logoRes = R.drawable.greeting,
                contentDescription = "Pinguino Saludando"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoCorreo(
                value = email,
                esValido = isValidEmail,
                yaExiste = yaExisteCorreo,
                onValueChange = {
                    email = it
                    isValidEmail = it.contains("@") &&
                            !it.contains(" ") &&
                            dominiosPermitidos.any { domain -> it.endsWith("@$domain") }

                    if (isValidEmail) {
                        validarCorreoExistente(it)
                    } else {
                        yaExisteCorreo = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampoNombreUsuario(
                value = username,
                onValueChange = { nuevoValor ->
                    username = nuevoValor
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampoContrasena(
                value = password,
                visible = passwordVisible,
                onValueChange = { password = it },
                onToggle = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampoConfirmarContrasena(
                value = confirmPassword,
                visible = confirmPasswordVisible,
                coincideCon = confirmPassword == password,
                onValueChange = { confirmPassword = it },
                onToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(4.dp))

            CheckboxTerminos(
                navController = navController,
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            BotonesRegistro(
                email = email,
                username = username,
                password = password,
                confirmPassword = confirmPassword,
                isValidEmail = isValidEmail,
                yaExisteCorreo = yaExisteCorreo,
                termsAccepted = termsAccepted,
                navController = navController,
                context = context,
                onMensajeValidacionChange = { mensajeValidacion = it },
                onGoogleSignInClick = onGoogleSignInClick
            )

            TextoIrIniciarSesion(navController)
        }
    }
}

@Composable
fun CampoCorreo(
    value: String,
    esValido: Boolean,
    yaExiste: Boolean,
    onValueChange: (String) -> Unit
) {
    val mostrarError = (!esValido || yaExiste) && value.isNotEmpty()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Correo electrónico *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (!mostrarError) PrimaryColor else Color.Red,
            unfocusedBorderColor = if (!mostrarError) TertiaryMediumColor else Color.Red,
            focusedLabelColor = if (!mostrarError) PrimaryColor else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            when {
                yaExiste && value.isNotEmpty() -> {
                    Text("Este correo ya está en uso.", color = Color.Red, fontSize = 12.sp)
                }

                !esValido && value.isNotEmpty() -> {
                    Text("El formato del correo no es válido.", color = Color.Red, fontSize = 12.sp)
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            "Solo servicios de correo electrónico permitidos.",
                            color = TertiaryMediumColor,
                            fontSize = 12.sp
                        )
                        AyudaDominios()
                    }
                }
            }
        }
    )
}

@Composable
fun AyudaDominios() {
    var mostrarDialogo by remember { mutableStateOf(false) }

    IconButton(onClick = { mostrarDialogo = true }) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Ayuda",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Dominios permitidos") },
            text = {
                Text(
                    "Puedes usar correos de los siguientes dominios:\n\n" +
                            "• gmail.com\n" +
                            "• hotmail.com\n" +
                            "• outlook.com\n" +
                            "• icloud.com\n" +
                            "• proton.me\n" +
                            "• yahoo.com\n" +
                            "• live.com\n" +
                            "• protonmail.com\n" +
                            "• aol.com\n" +
                            "• mail.com\n" +
                            "• zoho.com\n" +
                            "• yandex.com\n"
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun CampoNombreUsuario(
    value: String,
    onValueChange: (String) -> Unit
) {
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
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) TertiaryMediumColor else Color.Red,
            focusedLabelColor = if (valido || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            when {
                value.all { it == ' ' } && value.length >= 3 -> {
                    Text(
                        text = "El nombre no puede solo contener espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                value.isBlank() -> {
                    Text(
                        text = "El nombre no puede estar vacío o solo contener espacios.",
                        color = TertiaryMediumColor,
                        fontSize = 12.sp
                    )
                }

                value.trim().length < 3 -> {
                    Text(
                        text = "Debe tener al menos 3 caracteres (excluyendo espacios).",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                !regex.matches(value) -> {
                    Text(
                        text = "Solo se permiten letras, números, guion bajo y espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                else -> {
                    Text(
                        text = "Nombre de usuario válido.",
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
fun CampoContrasena(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    val passwordValidationMessage = remember(value) {
        when {
            value.isEmpty() -> "La contraseña debe tener al menos 8 caracteres, una letra minúscula, una mayúsccula, un número y un carácter especial."
            value.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            !value.any { it.isLowerCase() } -> "Debe contener al menos una letra minúscula."
            !value.any { it.isUpperCase() } -> "Debe contener al menos una letra mayúscula."
            !value.any { it.isDigit() } -> "Debe contener al menos un número."
            !value.any { it in "!@#$%^&*()-_=+[{]}|;:,.<>?/`~" } -> "Debe contener al menos un carácter especial."
            else -> "Contraseña válida."
        }
    }

    val isValidPassword = passwordValidationMessage == "Contraseña válida."

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye else R.drawable.ic_eye_closed
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValidPassword || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedBorderColor = if (isValidPassword || value.isEmpty()) TertiaryMediumColor else Color.Red,
            focusedLabelColor = if (isValidPassword || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            val color =
                if (!isValidPassword && value.isNotEmpty()) Color.Red else TertiaryMediumColor

            Text(
                text = passwordValidationMessage,
                color = color,
                fontSize = 12.sp
            )
        }
    )
}

@Composable
fun CampoConfirmarContrasena(
    value: String,
    visible: Boolean,
    coincideCon: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Confirmar contraseña *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye else R.drawable.ic_eye_closed
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (coincideCon || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedBorderColor = if (coincideCon || value.isEmpty()) TertiaryMediumColor else Color.Red,
            focusedLabelColor = if (coincideCon || value.isEmpty()) PrimaryColor else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            if (value.isEmpty()) {
                Text(
                    "Las contraseñas deben coincidir.",
                    color = TertiaryMediumColor,
                    fontSize = 12.sp
                )
            }

            if (!coincideCon && value.isNotEmpty()) {
                Text(
                    text = "Las contraseñas introducidas no coinciden.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            if (coincideCon && value.isNotEmpty()) {
                Text(
                    text = "Las contraseñas coinciden.",
                    color = TertiaryMediumColor,
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
fun CheckboxTerminos(
    checked: Boolean,
    navController: NavController,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.97f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryColor,
                uncheckedColor = TertiaryMediumColor
            )
        )
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = SecondaryColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Acepto los términos y condiciones")
                }
            },
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                navController.navigate("TyC")
            }
        )
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BotonesRegistro(
    email: String,
    username: String,
    password: String,
    confirmPassword: String,
    isValidEmail: Boolean,
    yaExisteCorreo: Boolean,
    termsAccepted: Boolean,
    navController: NavController,
    context: Context,
    onMensajeValidacionChange: (String?) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val buttonModifier = Modifier.width(200.dp)
    var isLoading by remember { mutableStateOf(false) }
    val tag = "Registro"

    // Diálogo de éxito
    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Registro exitoso! Hemos enviado un enlace de verificación a tu correo electrónico.",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("iniciar") {
                    popUpTo("registro") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    // Diálogo de fallo
    var mostrarDialogoFallo by remember { mutableStateOf(false) }
    if (mostrarDialogoFallo) {
        FalloDialogoGuardadoAnimado(
            mensaje = "Hubo un error al enviar el enlace de verificación.",
            onDismiss = { mostrarDialogoFallo = false }
        )
    }

    Button(
        onClick = {
            android.util.Log.d(tag, "Iniciando proceso de registro")
            android.util.Log.d(tag, "Email: $email")
            android.util.Log.d(tag, "Username: $username")

            val passwordRegex =
                Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()\\-_=+\\[{\\]}|;:,.<>?/`~]).{8,}$")
            val usernameRegex = "^[a-zA-Z0-9_ ]*$".toRegex()
            val limpio = username.filter { it.code != 8203 }
            val usernameValido = limpio.isNotBlank() &&
                    limpio.trim().length >= 3 &&
                    usernameRegex.matches(limpio)

            when {
                email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    onMensajeValidacionChange("Por favor completa todos los campos.")
                }

                !isValidEmail -> {
                    onMensajeValidacionChange("El formato del correo no es válido.")
                }

                yaExisteCorreo -> {
                    onMensajeValidacionChange("El correo electrónico ya está en uso.")
                }

                !usernameValido -> {
                    onMensajeValidacionChange("El nombre de usuario no cumple con los requisitos.")
                }

                !passwordRegex.matches(password) -> {
                    onMensajeValidacionChange("La contraseña no cumple con los requisitos.")
                }

                password != confirmPassword -> {
                    onMensajeValidacionChange("Las contraseñas no coinciden.")
                }

                !termsAccepted -> {
                    onMensajeValidacionChange("Debes aceptar los términos y condiciones.")
                }

                else -> {
                    isLoading = true

                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val auth = FirebaseAuth.getInstance()
                                val userId = auth.currentUser!!.uid

                                val uLogin = Usuario(
                                    userId = userId,
                                    email = email,
                                    username = username
                                )

                                val db = FirebaseFirestore.getInstance()
                                db.collection("usuarios")
                                    .document(email)
                                    .set(uLogin.toMap(), SetOptions.merge())
                                    .addOnSuccessListener {
                                        auth.currentUser
                                            ?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                isLoading = false
                                                if (verifyTask.isSuccessful) {
                                                    mostrarDialogoExito = true
                                                } else {
                                                    mostrarDialogoFallo = true
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Error guardando usuario: ${e.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Error al crear usuario: ${task.exception?.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        },
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Registrar", color = MaterialTheme.colorScheme.onPrimary)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun TextoIrIniciarSesion(navController: NavController) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append("¿Ya tienes una cuenta? ")
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Iniciar sesión")
            }
        },
        fontSize = 14.sp,
        modifier = Modifier
            .padding(30.dp)
            .clickable {
                navController.navigate("iniciar")
            }
    )
}
