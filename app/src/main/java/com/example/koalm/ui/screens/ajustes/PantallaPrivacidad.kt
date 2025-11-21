/*PantallaPrivacidad.kt*/
package com.example.koalm.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.BorderColor
import com.example.koalm.ui.theme.ContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrivacidad(navController: NavHostController) {

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    // Colores de card y borde adaptados a modo claro/oscuro
    val cardBackground = if (isDark) {
        colorScheme.surface
    } else {
        ContainerColor
    }

    val cardBorderColor = if (isDark) {
        colorScheme.outlineVariant
    } else {
        BorderColor
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacidad",
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
        },
        containerColor = colorScheme.background
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
                colors = CardDefaults.cardColors(
                    containerColor = cardBackground
                )
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("El responsable del tratamiento de tus datos es el equipo de desarrollo de PinguBalance, una aplicación creada como proyecto de Ingeniería de Software enfocada en el bienestar físico y mental.\n\n")

                            append("Al utilizar PinguBalance, podríamos recopilar los siguientes tipos de información:\n\n")
                            append("• Datos personales básicos: nombre, correo electrónico y, en algunos casos, edad.\n")
                            append("• Datos de salud y actividad: horas de sueño, hábitos registrados, metas de pasos, hidratación y otros parámetros que tú decides registrar.\n")
                            append("• Geolocalización: solo si otorgas permiso explícito dentro de la app.\n")
                            append("• Datos técnicos: tipo de dispositivo, sistema operativo, versión de la app e identificadores anónimos para mejorar el servicio.\n\n")

                            append("Usamos tus datos para:\n\n")
                            append("• Mostrarte estadísticas y gráficas personalizadas sobre tus hábitos y progreso.\n")
                            append("• Ayudarte a llevar un seguimiento de tus rutinas y hábitos saludables.\n")
                            append("• Mejorar el rendimiento, estabilidad y seguridad de PinguBalance.\n")
                            append("• Comunicarnos contigo en caso de soporte técnico o avisos importantes.\n\n")
                            append("En ningún caso utilizaremos tus datos con fines publicitarios sin tu consentimiento explícito.\n\n")

                            append("Tu uso de la app implica tu consentimiento para el tratamiento de tus datos bajo los términos descritos en esta política. En el caso de datos sensibles (como información de salud o ubicación), siempre procuraremos solicitar tu autorización de forma clara.\n\n")

                            append("No compartimos tu información personal con terceros, salvo en estos casos:\n\n")
                            append("• Proveedores tecnológicos (por ejemplo, Firebase) que nos ayudan a almacenar datos y enviar notificaciones.\n")
                            append("• Obligaciones legales o requerimientos de una autoridad competente.\n")
                            append("En todos los casos, buscamos que estos proveedores cumplan con estándares adecuados de privacidad.\n\n")

                            append("Como titular de tus datos, tienes derecho a:\n\n")
                            append("• Acceder a tus datos personales.\n")
                            append("• Rectificar información incorrecta o desactualizada.\n")
                            append("• Solicitar la eliminación de tus datos si ya no deseas que sean tratados.\n")
                            append("• Oponerte a ciertos usos de tu información.\n\n")
                            append("Para ejercer estos derechos puedes escribirnos a: adslatex@gmail.com\n\n")

                            append("Tomamos medidas técnicas y organizativas para proteger tu información, como:\n\n")
                            append("• Uso de servicios con cifrado y autenticación segura.\n")
                            append("• Copias de seguridad periódicas.\n")
                            append("• Restricción de acceso a la información solo al equipo necesario.\n\n")

                            append("Conservamos tus datos únicamente el tiempo necesario para cumplir con las finalidades de la app. Puedes solicitar la eliminación de tu cuenta y datos cuando lo desees.\n\n")

                            append("Esta Política de Privacidad puede actualizarse. Si realizamos cambios importantes, lo notificaremos dentro de PinguBalance o por correo electrónico.\n\n")

                            append("Al usar PinguBalance, aceptas esta Política de Privacidad. Si no estás de acuerdo con los términos, te recomendamos no utilizar la aplicación.")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}
