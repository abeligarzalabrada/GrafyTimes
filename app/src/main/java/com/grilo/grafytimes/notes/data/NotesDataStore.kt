package com.grilo.grafytimes.notes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// Singleton para acceder al DataStore de notas
val Context.notesDataStore: DataStore<Preferences> by preferencesDataStore(name = "notes_preferences")

class NotesDataStore(private val context: Context) {
    
    companion object {
        private val NOTES_KEY = stringPreferencesKey("user_notes")
    }
    
    // Guardar una nota
    suspend fun saveNote(note: Note) {
        context.notesDataStore.edit { preferences ->
            // Obtener la lista actual de notas
            val currentNotesJson = preferences[NOTES_KEY] ?: "[]"
            val currentNotes = Json.decodeFromString<List<Note>>(currentNotesJson).toMutableList()
            
            // Si la nota ya existe (tiene ID), actualizarla
            val updatedNote = if (note.id.isBlank()) {
                note.copy(
                    id = UUID.randomUUID().toString(),
                    createdAt = java.time.LocalDateTime.now().toString()
                )
            } else {
                note.copy(updatedAt = java.time.LocalDateTime.now().toString())
            }
            
            // Buscar y reemplazar o añadir
            val index = currentNotes.indexOfFirst { it.id == updatedNote.id }
            if (index >= 0) {
                currentNotes[index] = updatedNote
            } else {
                currentNotes.add(updatedNote)
            }
            
            // Guardar la lista actualizada
            preferences[NOTES_KEY] = Json.encodeToString(currentNotes)
        }
    }
    
    // Eliminar una nota
    suspend fun deleteNote(noteId: String) {
        context.notesDataStore.edit { preferences ->
            // Obtener la lista actual de notas
            val currentNotesJson = preferences[NOTES_KEY] ?: "[]"
            val currentNotes = Json.decodeFromString<List<Note>>(currentNotesJson).toMutableList()
            
            // Eliminar la nota
            currentNotes.removeIf { it.id == noteId }
            
            // Guardar la lista actualizada
            preferences[NOTES_KEY] = Json.encodeToString(currentNotes)
        }
    }
    
    // Obtener todas las notas
    val notesFlow: Flow<List<Note>> = context.notesDataStore.data.map { preferences ->
        val notesJson = preferences[NOTES_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<Note>>(notesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Obtener notas filtradas por tipo de actividad
    fun getNotesByActivityType(activityType: String): Flow<List<Note>> {
        return notesFlow.map { notes ->
            if (activityType.isBlank()) {
                notes // Si no hay filtro, devolver todas
            } else {
                notes.filter { it.activityType == activityType }
            }
        }
    }
}