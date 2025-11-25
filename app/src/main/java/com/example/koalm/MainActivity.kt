package com.example.koalm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.navigation.AppNavigation
import com.example.koalm.ui.theme.KoalmTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTIVITY_RECOGNITION_REQ_CODE = 101
    }

    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Permiso de reconocimiento de actividad + servicio de pasos
        requestActivityRecognitionIfNeeded()

        // ¿Desde intent (Google) mandamos un startDestination específico?
        val fromIntent: String? = intent.getStringExtra("startDestination")
        val startDestination: String = fromIntent
            ?: if (firebaseAuth.currentUser?.isEmailVerified == true) {
                "menu"
            } else {
                "iniciar"
            }

        // Servicio de pasos
        launchStepService()

        setContent {
            val navController = rememberNavController()
            MainApp(
                navController = navController,
                onGoogleSignInClick = ::handleGoogleSignIn,
                startDestination = startDestination
            )
        }
    }

    // ---------------- SERVICIO DE PASOS ----------------

    private fun launchStepService() {
        val intent = Intent(this, com.example.koalm.services.MovimientoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                launchStepService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQ_CODE
                )
            }
        } else {
            launchStepService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQ_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launchStepService()
            }
        }
    }

    // ---------------- GOOGLE SIGN-IN (Credential Manager) ----------------

    private fun handleGoogleSignIn() {
        // 1) Validar que el client ID esté bien configurado
        val webClientId = getString(R.string.default_web_client_id)
        if (webClientId.isBlank() ||
            webClientId.contains("YOUR_WEB_CLIENT_ID", ignoreCase = true)
        ) {
            Toast.makeText(
                this,
                "default_web_client_id no está configurado correctamente.\n" +
                        "Revisa strings.xml y google-services.json.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // 2) Opción de Google ID para Credential Manager
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false) // permite cuentas nuevas y existentes
            // Puedes quitar AutoSelect para que siempre muestre el selector.
            //.setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(this@MainActivity, request)
                processCredential(response)
            } catch (e: GetCredentialException) {
                e.printStackTrace()
                val msg = e.errorMessage ?: e.message ?: "Error desconocido en Google Sign-In"
                Toast.makeText(
                    this@MainActivity,
                    "No se pudo iniciar sesión con Google.\n$msg",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun processCredential(response: GetCredentialResponse) {
        // Extraer el ID token de la credencial de Google
        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(response.credential.data)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error al obtener la credencial de Google.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val idToken: String = googleCredential.idToken ?: run {
            Toast.makeText(this, "No se pudo obtener el ID Token.", Toast.LENGTH_LONG).show()
            return
        }

        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val correo = user?.email ?: return@addOnCompleteListener

                // Metas de salud por defecto
                val metasRef = Firebase.firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")

                metasRef.get().addOnSuccessListener { metasDoc ->
                    if (!metasDoc.exists()) {
                        metasRef.set(
                            mapOf(
                                "metaPasos" to 6000,
                                "metaMinutos" to 60,
                                "metaCalorias" to 300
                            )
                        )
                    }
                }

                // Revisar si el perfil está completo
                val userDocRef = Firebase.firestore
                    .collection("usuarios")
                    .document(correo)

                userDocRef.get().addOnSuccessListener { doc ->
                    val destino: String = if (doc.exists()) {
                        val nombre = doc.getString("nombre") ?: ""
                        val apellido = doc.getString("apellido") ?: ""
                        val nacimiento = doc.getString("nacimiento") ?: ""
                        val genero = doc.getString("genero") ?: ""
                        val peso = doc.getDouble("peso")?.toFloat()
                        val altura = doc.getLong("altura")?.toInt()

                        val completo = listOf(
                            nombre.isNotBlank(),
                            apellido.isNotBlank(),
                            nacimiento.isNotBlank(),
                            genero.isNotBlank(),
                            peso != null,
                            altura != null
                        ).all { it }

                        if (completo) "menu" else "personalizar"
                    } else {
                        "personalizar"
                    }

                    // Relanzamos MainActivity con el startDestination adecuado
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("startDestination", destino)
                        addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(
                    this,
                    "Error al iniciar con Google (Firebase): ${task.exception?.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ---------------- COMPOSABLE ROOT ----------------

    @Composable
    private fun MainApp(
        navController: NavHostController,
        onGoogleSignInClick: () -> Unit,
        startDestination: String
    ) {
        KoalmTheme {
            val systemUi = rememberSystemUiController()
            val isDark = isSystemInDarkTheme()

            SideEffect {
                systemUi.setSystemBarsColor(Color.Transparent, darkIcons = !isDark)
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation(
                    navController = navController,
                    onGoogleSignInClick = onGoogleSignInClick,
                    startDestination = startDestination
                )
            }
        }
    }
}
