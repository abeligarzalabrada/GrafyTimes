package com.grilo.grafytimes.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grilo.grafytimes.settings.components.SettingsItem
import com.grilo.grafytimes.settings.components.SettingsSection
import com.grilo.grafytimes.settings.data.ThemeMode
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para diálogos
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    
    // Estado temporal para PIN
    var tempPin by remember { mutableStateOf("") }
    
    // Lanzadores para guardar y cargar archivos
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        val jsonData = viewModel.exportDataToJson()
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonData.toByteArray())
                        }
                        Toast.makeText(context, "Datos exportados correctamente", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al exportar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )
    
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val jsonData = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                        val success = viewModel.importDataFromJson(jsonData)
                        if (success) {
                            Toast.makeText(context, "Datos importados correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al importar datos", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al importar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de personalización
            SettingsSection(
                title = "Personalización",
                icon = Icons.Default.ColorLens
            ) {
                // Tema
                SettingsItem(
                    title = "Tema",
                    description = when(viewModel.themeMode) {
                        ThemeMode.LIGHT -> "Tema claro"
                        ThemeMode.DARK -> "Tema oscuro"
                        ThemeMode.SYSTEM -> "Tema del sistema"
                    },
                    icon = when(viewModel.themeMode) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.SYSTEM -> Icons.Default.Settings
                    },
                    onClick = { showThemeDialog = true }
                )
                
                // Color primario
                SettingsItem(
                    title = "Color principal",
                    description = "Personalizar color de la aplicación",
                    icon = Icons.Default.ColorLens,
                    onClick = { showColorPickerDialog = true },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(viewModel.primaryColorHex)))
                        )
                    }
                )
            }
            
            // Sección de seguridad
            SettingsSection(
                title = "Seguridad",
                icon = Icons.Default.Lock
            ) {
                // Bloqueo de app
                SettingsItem(
                    title = "Bloqueo de aplicación",
                    description = if (viewModel.useAppLock) "Activado" else "Desactivado",
                    icon = Icons.Default.Lock,
                    onClick = { showPinDialog = true },
                    trailing = {
                        Switch(
                            checked = viewModel.useAppLock,
                            onCheckedChange = { 
                                if (!it) {
                                    // Si está desactivando, simplemente guardar
                                    viewModel.saveAppLockSettings(false, "")
                                } else {
                                    // Si está activando, mostrar diálogo para PIN
                                    showPinDialog = true
                                }
                            }
                        )
                    }
                )
            }
            
            // Sección de notificaciones
            SettingsSection(
                title = "Notificaciones",
                icon = Icons.Default.Notifications
            ) {
                // Activar notificaciones
                SettingsItem(
                    title = "Recordatorios",
                    description = if (viewModel.enableNotifications) "Activados" else "Desactivados",
                    icon = Icons.Default.Notifications,
                    onClick = { showNotificationDialog = true },
                    trailing = {
                        Switch(
                            checked = viewModel.enableNotifications,
                            onCheckedChange = { 
                                if (!it) {
                                    // Si está desactivando, simplemente guardar
                                    viewModel.saveNotificationSettings(
                                        false,
                                        viewModel.reminderFrequency,
                                        viewModel.reminderTime,
                                        viewModel.enableInactivityReminder,
                                        viewModel.inactivityReminderDays
                                    )
                                } else {
                                    // Si está activando, mostrar diálogo de configuración
                                    showNotificationDialog = true
                                }
                            }
                        )
                    }
                )
            }
            
            // Sección de respaldo y recuperación
            SettingsSection(
                title = "Respaldo y recuperación",
                icon = Icons.Default.Backup
            ) {
                // Exportar datos
                SettingsItem(
                    title = "Exportar datos",
                    description = "Guardar todos los datos en un archivo",
                    icon = Icons.Default.Backup,
                    onClick = { 
                        saveFileLauncher.launch("grafytimes_backup_${System.currentTimeMillis()}.json")
                    }
                )
                
                // Importar datos
                SettingsItem(
                    title = "Importar datos",
                    description = "Cargar datos desde un archivo",
                    icon = Icons.Default.Restore,
                    onClick = { 
                        openFileLauncher.launch(arrayOf("application/json"))
                    }
                )
            }
        }
    }
    
    // Diálogo de selección de tema
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Seleccionar tema") },
            text = {
                Column {
                    ThemeOption(
                        title = "Tema claro",
                        selected = viewModel.themeMode == ThemeMode.LIGHT,
                        onClick = { 
                            viewModel.saveThemeSettings(ThemeMode.LIGHT, viewModel.primaryColorHex, false)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        title = "Tema oscuro",
                        selected = viewModel.themeMode == ThemeMode.DARK,
                        onClick = { 
                            viewModel.saveThemeSettings(ThemeMode.DARK, viewModel.primaryColorHex, true)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        title = "Tema del sistema",
                        selected = viewModel.themeMode == ThemeMode.SYSTEM,
                        onClick = { 
                            viewModel.saveThemeSettings(ThemeMode.SYSTEM, viewModel.primaryColorHex, viewModel.useDarkTheme)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de configuración de PIN
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configurar PIN de seguridad") },
            text = {
                Column {
                    Text("Ingresa un PIN de 4 dígitos para proteger la aplicación")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) tempPin = it },
                        label = { Text("PIN (4 dígitos)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        if (tempPin.length == 4) {
                            viewModel.saveAppLockSettings(true, tempPin)
                            tempPin = ""
                            showPinDialog = false
                        } else {
                            Toast.makeText(context, "El PIN debe tener 4 dígitos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = tempPin.length == 4
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    tempPin = ""
                    showPinDialog = false 
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de configuración de notificaciones
    if (showNotificationDialog) {
        var tempFrequency by remember { mutableStateOf(viewModel.reminderFrequency) }
        var tempTime by remember { mutableStateOf(viewModel.reminderTime) }
        var tempEnableInactivity by remember { mutableStateOf(viewModel.enableInactivityReminder) }
        var tempInactivityDays by remember { mutableStateOf(viewModel.inactivityReminderDays.toFloat()) }
        
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Configurar recordatorios") },
            text = {
                Column {
                    Text("Frecuencia de recordatorios")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempFrequency == 0,
                            onClick = { tempFrequency = 0 }
                        )
                        Text("Diario", modifier = Modifier.clickable { tempFrequency = 0 })
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = tempFrequency == 1,
                            onClick = { tempFrequency = 1 }
                        )
                        Text("Semanal", modifier = Modifier.clickable { tempFrequency = 1 })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hora del recordatorio: $tempTime")
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showTimePickerDialog = true }) {
                            Text("Cambiar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recordatorio de inactividad")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = tempEnableInactivity,
                            onCheckedChange = { tempEnableInactivity = it }
                        )
                    }
                    
                    if (tempEnableInactivity) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recordar después de ${tempInactivityDays.toInt()} días sin actividad")
                        Slider(
                            value = tempInactivityDays,
                            onValueChange = { tempInactivityDays = it },
                            valueRange = 1f..7f,
                            steps = 5
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.saveNotificationSettings(
                            true,
                            tempFrequency,
                            tempTime,
                            tempEnableInactivity,
                            tempInactivityDays.toInt()
                        )
                        showNotificationDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de selección de hora
    if (showTimePickerDialog) {
        var tempTimeState by remember { 
            val parts = viewModel.reminderTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            mutableStateOf(LocalTime.of(hour, minute))
        }
        
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("Seleccionar hora") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = androidx.compose.material3.rememberTimePickerState(
                        initialHour = tempTimeState.hour,
                        initialMinute = tempTimeState.minute
                    ), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                val timeState = androidx.compose.material3.rememberTimePickerState(
                    initialHour = tempTimeState.hour,
                    initialMinute = tempTimeState.minute
                )
                TextButton(
                    onClick = { 
                        val formattedTime = String.format("%02d:%02d", timeState.hour, timeState.minute)
                        viewModel.reminderTime = formattedTime
                        showTimePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de selección de color
    if (showColorPickerDialog) {
        val colorOptions = listOf(
            "#3F51B5" to "Indigo",  // Default
            "#F44336" to "Rojo",
            "#4CAF50" to "Verde",
            "#2196F3" to "Azul",
            "#FF9800" to "Naranja",
            "#9C27B0" to "Púrpura",
            "#795548" to "Marrón",
            "#607D8B" to "Azul grisáceo"
        )
        
        AlertDialog(
            onDismissRequest = { showColorPickerDialog = false },
            title = { Text("Seleccionar color principal") },
            text = {
                Column {
                    colorOptions.forEach { (colorHex, colorName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.saveThemeSettings(
                                        viewModel.themeMode,
                                        colorHex,
                                        viewModel.useDarkTheme
                                    )
                                    showColorPickerDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(colorName)
                            Spacer(modifier = Modifier.weight(1f))
                            if (viewModel.primaryColorHex == colorHex) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Seleccionado"
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showColorPickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.padding(8.dp))
        Text(title)
    }
}
