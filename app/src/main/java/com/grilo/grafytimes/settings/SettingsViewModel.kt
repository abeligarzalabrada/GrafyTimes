package com.grilo.grafytimes.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grilo.grafytimes.settings.data.AppSettings
import com.grilo.grafytimes.settings.data.SettingsDataStore
import com.grilo.grafytimes.settings.data.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)
    
    // Estados UI observables
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var primaryColorHex by mutableStateOf("")
    var useDarkTheme by mutableStateOf(false)
    var useAppLock by mutableStateOf(false)
    var appLockPin by mutableStateOf("")
    var enableNotifications by mutableStateOf(false)
    var reminderFrequency by mutableStateOf(0) // 0: Diario, 1: Semanal
    var reminderTime by mutableStateOf("") // Formato "HH:mm"
    var inactivityReminderDays by mutableStateOf(3)
    var enableInactivityReminder by mutableStateOf(false)
    
    init {
        // Cargar configuraciones guardadas
        viewModelScope.launch {
            dataStore.settingsFlow.collect { settings ->
                themeMode = settings.themeMode
                primaryColorHex = settings.primaryColorHex
                useDarkTheme = settings.useDarkTheme
                useAppLock = settings.useAppLock
                appLockPin = settings.appLockPin
                enableNotifications = settings.enableNotifications
                reminderFrequency = settings.reminderFrequency
                reminderTime = settings.reminderTime
                inactivityReminderDays = settings.inactivityReminderDays
                enableInactivityReminder = settings.enableInactivityReminder
            }
        }
    }
    
    // Guardar configuración de tema
    fun saveThemeSettings(themeMode: ThemeMode, primaryColorHex: String, useDarkTheme: Boolean) {
        this.themeMode = themeMode
        this.primaryColorHex = primaryColorHex
        this.useDarkTheme = useDarkTheme
        
        viewModelScope.launch {
            dataStore.updateThemeSettings(themeMode, primaryColorHex, useDarkTheme)
        }
    }
    
    // Guardar configuración de bloqueo de app
    fun saveAppLockSettings(useAppLock: Boolean, pin: String) {
        this.useAppLock = useAppLock
        this.appLockPin = pin
        
        viewModelScope.launch {
            dataStore.updateAppLockSettings(useAppLock, pin)
        }
    }
    
    // Guardar configuración de notificaciones
    fun saveNotificationSettings(
        enableNotifications: Boolean,
        reminderFrequency: Int,
        reminderTime: String,
        enableInactivityReminder: Boolean,
        inactivityReminderDays: Int
    ) {
        this.enableNotifications = enableNotifications
        this.reminderFrequency = reminderFrequency
        this.reminderTime = reminderTime
        this.enableInactivityReminder = enableInactivityReminder
        this.inactivityReminderDays = inactivityReminderDays
        
        viewModelScope.launch {
            dataStore.updateNotificationSettings(
                enableNotifications,
                reminderFrequency,
                reminderTime,
                enableInactivityReminder,
                inactivityReminderDays
            )
        }
    }
    
    // Exportar datos a JSON
    suspend fun exportDataToJson(): String {
        return dataStore.exportAllDataToJson()
    }
    
    // Importar datos desde JSON
    suspend fun importDataFromJson(jsonData: String): Boolean {
        return dataStore.importAllDataFromJson(jsonData)
    }
}