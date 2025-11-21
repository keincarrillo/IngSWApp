package com.example.koalm.ui.screens.ajustes

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor
import com.example.koalm.ui.theme.PrimaryColor
import com.example.koalm.ui.theme.TertiaryMediumColor
import com.example.koalm.ui.theme.White
import com.example.koalm.ui.viewmodels.InicioSesionPreferences
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarContrasena(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cambiar contraseña",
                        color = colorScheme.onSurface
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
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MascotaCambiarContrasena()

            Spacer(modifier = Modifier.height(20.dp))

            ValidarContrasenaActual(navController = navController) {}
        }
    }
}

@Composable
fun MascotaCambiarContrasena() {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val circleBackground = if (isDark) {
        colorScheme.surfaceVariant
    } else {
        ContainerColor
    }

    val borderColor = if (isDark) {
        colorScheme.outlineVariant
    } else {
        BorderColor
    }

    Box(
        modifier = Modifier
            .size(320.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(circleBackground)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pinguino_contrasena),
                contentDescription = "Pinguino cambio de contraseña",
                modifier = Modifier
                    .fillMaxSize(0.95f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun CampoValidarContrasena(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu contraseña") },
        modifier = Modifier.fillMaxWidth(0.97f),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = TertiaryMediumColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun MensajeExplicacionCambio() {
    Text(
        text = "Te enviaremos un enlace de restablecimiento al correo asociado a tu cuenta.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 4.dp)
    )
}

@Suppress("DEPRECATION")
fun cerrarSesion(context: Context, navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    InicioSesionPreferences(context).reiniciarAnimacion()

    @Suppress("DEPRECATION")
    Identity.getSignInClient(context)
        .signOut()
        .addOnCompleteListener {
        }

    context.getSharedPreferences(
        context.getString(R.string.prefs_file),
        Context.MODE_PRIVATE
    ).edit {
        clear()
    }

    navController.navigate("iniciar") {
        popUpTo("menu") { inclusive = true }
    }
}

@Composable
fun ValidarContrasenaActual(
    navController: NavController,
    onValidacionExitosa: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val correo = user?.email ?: ""

    var contrasenaActual by remember { mutableStateOf("") }
    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var mostrarDialogoExito by remember { mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "Revisa tu bandeja de entrada; te enviamos el enlace a tu correo $correo",
            onDismiss = {
                mostrarDialogoExito = false
                cerrarSesion(context, navController)
            }
        )
    }

    CampoValidarContrasena(
        value = contrasenaActual,
        onValueChange = { contrasenaActual = it }
    )

    MensajeExplicacionCambio()

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (correo.isBlank()) {
                mensajeValidacion = "No se encontró correo del usuario actual"
                return@Button
            }
            if (contrasenaActual.isBlank()) {
                mensajeValidacion = "Ingresa tu contraseña actual"
                return@Button
            }

            isLoading = true
            auth.signInWithEmailAndPassword(correo, contrasenaActual)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        onValidacionExitosa()

                        val actionCodeSettings = ActionCodeSettings.newBuilder()
                            .setUrl("https://koalm-94491.web.app")
                            .setHandleCodeInApp(false)
                            .setAndroidPackageName("com.example.koalm", true, "35")
                            .build()

                        auth.sendPasswordResetEmail(correo, actionCodeSettings)
                            .addOnSuccessListener {
                                mostrarDialogoExito = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    it.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        mensajeValidacion = "La contraseña ingresada no es correcta"
                    }
                }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor,
            contentColor = White
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text("Validar contraseña", color = White)
        }
    }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = { mensajeValidacion = null }
        )
    }
}
