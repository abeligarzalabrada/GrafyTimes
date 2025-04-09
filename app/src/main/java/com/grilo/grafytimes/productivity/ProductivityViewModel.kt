package com.grilo.grafytimes.productivity

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import com.grilo.grafytimes.biblestudy.data.BibleStudyDataStore
import com.grilo.grafytimes.biblestudy.data.ServiceRecordWithStudy
import com.grilo.grafytimes.productivity.data.ProductivityDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil
import kotlin.math.max

class ProductivityViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = ProductivityDataStore(application)
    private val userDataStore = com.grilo.grafytimes.user.data.UserDataStore(application)
    private val bibleStudyDataStore = BibleStudyDataStore(application)
    
    // Estados UI observables
    var monthlyGoalHours by mutableStateOf("")
    var dailyRequiredHours by mutableStateOf(0f)
    var hoursToday by mutableStateOf("")
    var totalWorkedHours by mutableStateOf(0f)
    var progressStatus by mutableStateOf(ProgressStatus.NOT_SET)
    var workedHoursMap by mutableStateOf<Map<Int, Float>>(emptyMap())
    var availableActivities by mutableStateOf<Set<com.grilo.grafytimes.user.data.ServiceActivity>>(emptySet())
    var selectedActivity by mutableStateOf<com.grilo.grafytimes.user.data.ServiceActivity?>(null)
    
    // Estados para estudios bíblicos
    var bibleStudies by mutableStateOf<List<BibleStudy>>(emptyList())
    var selectedBibleStudy by mutableStateOf<BibleStudy?>(null)
    var linkBibleStudyToRecord by mutableStateOf(false)
    
    // Estado para calculadora de tiempo
    var useTimeCalculator by mutableStateOf(false)
    
    init {
        // Cargar datos guardados
        viewModelScope.launch {
            dataStore.monthlyGoalFlow.collect { savedGoal ->
                monthlyGoalHours = savedGoal
                if (savedGoal.isNotEmpty()) {
                    calculateDailyHours()
                }
            }
        }
        
        viewModelScope.launch {
            val currentDate = LocalDate.now()
            dataStore.getWorkedHoursForMonth(currentDate.year, currentDate.monthValue)
                .collect { hoursMap ->
                    workedHoursMap = hoursMap
                    totalWorkedHours = hoursMap.values.sum()
                    updateProgressStatus()
                }
        }
        
        // Cargar actividades disponibles del usuario
        viewModelScope.launch {
            userDataStore.userDataFlow.collect { userData ->
                availableActivities = userData.activities
                // Seleccionar la primera actividad por defecto si hay disponibles
                if (selectedActivity == null && availableActivities.isNotEmpty()) {
                    selectedActivity = availableActivities.first()
                }
            }
        }
        
        // Cargar estudios bíblicos disponibles
        viewModelScope.launch {
            bibleStudyDataStore.bibleStudiesFlow.collect { studies ->
                bibleStudies = studies
            }
        }
    }
    
    // Calcular horas diarias basado en meta mensual
    fun calculateDailyHours() {
        val goalHours = monthlyGoalHours.toFloatOrNull() ?: return
        
        // Guardar la meta en DataStore
        viewModelScope.launch {
            dataStore.saveMonthlyGoal(monthlyGoalHours)
        }
        
        // Obtener mes y año actual
        val currentYearMonth = YearMonth.now()
        val daysInMonth = currentYearMonth.lengthOfMonth()
        
        // Obtener día actual del mes
        val currentDay = LocalDate.now().dayOfMonth
        
        // Calcular días restantes del mes (incluyendo hoy)
        val remainingDays = daysInMonth - currentDay + 1
        
        // Calcular horas pendientes (meta total - ya trabajadas)
        val pendingHours = max(0f, goalHours - totalWorkedHours)
        
        // Calcular horas necesarias por día
        dailyRequiredHours = if (remainingDays > 0) {
            ceil(pendingHours / remainingDays * 10) / 10 // Redondeo a 1 decimal
        } else {
            0f
        }
        
        updateProgressStatus()
    }
    
    // Registrar horas trabajadas hoy
    fun registerHoursToday() {
        val hours = hoursToday.toFloatOrNull() ?: return
        val today = LocalDate.now()
        
        // Verificar que se haya seleccionado un tipo de actividad
        if (selectedActivity == null && availableActivities.isNotEmpty()) {
            selectedActivity = availableActivities.first()
        }
        
        viewModelScope.launch {
            // Guardar las horas trabajadas con el tipo de actividad
            dataStore.saveWorkedHours(today, hours)
            
            // Si se ha vinculado un estudio bíblico, guardar el registro con el estudio
            if (linkBibleStudyToRecord && selectedBibleStudy != null) {
                val serviceRecord = ServiceRecordWithStudy(
                    date = today.toString(),
                    hours = hours,
                    activityType = selectedActivity?.name ?: "",
                    bibleStudyId = selectedBibleStudy?.id ?: ""
                )
                bibleStudyDataStore.saveServiceRecordWithStudy(serviceRecord)
            }
            
            // Actualizar el mapa y el total
            val updatedMap = workedHoursMap.toMutableMap()
            updatedMap[today.dayOfMonth] = hours
            workedHoursMap = updatedMap
            totalWorkedHours = updatedMap.values.sum()
            
            // Recalcular el estado
            calculateDailyHours()
            updateProgressStatus()
            
            // Limpiar el campo de horas y resetear selección de estudio
            hoursToday = ""
            linkBibleStudyToRecord = false
            selectedBibleStudy = null
        }
    }
    
    // Actualizar el estado de progreso
    private fun updateProgressStatus() {
        if (monthlyGoalHours.isEmpty()) {
            progressStatus = ProgressStatus.NOT_SET
            return
        }
        
        val goal = monthlyGoalHours.toFloatOrNull() ?: return
        
        // Calcular el progreso esperado hasta hoy
        val currentDay = LocalDate.now().dayOfMonth
        val daysInMonth = YearMonth.now().lengthOfMonth()
        val expectedProgress = goal * currentDay / daysInMonth
        
        progressStatus = when {
            totalWorkedHours >= goal -> ProgressStatus.COMPLETED
            totalWorkedHours >= expectedProgress -> ProgressStatus.AHEAD
            totalWorkedHours < expectedProgress -> ProgressStatus.BEHIND
            else -> ProgressStatus.NOT_SET
        }
    }
    
    // Métodos para la gestión de estudios bíblicos
    fun toggleBibleStudyLink() {
        linkBibleStudyToRecord = !linkBibleStudyToRecord
        if (!linkBibleStudyToRecord) {
            selectedBibleStudy = null
        }
    }
    
    fun selectBibleStudy(study: BibleStudy?) {
        selectedBibleStudy = study
    }
    
    // Método para la calculadora de tiempo
    fun toggleTimeCalculator() {
        useTimeCalculator = !useTimeCalculator
    }
    
    fun setHoursFromCalculator(hours: Float) {
        hoursToday = hours.toString()
    }
}

enum class ProgressStatus {
    NOT_SET,
    AHEAD,
    BEHIND,
    COMPLETED
}