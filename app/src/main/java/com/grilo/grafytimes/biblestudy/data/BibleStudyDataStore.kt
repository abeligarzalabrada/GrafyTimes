package com.grilo.grafytimes.biblestudy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// Singleton para acceder al DataStore de estudios bíblicos
val Context.bibleStudyDataStore: DataStore<Preferences> by preferencesDataStore(name = "bible_study_preferences")

class BibleStudyDataStore(private val context: Context) {
    
    companion object {
        private val BIBLE_STUDIES_KEY = stringPreferencesKey("bible_studies")
        private val SERVICE_RECORDS_WITH_STUDY_KEY = stringPreferencesKey("service_records_with_study")
    }
    
    // Guardar un estudio bíblico
    suspend fun saveBibleStudy(bibleStudy: BibleStudy) {
        context.bibleStudyDataStore.edit { preferences ->
            // Obtener la lista actual de estudios
            val currentStudiesJson = preferences[BIBLE_STUDIES_KEY] ?: "[]"
            val currentStudies = Json.decodeFromString<List<BibleStudy>>(currentStudiesJson).toMutableList()
            
            // Si el estudio ya existe (tiene ID), actualizarlo
            val updatedStudy = if (bibleStudy.id.isBlank()) {
                bibleStudy.copy(id = UUID.randomUUID().toString())
            } else {
                bibleStudy
            }
            
            // Buscar y reemplazar o añadir
            val index = currentStudies.indexOfFirst { it.id == updatedStudy.id }
            if (index >= 0) {
                currentStudies[index] = updatedStudy
            } else {
                currentStudies.add(updatedStudy)
            }
            
            // Guardar la lista actualizada
            preferences[BIBLE_STUDIES_KEY] = Json.encodeToString(currentStudies)
        }
    }
    
    // Eliminar un estudio bíblico
    suspend fun deleteBibleStudy(studyId: String) {
        context.bibleStudyDataStore.edit { preferences ->
            // Obtener la lista actual de estudios
            val currentStudiesJson = preferences[BIBLE_STUDIES_KEY] ?: "[]"
            val currentStudies = Json.decodeFromString<List<BibleStudy>>(currentStudiesJson).toMutableList()
            
            // Eliminar el estudio
            currentStudies.removeIf { it.id == studyId }
            
            // Guardar la lista actualizada
            preferences[BIBLE_STUDIES_KEY] = Json.encodeToString(currentStudies)
            
            // También eliminar los registros de servicio asociados a este estudio
            val recordsJson = preferences[SERVICE_RECORDS_WITH_STUDY_KEY] ?: "[]"
            val records = Json.decodeFromString<List<ServiceRecordWithStudy>>(recordsJson).toMutableList()
            records.removeIf { it.bibleStudyId == studyId }
            preferences[SERVICE_RECORDS_WITH_STUDY_KEY] = Json.encodeToString(records)
        }
    }
    
    // Obtener todos los estudios bíblicos
    val bibleStudiesFlow: Flow<List<BibleStudy>> = context.bibleStudyDataStore.data.map { preferences ->
        val studiesJson = preferences[BIBLE_STUDIES_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<BibleStudy>>(studiesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Obtener todos los registros de servicio con estudios
    val serviceRecordsWithStudyFlow: Flow<List<ServiceRecordWithStudy>> = context.bibleStudyDataStore.data.map { preferences ->
        val recordsJson = preferences[SERVICE_RECORDS_WITH_STUDY_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<ServiceRecordWithStudy>>(recordsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Obtener los registros de servicio para un estudio bíblico específico
    fun getServiceRecordsForStudy(studyId: String): Flow<List<ServiceRecordWithStudy>> {
        return serviceRecordsWithStudyFlow.map { records ->
            records.filter { it.bibleStudyId == studyId }
        }
    }
    
    // Exportar datos de estudios bíblicos a JSON
    suspend fun exportBibleStudiesToJson(): String {
        val studies = bibleStudiesFlow.first()
        val records = serviceRecordsWithStudyFlow.first()
        val exportData = mapOf(
            "studies" to studies,
            "records" to records
        )
        return Json.encodeToString(exportData)
    }

    // Importar datos de estudios bíblicos desde JSON
    suspend fun importBibleStudiesFromJson(jsonData: String): Boolean {
        return try {
            val importData = Json.decodeFromString<Map<String, Any>>(jsonData)
            val studies = Json.decodeFromString<List<BibleStudy>>(
                Json.encodeToString(importData["studies"])
            )
            val records = Json.decodeFromString<List<ServiceRecordWithStudy>>(
                Json.encodeToString(importData["records"])
            )
            
            context.bibleStudyDataStore.edit { preferences ->
                preferences[BIBLE_STUDIES_KEY] = Json.encodeToString(studies)
                preferences[SERVICE_RECORDS_WITH_STUDY_KEY] = Json.encodeToString(records)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Guardar un registro de servicio con estudio bíblico
    suspend fun saveServiceRecordWithStudy(record: ServiceRecordWithStudy) {
        context.bibleStudyDataStore.edit { preferences ->
            val recordsJson = preferences[SERVICE_RECORDS_WITH_STUDY_KEY] ?: "[]"
            val currentRecords = Json.decodeFromString<List<ServiceRecordWithStudy>>(recordsJson).toMutableList()
            
            // Si el registro ya existe, actualizarlo; si no, añadirlo
            val index = currentRecords.indexOfFirst { it.id == record.id }
            if (index >= 0) {
                currentRecords[index] = record
            } else {
                currentRecords.add(record)
            }
            
            preferences[SERVICE_RECORDS_WITH_STUDY_KEY] = Json.encodeToString(currentRecords)
        }
    }
}