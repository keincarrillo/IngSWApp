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
fun PantallaTyC(navController: NavHostController) {

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    // Colores de card y borde adaptados a modo claro/oscuro (igual que otras pantallas)
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
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    color = cardBorderColor
                ),
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
                            append(
                                "Al descargar, instalar o utilizar la aplicación PinguBalance, aceptas los presentes Términos y Condiciones de uso. " +
                                        "Si no estás de acuerdo con ellos, te recomendamos no utilizar la aplicación.\n\n"
                            )

                            append(
                                "PinguBalance es una aplicación desarrollada como proyecto de Ingeniería de Software, " +
                                        "orientada al bienestar físico y mental. La app permite monitorear hábitos como sueño, " +
                                        "alimentación, hidratación, actividad física y otros registros relacionados con tu salud y bienestar.\n\n"
                            )

                            append("USO DE LA APLICACIÓN\n")
                            append(
                                "Te comprometes a utilizar PinguBalance únicamente para fines personales y lícitos. " +
                                        "Queda estrictamente prohibido:\n\n"
                            )
                            append("• Usar la app para recopilar datos de otros usuarios sin su consentimiento.\n")
                            append("• Intentar vulnerar la seguridad, integridad o disponibilidad del sistema.\n")
                            append("• Modificar, copiar, distribuir o comercializar el software sin autorización escrita del equipo desarrollador.\n\n")

                            append("PROPIEDAD INTELECTUAL\n")
                            append(
                                "Todos los derechos de propiedad intelectual sobre el diseño, código fuente, nombre, logotipo (pingüino) y contenido visual de PinguBalance " +
                                        "pertenecen a sus desarrolladores. No se otorgan licencias de uso más allá de lo expresamente permitido para utilizar la app como usuario final.\n\n"
                            )

                            append("ALCANCE Y LIMITACIONES\n")
                            append(
                                "PinguBalance es una herramienta de acompañamiento personal y educativo. No constituye un servicio médico, nutricional ni psicológico profesional. " +
                                        "Las recomendaciones, gráficas o indicadores mostrados en la app no sustituyen el diagnóstico ni el tratamiento de profesionales de la salud. " +
                                        "El uso de la aplicación es bajo tu propia responsabilidad.\n\n"
                            )

                            append("DATOS Y PRIVACIDAD\n")
                            append(
                                "La app puede recopilar información como:\n\n"
                            )
                            append("• Datos personales básicos (por ejemplo: nombre y correo electrónico).\n")
                            append("• Datos de hábitos y actividad (sueño, pasos, hidratación, metas, etc.).\n")
                            append("• Geolocalización, únicamente si otorgas tu consentimiento explícito.\n\n")
                            append(
                                "El tratamiento de esta información se realiza conforme a lo establecido en nuestra Política de Privacidad. " +
                                        "La aplicación puede integrar servicios externos (como Firebase) que cuentan con sus propios términos y políticas, " +
                                        "los cuales también aceptas al usar PinguBalance.\n\n"
                            )

                            append("MODIFICACIONES A LOS TÉRMINOS\n")
                            append(
                                "Nos reservamos el derecho de modificar estos Términos y Condiciones en cualquier momento. " +
                                        "En caso de cambios relevantes, se notificará dentro de la aplicación o por correo electrónico, según corresponda.\n\n"
                            )

                            append("LEGISLACIÓN APLICABLE\n")
                            append(
                                "Este documento se rige por las leyes mexicanas, incluyendo la Ley Federal de Protección de Datos Personales en Posesión de los Particulares y demás normativa aplicable.\n\n"
                            )

                            append("CONTACTO\n")
                            append(
                                "Para dudas, comentarios o ejercicio de derechos relacionados con tus datos, puedes contactarnos en:\n" +
                                        "pingubalance@gmail.com\n"
                            )
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
