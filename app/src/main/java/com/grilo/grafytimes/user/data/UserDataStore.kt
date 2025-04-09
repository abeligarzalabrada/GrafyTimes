package com.grilo.grafytimes.user.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Singleton para acceder al DataStore de usuario
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserDataStore(private val context: Context) {
    
    companion object {
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val SERVICE_PRIVILEGE_KEY = stringPreferencesKey("service_privilege")
        private val ACTIVITIES_KEY = stringSetPreferencesKey("service_activities")
        private val MONTHLY_GOAL_KEY = stringPreferencesKey("monthly_goal")
        private val IS_CONFIGURED_KEY = booleanPreferencesKey("is_configured")
    }
    
    // Guardar los datos del usuario
    suspend fun saveUserData(userData: UserData) {
        context.userDataStore.edit { preferences ->
            preferences[NAME_KEY] = userData.name
            preferences[SERVICE_PRIVILEGE_KEY] = userData.servicePrivilege.name
            preferences[ACTIVITIES_KEY] = userData.activities.map { it.name }.toSet()
            preferences[MONTHLY_GOAL_KEY] = userData.monthlyGoalHours
            preferences[IS_CONFIGURED_KEY] = userData.isConfigured
        }
    }
    
    // Obtener los datos del usuario como Flow
    val userDataFlow: Flow<UserData> = context.userDataStore.data.map { preferences ->
        val name = preferences[NAME_KEY] ?: ""
        val servicePrivilegeStr = preferences[SERVICE_PRIVILEGE_KEY] ?: ServicePrivilege.BAPTIZED_PUBLISHER.name
        val activitiesSet = preferences[ACTIVITIES_KEY] ?: emptySet()
        val monthlyGoal = preferences[MONTHLY_GOAL_KEY] ?: ""
        val isConfigured = preferences[IS_CONFIGURED_KEY] ?: false
        
        // Convertir strings a enums
        val servicePrivilege = try {
            ServicePrivilege.valueOf(servicePrivilegeStr)
        } catch (e: Exception) {
            ServicePrivilege.BAPTIZED_PUBLISHER
        }
        
        val activities = activitiesSet.mapNotNull { activityStr ->
            try {
                ServiceActivity.valueOf(activityStr)
            } catch (e: Exception) {
                null
            }
        }.toSet()
        
        UserData(
            name = name,
            servicePrivilege = servicePrivilege,
            activities = activities,
            monthlyGoalHours = monthlyGoal,
            isConfigured = isConfigured
        )
    }
    
    // Verificar si el usuario ya ha configurado la app
    val isConfiguredFlow: Flow<Boolean> = context.userDataStore.data.map { preferences ->
        preferences[IS_CONFIGURED_KEY] ?: false
    }
    
    // Actualizar la meta mensual de horas
    suspend fun updateMonthlyGoal(goal: String) {
        context.userDataStore.edit { preferences ->
            preferences[MONTHLY_GOAL_KEY] = goal
        }
    }
}