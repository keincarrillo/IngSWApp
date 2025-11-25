package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import java.util.Locale
import com.example.koalm.R
import com.example.koalm.ui.components.Logo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaControlPeso(
    navController: NavHostController
) {
    val correo = FirebaseAuth.getInstance().currentUser?.email
    var pesoActual by remember { mutableStateOf(0f) }
    var pesoObjetivo by remember { mutableStateOf(0f) }

    LaunchedEffect(correo) {
        if (correo == null) return@LaunchedEffect
        val firestore = Firebase.firestore

        val pesoUsuario: Float = try {
            val userDoc = firestore
                .collection("usuarios")
                .document(correo)
                .get()
                .await()
            userDoc.getDouble("peso")?.toFloat() ?: 0f
        } catch (e: Exception) {
            0f
        }

        try {
            val metasDoc = firestore
                .collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")
                .get()
                .await()

            val pesoActualDb = metasDoc.getDouble("pesoActual")?.toFloat()
            val pesoObjetivoDb = metasDoc.getDouble("pesoObjetivo")?.toFloat()

            pesoActual = pesoActualDb ?: pesoUsuario
            pesoObjetivo = pesoObjetivoDb ?: 0f
        } catch (e: Exception) {
            pesoActual = pesoUsuario
            pesoObjetivo = 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clickable { navController.navigate("progreso-peso") },
                        contentAlignment = Alignment.Center
                    ) {
                        Logo(
                            logoRes = R.drawable.pinguino_peso,
                            contentDescription = "Pingüino peso"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toca al pingüino para ver tu progreso",
                        fontSize = 13.sp,
                        color = TertiaryMediumColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            pesoObjetivo > 0f && pesoObjetivo > pesoActual -> {
                                String.format(
                                    Locale.getDefault(),
                                    "Debes ganar %.1f kg",
                                    kotlin.math.abs(pesoObjetivo - pesoActual)
                                )
                            }
                            pesoObjetivo > 0f && pesoObjetivo < pesoActual -> {
                                String.format(
                                    Locale.getDefault(),
                                    "Debes perder %.1f kg",
                                    kotlin.math.abs(pesoObjetivo - pesoActual)
                                )
                            }
                            pesoObjetivo == pesoActual && pesoActual > 0f -> "¡Estás en tu peso objetivo!"
                            else -> "No hay objetivo de peso establecido"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ComponenteObjetivos(
                        titulo = "Peso actual",
                        textoBoton = "Actualizar peso",
                        valor = pesoActual,
                        navController = navController,
                        ruta = "actualizar-peso"
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ComponenteObjetivos(
                        titulo = "Objetivo",
                        textoBoton = "Editar objetivo",
                        valor = pesoObjetivo,
                        navController = navController,
                        ruta = "objetivos-peso"
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponenteObjetivos(
    titulo: String,
    textoBoton: String,
    valor: Float,
    navController: NavHostController,
    ruta: String
) {
    Text(titulo, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Text("$valor kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = { navController.navigate(ruta) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(textoBoton)
    }
}
