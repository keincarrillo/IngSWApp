package com.example.koalm.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTyC(navController: NavHostController) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.primary   // mismo color que el botón Registrar
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text =
                            "Al descargar, instalar o utilizar la aplicación KOALM, usted acepta los presentes Términos y Condiciones de uso.\n" +
                                    "Si no está de acuerdo con alguno de ellos, le recomendamos no utilizar la aplicación.\n" +
                                    "KOALM es una aplicación móvil que permite al usuario monitorear su actividad física diaria, incluyendo pasos, " +
                                    "calidad y duración del sueño, tiempo de ejercicio y seguimiento de hábitos personales, tanto establecidos como nuevos.\n" +
                                    "El usuario se compromete a utilizar la aplicación únicamente para fines personales y lícitos.\n" +
                                    "Queda prohibido:\n" +
                                    "•\tUsar la app para recopilar datos de otros usuarios sin consentimiento.\n" +
                                    "•\tIntentar vulnerar la seguridad o integridad del sistema.\n" +
                                    "•\tModificar, copiar, distribuir o comercializar el software sin autorización.\n" +
                                    "Todos los derechos de propiedad intelectual sobre el software, diseño, código fuente y contenido visual pertenecen a los desarrolladores del proyecto KOALM.\n" +
                                    "No se otorgan licencias de uso más allá de lo expresamente permitido.\n\n" +
                                    "KOALM es una herramienta de acompañamiento personal. No constituye un servicio médico ni reemplaza el consejo de profesionales de la salud.\n" +
                                    "El equipo desarrollador no se hace responsable por daños derivados del uso incorrecto de la aplicación.\n" +
                                    "KOALM puede recopilar información como:\n" +
                                    "•\tDatos personales (nombre, correo electrónico).\n" +
                                    "•\tDatos de actividad física (pasos, sueño, hábitos).\n" +
                                    "•\tGeolocalización (opcional y solo con consentimiento).\n" +
                                    "Toda la información se maneja conforme a lo establecido en nuestra Política de Privacidad.\n" +
                                    "La aplicación puede integrar servicios externos como Firebase, los cuales cuentan con sus propias políticas de privacidad y tratamiento de datos.\n" +
                                    "Al usar KOALM, usted también acepta los términos de dichos servicios.\n" +
                                    "Nos reservamos el derecho de modificar estos términos en cualquier momento, en caso de cambios sustanciales, se notificará a los usuarios dentro de la app o por correo electrónico.\n" +
                                    "Este documento se rige por las leyes mexicanas, incluyendo la Ley Federal de Protección de Datos Personales en Posesión de los Particulares.\n" +
                                    "Para dudas, aclaraciones o ejercicio de derechos ARCO (Acceso, Rectificación, Cancelación u Oposición), puedes contactarnos al correo:\n" +
                                    "adslatex@gmail.com",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

