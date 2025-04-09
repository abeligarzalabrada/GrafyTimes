package com.grilo.grafytimes.statistics

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import com.grilo.grafytimes.biblestudy.data.BibleStudyDataStore
import com.grilo.grafytimes.statistics.data.ActivitySummary
import com.grilo.grafytimes.statistics.data.CalendarEntry
import com.grilo.grafytimes.statistics.data.MonthlyReport
import com.grilo.grafytimes.statistics.data.MonthlyStatistics
import com.grilo.grafytimes.statistics.data.StatisticsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val statisticsDataStore = StatisticsDataStore(application)
    private val bibleStudyDataStore = BibleStudyDataStore(application)
    
    // Estados UI observables para estadísticas mensuales
    var currentYearMonth by mutableStateOf(YearMonth.now())
    var currentMonthStatistics by mutableStateOf<MonthlyStatistics?>(null)
    var historicalStatistics by mutableStateOf<List<MonthlyStatistics>>(emptyList())
    var calendarEntries by mutableStateOf<List<CalendarEntry>>(emptyList())
    var selectedDate by mutableStateOf<LocalDate?>(null)
    var selectedDateEntries by mutableStateOf<List<CalendarEntry>>(emptyList())
    var bibleStudiesMap by mutableStateOf<Map<String, BibleStudy>>(emptyMap())
    var monthlyReport by mutableStateOf<MonthlyReport?>(null)
    
    // Estado para exportación
    var reportText by mutableStateOf("")
    
    init {
        // Cargar estadísticas del mes actual
        loadCurrentMonthStatistics()
        
        // Cargar estadísticas históricas
        loadHistoricalStatistics()
        
        // Cargar entradas de calendario
        loadCalendarEntries()
        
        // Cargar estudios bíblicos para referencias
        loadBibleStudies()
    }
    
    // Cargar estadísticas del mes actual
    fun loadCurrentMonthStatistics() {
        viewModelScope.launch {
            statisticsDataStore.getMonthlyStatistics(
                currentYearMonth.year,
                currentYearMonth.monthValue
            ).collect { stats ->
                currentMonthStatistics = stats
            }
        }
    }
    
    // Cargar estadísticas históricas
    fun loadHistoricalStatistics(monthsCount: Int = 4) {
        viewModelScope.launch {
            statisticsDataStore.getHistoricalStatistics(monthsCount).collect { stats ->
                historicalStatistics = stats
            }
        }
    }
    
    // Cargar entradas de calendario
    fun loadCalendarEntries() {
        viewModelScope.launch {
            statisticsDataStore.getCalendarEntries(
                currentYearMonth.year,
                currentYearMonth.monthValue
            ).collect { entries ->
                calendarEntries = entries
            }
        }
    }
    
    // Cargar estudios bíblicos
    private fun loadBibleStudies() {
        viewModelScope.launch {
            bibleStudyDataStore.bibleStudiesFlow.collect { studies ->
                bibleStudiesMap = studies.associateBy { it.id }
            }
        }
    }
    
    // Cambiar al mes anterior
    fun previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1)
        loadCurrentMonthStatistics()
        loadCalendarEntries()
    }
    
    // Cambiar al mes siguiente
    fun nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1)
        loadCurrentMonthStatistics()
        loadCalendarEntries()
    }
    
    // Seleccionar una fecha en el calendario
    fun selectDate(date: LocalDate) {
        selectedDate = date
        selectedDateEntries = calendarEntries.filter {
            try {
                LocalDate.parse(it.date).isEqual(date)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Generar informe mensual
    fun generateMonthlyReport() {
        viewModelScope.launch {
            statisticsDataStore.generateMonthlyReport(
                currentYearMonth.year,
                currentYearMonth.monthValue
            ).collect { report ->
                monthlyReport = report
                generateReportText(report)
            }
        }
    }
    
    // Generar texto del informe para exportar
    private fun generateReportText(report: MonthlyReport) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val monthYearText = YearMonth.parse(report.yearMonth).format(formatter)
        
        val sb = StringBuilder()
        sb.appendLine("INFORME DE SERVICIO - $monthYearText")
        sb.appendLine("====================================")
        sb.appendLine()
        sb.appendLine("RESUMEN:")
        sb.appendLine("- Total de horas: ${report.totalHours}")
        sb.appendLine("- Meta mensual: ${report.goalHours} horas")
        sb.appendLine("- Porcentaje completado: ${String.format("%.1f", report.goalPercentage)}%")
        sb.appendLine("- Estudios bíblicos visitados: ${report.bibleStudiesCount}")
        sb.appendLine()
        
        sb.appendLine("ACTIVIDADES:")
        report.activitiesSummary.forEach { (_, summary) ->
            sb.appendLine("- ${summary.activityName}: ${summary.totalHours} horas en ${summary.sessionCount} salidas")
        }
        sb.appendLine()
        
        sb.appendLine("REGISTRO DIARIO:")
        val entriesByDate = report.calendarEntries.groupBy { it.date }
        entriesByDate.forEach { (dateStr, entries) ->
            try {
                val date = LocalDate.parse(dateStr)
                val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                sb.appendLine(dateFormatted)
                
                entries.forEach { entry ->
                    val activityInfo = if (entry.activityType.isNotBlank()) {
                        "[${entry.activityType}]"
                    } else {
                        ""
                    }
                    
                    val studyInfo = if (entry.bibleStudyId.isNotBlank() && bibleStudiesMap.containsKey(entry.bibleStudyId)) {
                        "- Estudio: ${bibleStudiesMap[entry.bibleStudyId]?.name ?: ""}"
                    } else {
                        ""
                    }
                    
                    sb.appendLine("  ${entry.hours} horas $activityInfo $studyInfo")
                }
                sb.appendLine()
            } catch (e: Exception) {
                // Ignorar fechas con formato incorrecto
            }
        }
        
        reportText = sb.toString()
    }
}