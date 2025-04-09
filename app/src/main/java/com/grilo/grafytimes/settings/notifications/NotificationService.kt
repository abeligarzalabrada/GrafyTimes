package com.grilo.grafytimes.settings.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.grilo.grafytimes.MainActivity
import com.grilo.grafytimes.R
import com.grilo.grafytimes.settings.data.SettingsDataStore
import com.grilo.grafytimes.statistics.data.StatisticsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NotificationService(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "grafy_times_channel"
        private const val REMINDER_WORK_NAME = "service_reminder_work"
        private const val INACTIVITY_WORK_NAME = "inactivity_reminder_work"
    }
    
    // Crear canal de notificaciones (requerido para Android 8.0+)
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de servicio"
            val descriptionText = "Notificaciones para recordar registrar horas y salir al servicio"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // Programar recordatorios según configuración
    fun scheduleReminders() {
        runBlocking {
            val settingsDataStore = SettingsDataStore(context)
            val settings = settingsDataStore.settingsFlow.first()
            
            // Cancelar trabajos existentes
            WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(INACTIVITY_WORK_NAME)
            
            // Si las notificaciones están habilitadas, programar recordatorios
            if (settings.enableNotifications) {
                // Configurar restricciones
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
                
                // Programar recordatorio de servicio (diario o semanal)
                val repeatInterval = if (settings.reminderFrequency == 0) 1L else 7L
                val repeatIntervalTimeUnit = TimeUnit.DAYS
                
                val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                    repeatInterval, repeatIntervalTimeUnit
                )
                    .setConstraints(constraints)
                    .build()
                
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    REMINDER_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    reminderWorkRequest
                )
                
                // Programar recordatorio de inactividad si está habilitado
                if (settings.enableInactivityReminder) {
                    val inactivityWorkRequest = PeriodicWorkRequestBuilder<InactivityReminderWorker>(
                        1, TimeUnit.DAYS
                    )
                        .setConstraints(constraints)
                        .build()
                    
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        INACTIVITY_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        inactivityWorkRequest
                    )
                }
            }
        }
    }
    
    // Mostrar notificación
    fun showNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usar un ícono temporal
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        with(NotificationManagerCompat.from(context)) {
            // Verificar permisos en Android 13+
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, builder.build())
            }
        }
    }
}

// Worker para recordatorios de servicio
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val settingsDataStore = SettingsDataStore(applicationContext)
        val settings = runBlocking { settingsDataStore.settingsFlow.first() }
        
        // Verificar si es hora de mostrar la notificación
        val currentTime = LocalTime.now()
        val reminderTime = try {
            val parts = settings.reminderTime.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            LocalTime.of(8, 0) // Hora por defecto
        }
        
        // Si estamos dentro de la ventana de tiempo (+-30 minutos)
        if (currentTime.hour == reminderTime.hour && 
            Math.abs(currentTime.minute - reminderTime.minute) <= 30) {
            
            val title = "Recordatorio de servicio"
            val message = if (settings.reminderFrequency == 0) {
                "¡Es hora de salir al servicio del campo hoy!"
            } else {
                "¡Es un buen día para dedicar tiempo al servicio del campo!"
            }
            
            NotificationService(applicationContext).showNotification(
                title, message, 1
            )
        }
        
        return Result.success()
    }
}

// Worker para recordatorios de inactividad
class InactivityReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val settingsDataStore = SettingsDataStore(applicationContext)
        val statisticsDataStore = StatisticsDataStore(applicationContext)
        
        runBlocking {
            val settings = settingsDataStore.settingsFlow.first()
            
            // Obtener la última fecha con actividad registrada
            val currentDate = LocalDate.now()
            val calendarEntries = statisticsDataStore.getCalendarEntries(
                currentDate.year,
                currentDate.monthValue
            ).first()
            
            // Encontrar la entrada más reciente
            val lastActivityDate = calendarEntries
                .map { LocalDate.parse(it.date) }
                .maxOrNull() ?: currentDate.minusDays(settings.inactivityReminderDays.toLong() + 1)
            
            // Calcular días desde la última actividad
            val daysSinceLastActivity = currentDate.toEpochDay() - lastActivityDate.toEpochDay()
            
            // Si han pasado más días de los configurados, mostrar notificación
            if (daysSinceLastActivity >= settings.inactivityReminderDays) {
                val title = "Recordatorio de inactividad"
                val message = "Han pasado $daysSinceLastActivity días desde tu última actividad de servicio."
                
                NotificationService(applicationContext).showNotification(
                    title, message, 2
                )
            }
        }
        
        return Result.success()
    }
}