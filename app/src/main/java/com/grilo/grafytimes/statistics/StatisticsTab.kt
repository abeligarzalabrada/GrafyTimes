package com.grilo.grafytimes.statistics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum que define las pestañas de navegación en la pantalla de estadísticas
 */
enum class StatisticsTab(val title: String, val icon: ImageVector) {
    MONTHLY("Mensual", Icons.Default.ShowChart),
    HISTORICAL("Histórico", Icons.Default.History),
    CALENDAR("Calendario", Icons.Default.CalendarMonth),
    REPORT("Informe", Icons.Default.Summarize)
}