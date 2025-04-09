package com.grilo.grafytimes.biblestudy

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import com.grilo.grafytimes.biblestudy.data.BibleStudyDataStore
import com.grilo.grafytimes.biblestudy.data.ServiceRecordWithStudy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BibleStudyViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = BibleStudyDataStore(application)
    
    // Estados UI observables
    var bibleStudies by mutableStateOf<List<BibleStudy>>(emptyList())
    var serviceRecords by mutableStateOf<List<ServiceRecordWithStudy>>(emptyList())
    
    init {
        // Cargar estudios bíblicos
        viewModelScope.launch {
            dataStore.bibleStudiesFlow.collect { studies ->
                bibleStudies = studies
            }
        }
        
        // Cargar registros de servicio con estudios
        viewModelScope.launch {
            dataStore.serviceRecordsWithStudyFlow.collect { records ->
                serviceRecords = records
            }
        }
    }
    
    // Guardar un estudio bíblico
    fun saveBibleStudy(study: BibleStudy) {
        viewModelScope.launch {
            dataStore.saveBibleStudy(study)
        }
    }
    
    // Eliminar un estudio bíblico
    fun deleteBibleStudy(studyId: String) {
        viewModelScope.launch {
            dataStore.deleteBibleStudy(studyId)
        }
    }
    
    // Obtener registros para un estudio específico
    fun getRecordsForStudy(studyId: String) {
        viewModelScope.launch {
            dataStore.getServiceRecordsForStudy(studyId).collect { records ->
                serviceRecords = records
            }
        }
    }
    
    // Exportar datos de estudios bíblicos a JSON
    suspend fun exportBibleStudiesToJson(): String {
        return dataStore.exportBibleStudiesToJson()
    }

    // Importar datos de estudios bíblicos desde JSON
    suspend fun importBibleStudiesFromJson(jsonData: String): Boolean {
        return dataStore.importBibleStudiesFromJson(jsonData)
    }
}