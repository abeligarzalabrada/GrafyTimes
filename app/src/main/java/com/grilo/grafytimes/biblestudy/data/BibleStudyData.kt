package com.grilo.grafytimes.biblestudy.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Modelo de datos para almacenar la información de un estudio bíblico
 */
@Serializable
data class BibleStudy(
    val id: String = "", // ID único para el estudio bíblico
    val name: String = "", // Nombre de la persona
    val contactInfo: String = "", // Información de contacto o notas personales
    val createdAt: String = LocalDateTime.now().toString(), // Fecha de creación
    val lastVisitDate: String = "", // Fecha de la última visita
    val isActive: Boolean = true // Si el estudio está activo o no
)

/**
 * Modelo para vincular un estudio bíblico a un registro de servicio
 */
@Serializable
data class ServiceRecordWithStudy(
    val id: String = "", // Unique ID for the service record
    val date: String = "", // Fecha del registro
    val hours: Float = 0f, // Horas trabajadas
    val activityType: String = "", // Tipo de actividad
    val bibleStudyId: String = "" // ID del estudio bíblico vinculado
)

