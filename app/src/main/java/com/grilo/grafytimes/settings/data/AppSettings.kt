package com.grilo.grafytimes.settings.data

import kotlinx.serialization.Serializable

/**
 * Modelo de datos para almacenar las configuraciones de la aplicación
 */
@Serializable
data class AppSettings(
    // Configuración de tema
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColorHex: String = "#3F51B5", // Color Indigo por defecto
    val useDarkTheme: Boolean = false,
    
    // Configuración de seguridad
    val useAppLock: Boolean = false,
    val appLockPin: String = "",
    
    // Configuración de notificaciones
    val enableNotifications: Boolean = false,
    val reminderFrequency: Int = 0, // 0: Diario, 1: Semanal
    val reminderTime: String = "08:00", // Formato "HH:mm"
    val enableInactivityReminder: Boolean = false,
    val inactivityReminderDays: Int = 3
)

/**
 * Enum que representa los modos de tema disponibles
 */
enum class ThemeMode {
    LIGHT,  // Tema claro siempre
    DARK,   // Tema oscuro siempre
    SYSTEM  // Seguir configuración del sistema
}