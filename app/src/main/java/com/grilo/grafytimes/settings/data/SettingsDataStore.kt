package com.grilo.grafytimes.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grilo.grafytimes.biblestudy.data.BibleStudyDataStore
import com.grilo.grafytimes.productivity.data.ProductivityDataStore
import com.grilo.grafytimes.statistics.data.StatisticsDataStore
import com.grilo.grafytimes.user.data.UserDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Singleton para acceder al DataStore de configuraciones
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")

class SettingsDataStore(private val context: Context) {
    
    companion object {
        // Claves para configuración de tema
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")
        private val USE_DARK_THEME_KEY = booleanPreferencesKey("use_dark_theme")
        
        // Claves para configuración de seguridad
        private val USE_APP_LOCK_KEY = booleanPreferencesKey("use_app_lock")
        private val APP_LOCK_PIN_KEY = stringPreferencesKey("app_lock_pin")
        
        // Claves para configuración de notificaciones
        private val ENABLE_NOTIFICATIONS_KEY = booleanPreferencesKey("enable_notifications")
        private val REMINDER_FREQUENCY_KEY = intPreferencesKey("reminder_frequency")
        private val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")
        private val ENABLE_INACTIVITY_REMINDER_KEY = booleanPreferencesKey("enable_inactivity_reminder")
        private val INACTIVITY_REMINDER_DAYS_KEY = intPreferencesKey("inactivity_reminder_days")
    }
    
    // Obtener las configuraciones como Flow
    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        val themeModeStr = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        val primaryColorHex = preferences[PRIMARY_COLOR_KEY] ?: "#3F51B5"
        val useDarkTheme = preferences[USE_DARK_THEME_KEY] ?: false
        
        val useAppLock = preferences[USE_APP_LOCK_KEY] ?: false
        val appLockPin = preferences[APP_LOCK_PIN_KEY] ?: ""
        
        val enableNotifications = preferences[ENABLE_NOTIFICATIONS_KEY] ?: false
        val reminderFrequency = preferences[REMINDER_FREQUENCY_KEY] ?: 0
        val reminderTime = preferences[REMINDER_TIME_KEY] ?: "08:00"
        val enableInactivityReminder = preferences[ENABLE_INACTIVITY_REMINDER_KEY] ?: false
        val inactivityReminderDays = preferences[INACTIVITY_REMINDER_DAYS_KEY] ?: 3
        
        // Convertir string a enum
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        
        AppSettings(
            themeMode = themeMode,
            primaryColorHex = primaryColorHex,
            useDarkTheme = useDarkTheme,
            useAppLock = useAppLock,
            appLockPin = appLockPin,
            enableNotifications = enableNotifications,
            reminderFrequency = reminderFrequency,
            reminderTime = reminderTime,
            enableInactivityReminder = enableInactivityReminder,
            inactivityReminderDays = inactivityReminderDays
        )
    }
    
    // Actualizar configuración de tema
    suspend fun updateThemeSettings(themeMode: ThemeMode, primaryColorHex: String, useDarkTheme: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
            preferences[PRIMARY_COLOR_KEY] = primaryColorHex
            preferences[USE_DARK_THEME_KEY] = useDarkTheme
        }
    }
    
    // Actualizar configuración de bloqueo de app
    suspend fun updateAppLockSettings(useAppLock: Boolean, pin: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_APP_LOCK_KEY] = useAppLock
            preferences[APP_LOCK_PIN_KEY] = pin
        }
    }
    
    // Actualizar configuración de notificaciones
    suspend fun updateNotificationSettings(
        enableNotifications: Boolean,
        reminderFrequency: Int,
        reminderTime: String,
        enableInactivityReminder: Boolean,
        inactivityReminderDays: Int
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS_KEY] = enableNotifications
            preferences[REMINDER_FREQUENCY_KEY] = reminderFrequency
            preferences[REMINDER_TIME_KEY] = reminderTime
            preferences[ENABLE_INACTIVITY_REMINDER_KEY] = enableInactivityReminder
            preferences[INACTIVITY_REMINDER_DAYS_KEY] = inactivityReminderDays
        }
    }
    
    // Exportar todos los datos a JSON
    suspend fun exportAllDataToJson(): String {
        // Obtener datos de todas las fuentes
        val settings = settingsFlow.first()
        
        // Obtener datos de usuario
        val userDataStore = UserDataStore(context)
        val userData = userDataStore.userDataFlow.first()
        
        // Obtener datos de productividad
        val productivityDataStore = ProductivityDataStore(context)
        val monthlyGoal = productivityDataStore.monthlyGoalFlow.first()
        
        // Obtener datos de estudios bíblicos
        val bibleStudyDataStore = BibleStudyDataStore(context)
        val bibleStudies = bibleStudyDataStore.bibleStudiesFlow.first()
        val serviceRecords = bibleStudyDataStore.serviceRecordsWithStudyFlow.first()
        
        // Obtener datos de estadísticas
        val statisticsDataStore = StatisticsDataStore(context)
        
        // Crear estructura de datos para exportación
        val exportData = mapOf(
            "settings" to settings,
            "userData" to userData,
            "monthlyGoal" to monthlyGoal,
            "bibleStudies" to bibleStudies,
            "serviceRecords" to serviceRecords
        )
        
        // Convertir a JSON
        return Json.encodeToString(exportData)
    }
    
    // Importar todos los datos desde JSON
    suspend fun importAllDataFromJson(jsonData: String): Boolean {
        return try {
            // Decodificar JSON
            val importedData = Json.decodeFromString<Map<String, Any>>(jsonData)
            
            // Importar configuraciones
            val settings = importedData["settings"] as? AppSettings
            if (settings != null) {
                updateThemeSettings(settings.themeMode, settings.primaryColorHex, settings.useDarkTheme)
                updateAppLockSettings(settings.useAppLock, settings.appLockPin)
                updateNotificationSettings(
                    settings.enableNotifications,
                    settings.reminderFrequency,
                    settings.reminderTime,
                    settings.enableInactivityReminder,
                    settings.inactivityReminderDays
                )
            }
            
            // Importar datos de usuario
            val userData = importedData["userData"]
            if (userData != null) {
                val userDataStore = UserDataStore(context)
                // Implementar la lógica para guardar los datos de usuario importados
            }
            
            // Importar datos de productividad
            val monthlyGoal = importedData["monthlyGoal"] as? String
            if (monthlyGoal != null) {
                val productivityDataStore = ProductivityDataStore(context)
                productivityDataStore.saveMonthlyGoal(monthlyGoal)
            }
            
            // Importar estudios bíblicos
            val bibleStudies = importedData["bibleStudies"]
            val serviceRecords = importedData["serviceRecords"]
            if (bibleStudies != null && serviceRecords != null) {
                val bibleStudyDataStore = BibleStudyDataStore(context)
                // Implementar la lógica para guardar los estudios bíblicos importados
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}