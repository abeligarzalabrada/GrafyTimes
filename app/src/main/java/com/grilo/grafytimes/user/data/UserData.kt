package com.grilo.grafytimes.user.data

import kotlinx.serialization.Serializable

/**
 * Modelo de datos para almacenar la información del usuario
 */
@Serializable
data class UserData(
    val name: String = "",
    val servicePrivilege: ServicePrivilege = ServicePrivilege.BAPTIZED_PUBLISHER,
    val activities: Set<ServiceActivity> = emptySet(),
    val monthlyGoalHours: String = "",
    val isConfigured: Boolean = false
)

/**
 * Enum que representa los privilegios de servicio disponibles
 */
enum class ServicePrivilege {
    UNBAPTIZED_PUBLISHER,   // Publicador no bautizado
    BAPTIZED_PUBLISHER,     // Publicador bautizado
    AUXILIARY_PIONEER,      // Precursor auxiliar
    REGULAR_PIONEER,        // Precursor regular
    SPECIAL_PIONEER,        // Precursor especial
    OTHER                   // Otros privilegios
}

/**
 * Enum que representa las actividades de servicio disponibles
 */
enum class ServiceActivity {
    FIELD_SERVICE,          // Servicio del campo
    FAMILY_WORSHIP,         // Adoración en familia
    PUBLIC_WITNESSING,      // Predicación pública
    INFORMAL_WITNESSING,    // Informal
    ISOLATED_TERRITORIES,   // Territorios aislados
    LETTER_WRITING          // Predicación por cartas
}