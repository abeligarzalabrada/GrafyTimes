package com.grilo.grafytimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.grilo.grafytimes.navigation.AppNavigation
import com.grilo.grafytimes.settings.data.SettingsDataStore
import com.grilo.grafytimes.settings.notifications.NotificationService
import com.grilo.grafytimes.ui.theme.GrafyTimesTheme
import com.grilo.grafytimes.user.UserConfigScreen
import com.grilo.grafytimes.user.UserViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    // Crear los ViewModels
    private val userViewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configurar notificaciones
        val notificationService = NotificationService(this)
        notificationService.createNotificationChannel()
        notificationService.scheduleReminders()
        
        setContent {
            GrafyTimesTheme {
                // Observar si el usuario ya ha configurado la app
                val isConfigured by userViewModel.isConfigured
                val isLoading by userViewModel.isLoading
                
                // Estado para el PIN de seguridad
                var showPinDialog by remember { mutableStateOf(false) }
                var pinInput by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf(false) }
                var appUnlocked by remember { mutableStateOf(false) }
                
                // Verificar si se requiere PIN al inicio
                val context = LocalContext.current
                LaunchedEffect(isConfigured, isLoading) {
                    if (!isLoading && isConfigured) {
                        val settingsDataStore = SettingsDataStore(context)
                        val settings = settingsDataStore.settingsFlow.first()
                        showPinDialog = settings.useAppLock
                    }
                }
                
                if (isLoading) {
                    // Mostrar pantalla de carga si es necesario
                    // Por ahora, simplemente no mostramos nada mientras carga
                } else if (!isConfigured) {
                    // Si el usuario no ha configurado la app, mostrar la pantalla de configuración
                    UserConfigScreen(
                        onConfigurationComplete = {
                            // No need for explicit action here as UserViewModel
                            // will update isConfigured state automatically
                        }
                    )
                } else if (showPinDialog && !appUnlocked) {
                    // Mostrar diálogo de PIN si es necesario
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Ingrese su PIN") },
                        text = {
                            TextField(
                                value = pinInput,
                                onValueChange = { 
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        pinInput = it 
                                        pinError = false
                                    }
                                },
                                label = { Text("PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = pinError,
                                supportingText = { if (pinError) Text("PIN incorrecto") }
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Verificar PIN
                                    runBlocking {
                                        val settingsDataStore = SettingsDataStore(context)
                                        val settings = settingsDataStore.settingsFlow.first()
                                        if (pinInput == settings.appLockPin) {
                                            appUnlocked = true
                                            showPinDialog = false
                                        } else {
                                            pinError = true
                                        }
                                    }
                                }
                            ) {
                                Text("Desbloquear")
                            }
                        }
                    )
                } else {
                    // Mostrar la navegación principal
                    AppNavigation()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    GrafyTimesTheme {
        AppNavigation()
    }
}