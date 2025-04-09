package com.grilo.grafytimes.notes.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Modelo de datos para almacenar notas personales del usuario
 */
@Serializable
data class Note(
    val id: String = "", // ID único para la nota
    val title: String = "", // Título de la nota
    val content: String = "", // Contenido de la nota
    val createdAt: String = LocalDateTime.now().toString(), // Fecha de creación
    val updatedAt: String = LocalDateTime.now().toString(), // Fecha de última actualización
    val activityType: String = "" // Tipo de actividad relacionada (opcional)
)