package com.grilo.grafytimes.productivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Componente para calcular el tiempo de servicio basado en hora de inicio y fin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCalculator(
    onTimeCalculated: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) } // Default 9:00 AM
    var endTime by remember { mutableStateOf(LocalTime.of(11, 0)) } // Default 11:00 AM
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Calculadora de tiempo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth()) {
            // Start time button
            OutlinedButton(
                onClick = { showStartTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Inicio: ${startTime.format(formatter)}")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // End time button
            OutlinedButton(
                onClick = { showEndTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Fin: ${endTime.format(formatter)}")
            }
        }
        
        // Calculate button
        Button(
            onClick = {
                val minutes = ChronoUnit.MINUTES.between(startTime, endTime)
                // Convert to hours with 1 decimal place precision
                val hours = (minutes / 6.0).roundToInt() / 10.0f
                onTimeCalculated(hours)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = !endTime.isBefore(startTime)
        ) {
            Text("Calcular tiempo")
        }
        
        // Show calculated time
        if (!endTime.isBefore(startTime)) {
            val minutes = ChronoUnit.MINUTES.between(startTime, endTime)
            val hours = (minutes / 6.0).roundToInt() / 10.0f
            
            Text(
                text = "Tiempo calculado: $hours horas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (endTime.isBefore(startTime)) {
            Text(
                text = "La hora de fin debe ser posterior a la de inicio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
    
    // Time pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                startTime = LocalTime.of(hour, minute)
                showStartTimePicker = false
            },
            initialHour = startTime.hour,
            initialMinute = startTime.minute
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                endTime = LocalTime.of(hour, minute)
                showEndTimePicker = false
            },
            initialHour = endTime.hour,
            initialMinute = endTime.minute
        )
    }
}

/**
 * Diálogo para seleccionar hora
 */
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Seleccionar hora") },
        text = {
            Column {
                // Hour selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hora: ")
                    Slider(
                        value = selectedHour.toFloat(),
                        onValueChange = { selectedHour = it.toInt() },
                        valueRange = 0f..23f,
                        steps = 23,
                        modifier = Modifier.weight(1f)
                    )
                    Text("$selectedHour", modifier = Modifier.width(30.dp))
                }
                
                // Minute selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Minuto: ")
                    Slider(
                        value = selectedMinute.toFloat(),
                        onValueChange = { selectedMinute = it.toInt() },
                        valueRange = 0f..59f,
                        steps = 11, // 5-minute increments
                        modifier = Modifier.weight(1f)
                    )
                    Text("$selectedMinute", modifier = Modifier.width(30.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}