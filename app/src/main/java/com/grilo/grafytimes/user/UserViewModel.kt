package com.grilo.grafytimes.user

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grilo.grafytimes.user.data.ServiceActivity
import com.grilo.grafytimes.user.data.ServicePrivilege
import com.grilo.grafytimes.user.data.UserData
import com.grilo.grafytimes.user.data.UserDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = UserDataStore(application)
    
    // Estados UI observables
    var userName by mutableStateOf("")
    var selectedPrivilege by mutableStateOf(ServicePrivilege.BAPTIZED_PUBLISHER)
    var selectedActivities by mutableStateOf<Set<ServiceActivity>>(emptySet())
    var monthlyGoalHours by mutableStateOf("")
    
    // Expose these as State<Boolean> for property delegation in MainActivity
    private val _isConfigured = mutableStateOf(false)
    val isConfigured: State<Boolean> = _isConfigured
    
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    
    init {
        // Verificar si el usuario ya ha configurado la app
        viewModelScope.launch {
            dataStore.isConfiguredFlow.collect { configured ->
                _isConfigured.value = configured
                
                // Si ya está configurado, cargar los datos del usuario
                if (configured) {
                    val userData = dataStore.userDataFlow.first()
                    userName = userData.name
                    selectedPrivilege = userData.servicePrivilege
                    selectedActivities = userData.activities
                    monthlyGoalHours = userData.monthlyGoalHours
                }
                
                _isLoading.value = false
            }
        }
    }
    
    // Guardar la configuración del usuario
    fun saveUserConfiguration() {
        if (userName.isBlank() || selectedActivities.isEmpty() || monthlyGoalHours.isBlank()) {
            return // No guardar si faltan datos esenciales
        }
        
        val userData = UserData(
            name = userName,
            servicePrivilege = selectedPrivilege,
            activities = selectedActivities,
            monthlyGoalHours = monthlyGoalHours,
            isConfigured = true
        )
        
        viewModelScope.launch {
            dataStore.saveUserData(userData)
            _isConfigured.value = true
        }
    }
    
    // Actualizar la meta mensual de horas
    fun updateMonthlyGoal(goal: String) {
        if (goal.isBlank()) return
        
        monthlyGoalHours = goal
        viewModelScope.launch {
            dataStore.updateMonthlyGoal(goal)
        }
    }
    
    // Alternar una actividad (seleccionar/deseleccionar)
    fun toggleActivity(activity: ServiceActivity) {
        selectedActivities = if (selectedActivities.contains(activity)) {
            selectedActivities - activity
        } else {
            selectedActivities + activity
        }
    }
}