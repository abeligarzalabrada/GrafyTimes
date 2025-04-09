package com.grilo.grafytimes.statistics.data

import android.content.Context
import com.grilo.grafytimes.biblestudy.data.BibleStudyDataStore
import com.grilo.grafytimes.biblestudy.data.ServiceRecordWithStudy
import com.grilo.grafytimes.productivity.data.ProductivityDataStore
import com.grilo.grafytimes.user.data.UserDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class StatisticsDataStore(private val context: Context) {
    private val productivityDataStore = ProductivityDataStore(context)
    private val bibleStudyDataStore = BibleStudyDataStore(context)
    private val userDataStore = UserDataStore(context)
    
    companion object {
        private val YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    
    /**
     * Obtiene las estadísticas para un mes específico
     */
    fun getMonthlyStatistics(year: Int, month: Int): Flow<MonthlyStatistics> {
        val yearMonth = YearMonth.of(year, month)
        val yearMonthStr = yearMonth.format(YEAR_MONTH_FORMATTER)
        
        return combine(
            productivityDataStore.getWorkedHoursForMonth(year, month),
            productivityDataStore.monthlyGoalFlow,
            bibleStudyDataStore.serviceRecordsWithStudyFlow
        ) { hoursMap, goalHours, allServiceRecords ->
            // Filtrar registros de servicio para este mes
            val monthStart = yearMonth.atDay(1)
            val monthEnd = yearMonth.atEndOfMonth()
            val serviceRecordsForMonth = allServiceRecords.filter {
                try {
                    val recordDate = LocalDate.parse(it.date)
                    recordDate.year == year && recordDate.monthValue == month
                } catch (e: Exception) {
                    false
                }
            }
            
            // Calcular horas totales
            val totalHours = hoursMap.values.sum()
            
            // Calcular porcentaje de meta
            val goalHoursFloat = goalHours.toFloatOrNull() ?: 0f
            val goalPercentage = if (goalHoursFloat > 0) {
                (totalHours / goalHoursFloat) * 100
            } else {
                0f
            }
            
            // Contar estudios bíblicos únicos visitados este mes
            val bibleStudiesCount = serviceRecordsForMonth
                .filter { it.bibleStudyId.isNotBlank() }
                .map { it.bibleStudyId }
                .distinct()
                .count()
            
            // Agrupar por tipo de actividad
            val activitiesSummary = serviceRecordsForMonth
                .groupBy { it.activityType }
                .mapValues { (activityName, records) ->
                    ActivitySummary(
                        activityName = activityName,
                        totalHours = records.sumOf { it.hours.toDouble() }.toFloat(),
                        sessionCount = records.size
                    )
                }
            
            // Contar días trabajados
            val daysWorked = hoursMap.size
            
            MonthlyStatistics(
                yearMonth = yearMonthStr,
                totalHours = totalHours,
                goalHours = goalHoursFloat,
                goalPercentage = goalPercentage,
                bibleStudiesCount = bibleStudiesCount,
                activitiesSummary = activitiesSummary,
                daysWorked = daysWorked
            )
        }
    }
    
    /**
     * Obtiene las estadísticas para los últimos N meses
     */
    fun getHistoricalStatistics(monthsCount: Int = 4): Flow<List<MonthlyStatistics>> = flow {
        val currentYearMonth = YearMonth.now()
        val statistics = mutableListOf<MonthlyStatistics>()
        
        // Obtener estadísticas para cada mes
        for (i in 0 until monthsCount) {
            val targetYearMonth = currentYearMonth.minusMonths(i.toLong())
            val monthStats = getMonthlyStatistics(
                targetYearMonth.year,
                targetYearMonth.monthValue
            ).first()
            statistics.add(monthStats)
        }
        
        emit(statistics)
    }
    
    /**
     * Obtiene las entradas de calendario para un mes específico
     */
    fun getCalendarEntries(year: Int, month: Int): Flow<List<CalendarEntry>> {
        val yearMonth = YearMonth.of(year, month)
        
        return combine(
            productivityDataStore.getWorkedHoursForMonth(year, month),
            bibleStudyDataStore.serviceRecordsWithStudyFlow
        ) { hoursMap, allServiceRecords ->
            val entries = mutableListOf<CalendarEntry>()
            
            // Convertir el mapa de horas a entradas de calendario
            for (day in 1..yearMonth.lengthOfMonth()) {
                val date = LocalDate.of(year, month, day)
                val dateStr = date.format(DATE_FORMATTER)
                
                // Si hay horas registradas para este día
                if (hoursMap.containsKey(day)) {
                    val hours = hoursMap[day] ?: 0f
                    
                    // Buscar registros de servicio para esta fecha
                    val serviceRecordsForDay = allServiceRecords.filter {
                        try {
                            LocalDate.parse(it.date).isEqual(date)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    
                    if (serviceRecordsForDay.isNotEmpty()) {
                        // Si hay registros de servicio, crear una entrada por cada uno
                        for (record in serviceRecordsForDay) {
                            entries.add(
                                CalendarEntry(
                                    date = dateStr,
                                    hours = record.hours,
                                    activityType = record.activityType,
                                    bibleStudyId = record.bibleStudyId,
                                    hasNotes = false // Por ahora no hay notas vinculadas
                                )
                            )
                        }
                    } else {
                        // Si no hay registros de servicio pero sí horas, crear una entrada genérica
                        entries.add(
                            CalendarEntry(
                                date = dateStr,
                                hours = hours,
                                activityType = "",
                                bibleStudyId = "",
                                hasNotes = false
                            )
                        )
                    }
                }
            }
            
            entries
        }
    }
    
    /**
     * Genera un informe mensual completo
     */
    fun generateMonthlyReport(year: Int, month: Int): Flow<MonthlyReport> {
        return combine(
            getMonthlyStatistics(year, month),
            getCalendarEntries(year, month)
        ) { stats, entries ->
            MonthlyReport(
                yearMonth = stats.yearMonth,
                totalHours = stats.totalHours,
                goalHours = stats.goalHours,
                goalPercentage = stats.goalPercentage,
                bibleStudiesCount = stats.bibleStudiesCount,
                activitiesSummary = stats.activitiesSummary,
                calendarEntries = entries,
                notes = emptyList() // Por ahora no hay notas
            )
        }
    }
}