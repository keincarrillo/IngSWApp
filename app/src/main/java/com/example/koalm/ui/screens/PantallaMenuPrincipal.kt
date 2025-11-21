package com.example.koalm.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.core.content.edit
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.Habito
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.BienvenidoDialogoAnimado
import com.example.koalm.ui.components.LogroDialogoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.example.koalm.ui.screens.habitos.personalizados.parseColorFromFirebase
import com.example.koalm.ui.screens.habitos.saludMental.PantallaLibros
import com.example.koalm.ui.screens.habitos.saludMental.PantallaNotas
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.viewmodels.DashboardViewModel
import com.example.koalm.ui.viewmodels.InicioSesionPreferences
import com.example.koalm.ui.viewmodels.LogrosPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.asImageBitmap
import com.example.koalm.ui.screens.auth.base64ToBitmap

// Modelo para separar lo que se muestra en UI de lo que se usa como ID en la navegación
data class HabitoPingu(
    val routeKey: String,   // lo que espera la pantalla de hábitos koalísticos
    val tituloUi: String,   // título que ve el usuario
    val descripcion: String,
    val imagenId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMenuPrincipal(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val habitosPingu = listOf(
        HabitoPingu(
            routeKey = "Desconexión koalística",
            tituloUi = "Desconexión pingüina",
            descripcion = "Haz una pausa y descansa como un pingüino relajado sobre el hielo.",
            imagenId = R.drawable.pinguino_naturaleza
        ),
        HabitoPingu(
            routeKey = "Alimentación consciente",
            tituloUi = "Alimentación equilibrada",
            descripcion = "Disfruta tus comidas como un pingüino saboreando su pez favorito.",
            imagenId = R.drawable.pinguino_comiendo
        ),
        HabitoPingu(
            routeKey = "Meditación koalística",
            tituloUi = "Respira como pingüino",
            descripcion = "Toma un momento para respirar profundo antes de tu siguiente zambullida del día.",
            imagenId = R.drawable.pinguino_meditando
        ),
        HabitoPingu(
            routeKey = "Hidratación koalística",
            tituloUi = "Hidratación polar",
            descripcion = "Mantén tu cuerpo fresco como un pingüino en aguas heladas: bebe suficiente agua.",
            imagenId = R.drawable.pinguino_bebiendo
        ),
        HabitoPingu(
            routeKey = "Descanso koalístico",
            tituloUi = "Sueño reparador",
            descripcion = "Duerme abrigado como un pingüino en su colonia durante la noche.",
            imagenId = R.drawable.pinguino_durmiendo
        ),
        HabitoPingu(
            routeKey = "Escritura koalística",
            tituloUi = "Escritura polar",
            descripcion = "Escribe tus ideas como huellitas en la nieve: claras y únicas.",
            imagenId = R.drawable.pinguino_escribiendo
        ),
        HabitoPingu(
            routeKey = "Lectura koalística",
            tituloUi = "Lectura abrigada",
            descripcion = "Refúgiate con un buen libro como pingüino que se protege del viento.",
            imagenId = R.drawable.pinguino_leyendo
        )
    )

    // Datos de usuario
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("") }

    // NUEVO: estado para la foto de perfil
    var imagenPerfilBase64 by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuarioEmail) {
        if (!usuarioEmail.isNullOrEmpty()) {
            db.collection("usuarios")
                .document(usuarioEmail)
                .get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username").orEmpty()
                    imagenPerfilBase64 = doc.getString("imagenBase64")
                }
                .addOnFailureListener {
                    username = "Kool"
                    imagenPerfilBase64 = null
                }
        }
    }

    val context = LocalContext.current
    val prefs = remember { InicioSesionPreferences(context) }
    var mostrarDialogoBienvenida by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!prefs.fueMostradaAnimacion()) {
            mostrarDialogoBienvenida = true
        }
    }

    if (mostrarDialogoBienvenida) {
        BienvenidoDialogoAnimado(
            mensaje = "Bienvenid@ $username",
            onDismiss = {
                mostrarDialogoBienvenida = false
                prefs.marcarAnimacionComoMostrada()
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (usuarioEmail != null) {
                DrawerContenido(
                    navController = navController,
                    userEmail = usuarioEmail,
                    imagenBase64 = imagenPerfilBase64, // NUEVO
                    onDestinationClicked = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("¡Hola, $username! 🐧✨") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconoNotificacionesConBadge(navController)
                        IconButton(onClick = { navController.navigate("ajustes") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                BarraNavegacionInferior(
                    navController = navController,
                    rutaActual = "menu"
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val viewModel: DashboardViewModel = viewModel()
                val racha = viewModel.rachaSemanal
                Spacer(modifier = Modifier.height(1.dp))
                SeccionTitulo("Racha semanal")

                FormatoRacha(
                    dias = racha,
                    onClick = { /* navController.navigate("racha_habitos") */ }
                )

                SeccionTitulo("Hábitos pingü")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(habitosPingu) { habito ->
                        HabitoCarruselItem(
                            titulo = habito.tituloUi,
                            descripcion = habito.descripcion,
                            imagenId = habito.imagenId,
                            onClick = {
                                navController.navigate(
                                    "pantalla_habitos_koalisticos/${habito.routeKey}"
                                )
                            }
                        )
                    }
                }

                SeccionTitulo("Mis hábitos")
                if (usuarioEmail != null && userId != null) {
                    DashboardScreen(
                        usuarioEmail = usuarioEmail,
                        userId = userId,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryColor
    )
}

@Composable
fun EstadisticasCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Text("Gráficos de estadísticas", color = TertiaryMediumColor)
        }
    }
}

@Composable
fun DrawerContenido(
    navController: NavHostController,
    userEmail: String,
    imagenBase64: String?,              // NUEVO
    onDestinationClicked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER BONITO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryColor, SecondaryColor)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // --- AVATAR DEL USUARIO (reemplaza al icono de meditar) ---
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                color = White.copy(alpha = 0.18f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imagenBase64.isNullOrEmpty()) {
                            val bitmap = remember(imagenBase64) { base64ToBitmap(imagenBase64) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    tint = White
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "PinguBalance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Tu espacio para cuidar tus hábitos",
                            style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECCIÓN: NAVEGACIÓN PRINCIPAL
            Text(
                "Navegación",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            NavigationDrawerItem(
                label = { Text("Test de ansiedad") },
                selected = false,
                icon = { Icon(Icons.Default.SelfImprovement, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    navController.navigate("test_de_ansiedad")
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SECCIÓN: ESTADÍSTICAS DE HÁBITOS
            Text(
                "Estadísticas de hábitos",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            NavigationDrawerItem(
                label = { Text("Salud física") },
                selected = false,
                icon = { Icon(Icons.Default.LocalDrink, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    scope.launch {
                        val snapshot = db.collection("habitos")
                            .document(userEmail)
                            .collection("predeterminados")
                            .whereEqualTo("clase", "FISICO")
                            .get()
                            .await()

                        if (snapshot.isEmpty) {
                            navController.navigate("salud_fisica")
                        } else {
                            navController.navigate("estadisticas_salud_fisica")
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Salud mental") },
                selected = false,
                icon = { Icon(Icons.Default.Nightlight, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    scope.launch {
                        val snapshot = db.collection("habitos")
                            .document(userEmail)
                            .collection("predeterminados")
                            .whereEqualTo("clase", "MENTAL")
                            .get()
                            .await()

                        if (snapshot.isEmpty) {
                            navController.navigate("salud_mental")
                        } else {
                            navController.navigate("estadisticas_salud_mental")
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Personalizados") },
                selected = false,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    scope.launch {
                        val snapshot = db.collection("habitos")
                            .document(userEmail)
                            .collection("personalizados")
                            .get()
                            .await()

                        if (snapshot.isEmpty) {
                            navController.navigate("gestion_habitos_personalizados")
                        } else {
                            navController.navigate("estadisticas_habito_perzonalizado")
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SECCIÓN: RINCÓN CREATIVO
            Text(
                "Rincón creativo",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            NavigationDrawerItem(
                label = { Text("Mis notas") },
                selected = false,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    navController.navigate("notas")
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Mis libros") },
                selected = false,
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                onClick = {
                    onDestinationClicked()
                    navController.navigate("libros")
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FormatoRacha(
    dias: List<Pair<String, Boolean>>,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp, horizontal = 12.dp)
            .clickable { onClick() }
    ) {
        if (dias.isEmpty() || dias.all { !it.second }) {
            Text(
                text = "¡Empieza hoy y construye tu racha!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dias) { dia ->
                    val (letra, completado) = dia
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (completado) PrimaryColor else TertiaryColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (completado) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completado",
                                    tint = White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        Text(
                            text = letra,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitoCarruselItem(
    titulo: String,
    descripcion: String,
    imagenId: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 120.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imagenId),
            contentDescription = titulo,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(8.dp)
        ) {
            Column {
                Text(
                    titulo,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    descripcion,
                    color = White,
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
fun DashboardScreen(
    usuarioEmail: String,
    userId: String,
    viewModel: DashboardViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val logrosPrefs = remember { LogrosPreferences(context) }
    val habitos = viewModel.habitos
    val habitosPre = viewModel.habitosPre
    val cargando = viewModel.cargando
    val mensajeValidacion by viewModel.mensajeValidacion

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = {
                viewModel.mensajeValidacion.value = null
            }
        )
    }

    Log.d("HABITOS_DEBUG", "Cantidad de hábitos personalizados ${habitos.size}")
    Log.d("HABITOS_DEBUG", "Cantidad de hábitos predeterminados: ${habitosPre.size}")

    var tipoSeleccionado by remember { mutableStateOf("todos") }
    val tipos = listOf("todos", "personalizado", "fisico", "mental")

    // Cargar los hábitos
    LaunchedEffect(usuarioEmail, userId) {
        viewModel.cargarHabitos(usuarioEmail, userId)
    }

    // Filtros de tipo
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tipos) { tipo ->
            val isSelected = tipo == tipoSeleccionado

            TextButton(
                onClick = { tipoSeleccionado = tipo },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFF6FBF2) else Color.Transparent,
                    contentColor = if (isSelected) Color.Black else Color.Gray
                ),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(tipo.replaceFirstChar { it.uppercaseChar() })
            }
        }
    }

    if (cargando) {
        // Mostrar un indicador de carga mientras se están obteniendo los datos
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val diaActual = (LocalDate.now().dayOfWeek.value + 6) % 7
        val tipoFiltrado = tipoSeleccionado.lowercase()

        // Filtrar hábitos personalizados
        val habitosFiltradosPersonalizados = habitos.filter { habito ->
            val tipoCoincide = tipoFiltrado == "todos" || habito.clase.name.lowercase() == tipoFiltrado
            val frecuencia = habito.frecuencia
            val frecuenciaEsDiaria = frecuencia == null || frecuencia.all { !it }
            val diaActivo = frecuenciaEsDiaria || frecuencia?.getOrNull(diaActual) == true

            tipoCoincide && diaActivo
        }

        // Filtrar hábitos predeterminados
        val habitosFiltradosPredeterminados = habitosPre.filter { habito ->
            val tipoCoincide = tipoFiltrado == "todos" || habito.clase.name.lowercase() == tipoFiltrado
            val frecuencia = habito.diasSeleccionados
            val diaActivo = frecuencia.getOrNull(diaActual) == true

            tipoCoincide && diaActivo
        }

        val habitosActivosPersonalizados = habitosFiltradosPersonalizados.filter { it.estaActivo }
        val hayHabitos = habitosActivosPersonalizados.isNotEmpty() || habitosFiltradosPredeterminados.isNotEmpty()

        if (hayHabitos) {
            habitosActivosPersonalizados.forEach { habito ->
                HabitoCardPersonalizado(
                    habito = habito,
                    progreso = viewModel.progresos[habito.nombre.replace(" ", "_")],
                    onIncrementar = { valor ->
                        viewModel.incrementarProgreso(usuarioEmail, habito, valor)
                    },
                    logrosPrefs = logrosPrefs
                )
            }

            habitosFiltradosPredeterminados.forEach { habito ->
                HabitoCardPredeterminado(
                    habito = habito,
                    progreso = viewModel.progresosPre[habito.id],
                    onIncrementar = { valor ->
                        viewModel.incrementarProgresoPre(usuarioEmail, habito, valor)
                    },
                    logrosPrefs = logrosPrefs
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val hayHabitosGuardados = habitos.isNotEmpty() || habitosPre.isNotEmpty()
                Text(
                    text = when (tipoFiltrado) {
                        "personalizado" -> "¿Qué son los hábitos personalizados?\nCrea los tuyos según tus metas."
                        "físico" -> "¿Qué son los hábitos físicos?\nActividades como control de sueño, alimentación e hidratación."
                        "mental" -> "¿Qué son los hábitos mentales?\nActividades como meditar, leer o escribir."
                        else -> {
                            if (hayHabitosGuardados) {
                                "No tienes hábitos activos este día."
                            } else {
                                "No tienes hábitos aún."
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Mostrar botón según tipo
                if (tipoFiltrado != "todos") {
                    val ruta = when (tipoFiltrado) {
                        "físico" -> "salud_fisica"
                        "mental" -> "salud_mental"
                        else -> "configurar_habito_personalizado"
                    }

                    val textoBoton = when (tipoFiltrado) {
                        "físico", "mental" -> "Configurar"
                        else -> stringResource(R.string.boton_agregar)
                    }

                    Button(
                        onClick = { navController.navigate(ruta) },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = textoBoton)
                    }
                }
            }
        }
    }
}

@Composable
fun HabitoCardPersonalizado(
    habito: HabitoPersonalizado,
    progreso: ProgresoDiario?,
    onIncrementar: (Int) -> Unit,
    logrosPrefs: LogrosPreferences
) {
    val realizados = progreso?.realizados ?: 0
    val completado = progreso?.completado ?: false

    var mostrarDialogoLogro by remember { mutableStateOf(false) }

    LaunchedEffect(completado) {
        if (completado && !logrosPrefs.fueMostrado(habito.nombre)) {
            mostrarDialogoLogro = true
            logrosPrefs.marcarComoMostrado(habito.nombre)
        }
    }

    if (mostrarDialogoLogro) {
        LogroDialogoAnimado(
            mensaje = "¡Has completado el objetivo diario de tu hábito!",
            onDismiss = { mostrarDialogoLogro = false }
        )
    }

    // Progreso del hábito visualmente
    val total = habito.objetivoDiario
    val progresoPorcentaje = (realizados.toFloat() / total).coerceIn(0f, 1f)

    var progresoAnimado by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progresoPorcentaje) {
        progresoAnimado = progresoPorcentaje.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progresoAnimado,
        animationSpec = tween(durationMillis = 300)
    )

    // Obtener colores
    val colorFondo = parseColorFromFirebase(habito.colorEtiqueta)
    val icono = obtenerIconoPorNombre(habito.iconoEtiqueta)
    val colorIcono = parseColorFromFirebase(habito.colorEtiqueta, darken = true)

    // Visualizar recordatorios
    val progresoText = if (realizados >= total && total > 0) {
        "Completado: $realizados/$total"
    } else {
        "Objetivo por día: $realizados/$total"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, colorIcono, RoundedCornerShape(16.dp))
            .background(colorFondo.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = "Icono del Hábito",
                    tint = colorIcono,
                    modifier = Modifier
                        .size(33.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progresoText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (completado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    modifier = Modifier
                        .size(40.dp),
                    contentDescription = "Completado",
                    tint = colorIcono
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Transparent, shape = CircleShape)
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            val radius = size.minDimension / 2 - strokeWidth / 2

                            // Fondo base gris
                            drawCircle(
                                color = Color.LightGray,
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            // Fondo animado sobrepuesto
                            drawArc(
                                color = colorIcono,
                                startAngle = -90f,
                                sweepAngle = 360 * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = Offset(
                                    (size.width - radius * 2) / 2,
                                    (size.height - radius * 2) / 2
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                        }
                ) {
                    IconButton(
                        onClick = { onIncrementar(1) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = colorIcono,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

@Composable
fun HabitoCardPredeterminado(
    habito: Habito,
    progreso: ProgresoDiario?,
    onIncrementar: (Int) -> Unit,
    logrosPrefs: LogrosPreferences
) {
    val realizados = progreso?.realizados ?: 0
    val completado = progreso?.completado ?: false
    val totalRecordatoriosxDia = progreso?.totalObjetivoDiario ?: 0
    var mostrarDialogo by remember { mutableStateOf(false) }
    var valorInput by remember { mutableStateOf("") }

    var mostrarDialogoLogro by remember { mutableStateOf(false) }

    LaunchedEffect(completado) {
        if (completado && !logrosPrefs.fueMostrado(habito.id)) {
            mostrarDialogoLogro = true
            logrosPrefs.marcarComoMostrado(habito.id)
        }
    }

    if (mostrarDialogoLogro) {
        LogroDialogoAnimado(
            mensaje = "¡Has completado el objetivo diario de tu hábito!",
            onDismiss = { mostrarDialogoLogro = false }
        )
    }

    // Dialogo para ingresar el progreso
    if (mostrarDialogo) {
        Dialog(
            onDismissRequest = { mostrarDialogo = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (habito.tipo) {
                            TipoHabito.LECTURA -> "¿Cuántas páginas leíste?"
                            TipoHabito.ESCRITURA -> "¿Cuántas páginas escribiste?"
                            TipoHabito.MEDITACION -> "¿Cuántos minutos meditaste?"
                            TipoHabito.DESCONEXION_DIGITAL -> "¿Cuántos minutos estuviste desconectado?"
                            TipoHabito.SUEÑO -> "¿Cuántas horas dormiste?"
                            TipoHabito.ALIMENTACION -> "¿Cuántas comidas hiciste?"
                            TipoHabito.HIDRATACION -> "¿Cuántos litros de agua tomaste?"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = {
                            valorInput = it.filter { char -> char.isDigit() || char == '.' }
                        },
                        label = {
                            Text(
                                when (habito.tipo) {
                                    TipoHabito.LECTURA -> "Páginas"
                                    TipoHabito.ESCRITURA -> "Páginas"
                                    TipoHabito.MEDITACION -> "Minutos"
                                    TipoHabito.DESCONEXION_DIGITAL -> "Minutos"
                                    TipoHabito.SUEÑO -> "Horas"
                                    TipoHabito.ALIMENTACION -> "Cantidad"
                                    TipoHabito.HIDRATACION -> "Litros"
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (habito.tipo == TipoHabito.SUEÑO) KeyboardType.Decimal else KeyboardType.Number
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { mostrarDialogo = false },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                        ) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val valor = if (habito.tipo == TipoHabito.SUEÑO) {
                                    valorInput.toFloatOrNull()?.toInt() ?: 0
                                } else {
                                    valorInput.toIntOrNull() ?: 0
                                }
                                if (valor > 0) {
                                    onIncrementar(valor)
                                    mostrarDialogo = false
                                    valorInput = ""
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }

    // Progreso del hábito visualmente
    val progresoPorcentaje = if (totalRecordatoriosxDia == 1) {
        if (completado) 1f else 0f
    } else {
        val total = totalRecordatoriosxDia.coerceAtLeast(1)
        (realizados.toFloat() / total).coerceIn(0f, 1f)
    }

    var progresoAnimado by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progresoPorcentaje) {
        progresoAnimado = progresoPorcentaje.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progresoAnimado,
        animationSpec = tween(durationMillis = 300)
    )

    // Visualizar recordatorios y métricas específicas según el tipo de hábito
    val progresoText = when (habito.tipo) {
        //Hábitos mentales
        TipoHabito.ESCRITURA -> {
            val paginasEscritas = realizados
            if (completado) "Completado: $paginasEscritas páginas" else "Objetivo: $paginasEscritas/${habito.objetivoPaginas} páginas"
        }
        TipoHabito.LECTURA -> {
            val paginasLeidas = realizados
            if (completado) "Completado: $paginasLeidas páginas" else "Objetivo: $paginasLeidas/${habito.objetivoPaginas} páginas"
        }
        TipoHabito.MEDITACION -> {
            val minutosMeditados = realizados
            if (completado) "Completado: $minutosMeditados minutos" else "Objetivo: $minutosMeditados/${habito.duracionMinutos} minutos"
        }
        TipoHabito.DESCONEXION_DIGITAL -> {
            val minutosDesconectado = realizados
            if (completado) "Completado: $minutosDesconectado minutos" else "Objetivo: $minutosDesconectado/${habito.duracionMinutos} minutos"
        }

        //Hábitos de salud
        TipoHabito.SUEÑO -> {
            val horasDormidas = realizados
            if (completado) "Completado: $horasDormidas horas" else "Objetivo: $horasDormidas/${habito.objetivoHorasSueno} horas"
        }

        TipoHabito.ALIMENTACION -> {
            val comidasRealizadas = realizados
            if (completado) "Completado: $comidasRealizadas comidas" else "Objetivo: $comidasRealizadas/${habito.objetivoPaginas} comidas"
        }
        TipoHabito.HIDRATACION -> {
            val vasosAgua = realizados
            if (completado) "Completado: $vasosAgua litros" else "Objetivo: $vasosAgua/${habito.objetivoPaginas} litros"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .background(ContainerColor.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (habito.tipo) {
                        TipoHabito.MEDITACION -> Icons.Default.SelfImprovement
                        TipoHabito.LECTURA -> Icons.Default.MenuBook
                        TipoHabito.DESCONEXION_DIGITAL -> Icons.Default.PhoneDisabled
                        TipoHabito.ESCRITURA -> Icons.Default.Edit
                        TipoHabito.SUEÑO -> Icons.Default.Nightlight
                        TipoHabito.ALIMENTACION -> Icons.Default.Restaurant
                        TipoHabito.HIDRATACION -> Icons.Default.LocalDrink
                    },
                    contentDescription = "Icono del Hábito",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(33.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = habito.titulo,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progresoText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (completado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    modifier = Modifier
                        .size(40.dp),
                    contentDescription = "Completado",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Transparent, shape = CircleShape)
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            val radius = size.minDimension / 2 - strokeWidth / 2

                            // Fondo base gris
                            drawCircle(
                                color = Color.LightGray,
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            // Fondo animado sobrepuesto
                            drawArc(
                                color = PrimaryColor,
                                startAngle = -90f,
                                sweepAngle = 360 * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = Offset(
                                    (size.width - radius * 2) / 2,
                                    (size.height - radius * 2) / 2
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                        }
                ) {
                    IconButton(
                        onClick = { mostrarDialogo = true },
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

@Composable
fun IconoNotificacionesConBadge(
    navController: NavHostController
) {
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()

    var notificacionesNoLeidas by remember { mutableStateOf(0) }

    // Escuchar en tiempo real la cantidad de notificaciones no leídas
    LaunchedEffect(usuarioEmail) {
        if (usuarioEmail != null) {
            db.collection("usuarios")
                .document(usuarioEmail)
                .collection("notificaciones")
                .whereEqualTo("leido", false)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        notificacionesNoLeidas = 0
                        return@addSnapshotListener
                    }
                    notificacionesNoLeidas = snapshots?.size() ?: 0
                }
        }
    }

    IconButton(onClick = { navController.navigate("notificaciones") }) {
        if (notificacionesNoLeidas > 0) {
            BadgedBox(
                badge = {
                    Badge {
                        Text(notificacionesNoLeidas.toString())
                    }
                }
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
            }
        } else {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
        }
    }
}

private fun formatearDuracion(minutos: Int): String {
    return if (minutos < 60) {
        "${minutos}min"
    } else {
        val horas = minutos / 60
        val mins = minutos % 60
        if (mins == 0) "${horas}h" else "${horas}h ${mins}min"
    }
}

@Composable
fun AvatarTopBar(imagenBase64: String?) {
    val isDark = isSystemInDarkTheme()
    val tint = if (isDark) Color.White else BrandPrimaryColor

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!imagenBase64.isNullOrEmpty()) {
            val bitmap = remember(imagenBase64) { base64ToBitmapDashboard(imagenBase64) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(tint)
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }
}

/**
 * Versión local de base64ToBitmap para este archivo.
 */
private fun base64ToBitmapDashboard(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
