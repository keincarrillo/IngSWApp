// ActividadDiariaViewModel.kt
package com.example.koalm.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.ActividadDiaria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActividadDiariaViewModel : ViewModel() {

    private val _actividades = MutableStateFlow<List<ActividadDiaria>>(emptyList())
    val actividades: StateFlow<List<ActividadDiaria>> = _actividades

    // Valores por defecto; se pueden actualizar desde Firestore si el documento existe
    private var metaPasos = 8000f
    private var metaCalorias = 500f
    private var metaTiempo = 180f

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        cargarActividades()
    }

    private fun cargarActividades() {
        viewModelScope.launch {
            val usuario = auth.currentUser ?: return@launch
            val correo = usuario.email ?: return@launch

            // 1) Leer metas desde Firestore (si existen en "usuarios/{correo}/metasSalud/valores")
            try {
                val docMeta = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")
                    .get()
                    .await()

                if (docMeta.exists()) {
                    docMeta.getLong("metaPasos")?.let { metaPasos = it.toFloat() }
                    docMeta.getLong("metaCalorias")?.let { metaCalorias = it.toFloat() }
                    docMeta.getLong("metaTiempo")?.let { metaTiempo = it.toFloat() }
                }

                // 🔐 Aseguramos metas > 0 (por si vienen en 0 o nulas)
                if (metaPasos <= 0f) metaPasos = 8000f
                if (metaCalorias <= 0f) metaCalorias = 500f
                if (metaTiempo <= 0f) metaTiempo = 180f

            } catch (e: Exception) {
                e.printStackTrace()
                // En error dejamos los valores por defecto
                metaPasos = 8000f
                metaCalorias = 500f
                metaTiempo = 180f
            }

            // 2) Construimos la semana actual (lunes a domingo) y traemos datos reales
            try {
                val hoy = LocalDate.now()
                val lunesSemana = hoy.with(DayOfWeek.MONDAY)
                val domingoSemana = lunesSemana.plusDays(6)

                val formateador = DateTimeFormatter.ISO_LOCAL_DATE
                val idLunes = lunesSemana.format(formateador)
                val idDomingo = domingoSemana.format(formateador)

                val snapshot = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metricasDiarias")
                    .orderBy("__name__", Query.Direction.ASCENDING)
                    .whereGreaterThanOrEqualTo("__name__", idLunes)
                    .whereLessThanOrEqualTo("__name__", idDomingo)
                    .get()
                    .await()

                val mapDocsPorFecha = snapshot.documents.associateBy { it.id }

                val listaPasos = mutableListOf<Float>()
                val listaCalorias = mutableListOf<Float>()
                val listaTiempo = mutableListOf<Float>()

                for (i in 0..6) {
                    val fechaIterada = lunesSemana.plusDays(i.toLong())
                    val claveFecha = fechaIterada.format(formateador)

                    if (mapDocsPorFecha.containsKey(claveFecha)) {
                        val doc = mapDocsPorFecha[claveFecha]!!
                        val pasosDoc = doc.getLong("pasos")?.toFloat() ?: 0f
                        val caloriasDoc = doc.getLong("calorias")?.toFloat() ?: 0f
                        val tiempoDoc = doc.getLong("tiempoActividad")?.toFloat() ?: 0f

                        listaPasos.add(pasosDoc)
                        listaCalorias.add(caloriasDoc)
                        listaTiempo.add(tiempoDoc)
                    } else {
                        // Día sin registro → 0
                        listaPasos.add(0f)
                        listaCalorias.add(0f)
                        listaTiempo.add(0f)
                    }
                }

                val actividadesFirebase = listOf(
                    ActividadDiaria(tipo = "Pasos", meta = metaPasos, datos = listaPasos),
                    ActividadDiaria(tipo = "Calorías quemadas", meta = metaCalorias, datos = listaCalorias),
                    ActividadDiaria(tipo = "Tiempo activo", meta = metaTiempo, datos = listaTiempo)
                )

                _actividades.value = actividadesFirebase

            } catch (e: Exception) {
                e.printStackTrace()
                _actividades.value = emptyList()
            }
        }
    }
}
