package com.example.koalm.ui.screens.tests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTestAnsiedad(navController: NavHostController? = null) {
    val preguntas = listOf(
        "Sentirse nervioso/a, intranquilo/a o con los nervios de punta.",
        "No poder dejar de preocuparse o no poder controlar la preocupación.",
        "Preocuparse demasiado por diferentes cosas.",
        "Dificultad para relajarse.",
        "Estar tan inquieto/a que es difícil permanecer sentad@ tranquilamente.",
        "Molestarse o ponerse irritable fácilmente.",
        "Sentir miedo como si algo terrible pudiera pasar."
    )

    val opciones = listOf("Nunca", "Varios días", "Más de la mitad del tiempo", "Casi todos los días")
    val respuestas = remember { mutableStateListOf<Int>().apply { repeat(preguntas.size) { add(-1) } } }

    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        bottomBar = {
            navController?.let { BarraNavegacionInferior(it, "estadisticas") }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Test de ansiedad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "En las últimas 2 semanas, ¿con qué frecuencia has experimentado alguno de los siguientes problemas?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 24.dp),
                color = colorScheme.onBackground
            )

            preguntas.forEachIndexed { index, pregunta ->
                PreguntaCard(
                    pregunta = pregunta,
                    opciones = opciones,
                    seleccionada = respuestas[index],
                    onSeleccionar = { respuestas[index] = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val resultado = calcularResultado(respuestas)
                        navController?.navigate("resultado_ansiedad/$resultado")
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = respuestas.none { it == -1 }
                ) {
                    Text(
                        "Ver resultados",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PreguntaCard(
    pregunta: String,
    opciones: List<String>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) colorScheme.outline else BorderColor
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isDarkTheme) colorScheme.surface else ContainerColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = pregunta,
                fontSize = 15.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            opciones.forEachIndexed { index, opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = seleccionada == index,
                        onClick = { onSeleccionar(index) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PrimaryColor,
                            unselectedColor = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = opcion,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

fun calcularResultado(respuestas: List<Int>): Int {
    return respuestas.sumOf { it + 1 }
}