package com.grilo.grafytimes.statistics.data

import com.grilo.grafytimes.user.data.ServiceActivity
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.YearMonth

/**
 * Modelo de datos para estadísticas mensuales
 */
@Serializable
data class MonthlyStatistics(
    val yearMonth: String = "", // Formato: "yyyy-MM"
    val totalHours: Float = 0f,
    val goalHours: Float = 0f,
    val goalPercentage: Float = 0f,
    val bibleStudiesCount: Int = 0,
    val activitiesSummary: Map<String, ActivitySummary> = emptyMap(),
    val daysWorked: Int = 0
)

/**
 * Resumen de una actividad específica
 */
@Serializable
data class ActivitySummary(
    val activityName: String = "",
    val totalHours: Float = 0f,
    val sessionCount: Int = 0
)

/**
 * Modelo para representar una entrada en el calendario
 */
@Serializable
data class CalendarEntry(
    val date: String = "", // Formato: "yyyy-MM-dd"
    val hours: Float = 0f,
    val activityType: String = "",
    val bibleStudyId: String = "",
    val hasNotes: Boolean = false
)

/**
 * Modelo para el informe mensual
 */
@Serializable
data class MonthlyReport(
    val yearMonth: String = "", // Formato: "yyyy-MM"
    val totalHours: Float = 0f,
    val goalHours: Float = 0f,
    val goalPercentage: Float = 0f,
    val bibleStudiesCount: Int = 0,
    val activitiesSummary: Map<String, ActivitySummary> = emptyMap(),
    val calendarEntries: List<CalendarEntry> = emptyList(),
    val notes: List<String> = emptyList()
)