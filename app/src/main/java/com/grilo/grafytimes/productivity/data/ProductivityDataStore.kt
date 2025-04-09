package com.grilo.grafytimes.productivity.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Singleton para acceder al DataStore
val Context.productivityDataStore: DataStore<Preferences> by preferencesDataStore(name = "productivity_preferences")

class ProductivityDataStore(private val context: Context) {
    
    companion object {
        private val MONTHLY_GOAL_KEY = stringPreferencesKey("monthly_goal")
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        // Función para crear una clave para horas trabajadas por día
        private fun getWorkedHoursKey(date: LocalDate): Preferences.Key<Float> {
            return floatPreferencesKey("worked_hours_${date.format(DATE_FORMAT)}")
        }
    }
    
    // Guardar la meta mensual
    suspend fun saveMonthlyGoal(goal: String) {
        context.productivityDataStore.edit { preferences ->
            preferences[MONTHLY_GOAL_KEY] = goal
        }
    }
    
    // Obtener la meta mensual
    val monthlyGoalFlow: Flow<String> = context.productivityDataStore.data.map { preferences ->
        preferences[MONTHLY_GOAL_KEY] ?: ""
    }
    
    // Guardar horas trabajadas para un día específico
    suspend fun saveWorkedHours(date: LocalDate, hours: Float) {
        context.productivityDataStore.edit { preferences ->
            preferences[getWorkedHoursKey(date)] = hours
        }
    }
    
    // Obtener horas trabajadas para un mes específico como un mapa de día -> horas
    fun getWorkedHoursForMonth(yearMonth: Int, monthValue: Int): Flow<Map<Int, Float>> {
        return context.productivityDataStore.data.map { preferences ->
            val result = mutableMapOf<Int, Float>()
            
            // Iterar por todos los días del mes
            for (day in 1..31) { // Máximo de días en un mes
                try {
                    val date = LocalDate.of(yearMonth, monthValue, day)
                    val key = getWorkedHoursKey(date)
                    val hours = preferences[key] ?: 0f
                    if (hours > 0) {
                        result[day] = hours
                    }
                } catch (e: Exception) {
                    // Ignorar fechas inválidas (como 31 de febrero)
                }
            }
            
            result
        }
    }
    
    // Obtener el total de horas trabajadas en el mes actual
    fun getTotalWorkedHoursForCurrentMonth(): Flow<Float> {
        val currentDate = LocalDate.now()
        return getWorkedHoursForMonth(currentDate.year, currentDate.monthValue)
            .map { hoursMap -> hoursMap.values.sum() }
    }
} 