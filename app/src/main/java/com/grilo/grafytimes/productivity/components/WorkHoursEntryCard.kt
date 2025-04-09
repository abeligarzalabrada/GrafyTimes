package com.grilo.grafytimes.productivity.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.grilo.grafytimes.ui.theme.ButtonStyle
import com.grilo.grafytimes.ui.theme.CardStyle
import com.grilo.grafytimes.ui.theme.TextFieldStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import com.grilo.grafytimes.user.data.ServiceActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkHoursEntryCard(
    hoursToday: String,
    onHoursTodayChange: (String) -> Unit,
    onRegisterHours: () -> Unit,
    dailyRequiredHours: Float,
    availableActivities: Set<ServiceActivity> = emptySet(),
    selectedActivity: ServiceActivity? = null,
    onActivitySelected: (ServiceActivity) -> Unit = {},
    // Nuevos parámetros para estudios bíblicos
    bibleStudies: List<BibleStudy> = emptyList(),
    selectedBibleStudy: BibleStudy? = null,
    onBibleStudySelected: (BibleStudy?) -> Unit = {},
    linkBibleStudyToRecord: Boolean = false,
    onToggleBibleStudyLink: () -> Unit = {},
    // Parámetros para calculadora de tiempo
    useTimeCalculator: Boolean = false,
    onToggleTimeCalculator: () -> Unit = {},
    onTimeCalculated: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    CardStyle.AnimatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Current date
            val currentDate = LocalDate.now()
            val formattedDate = currentDate.format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es"))
            )
            
            Text(
                text = formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Deberías trabajar $dailyRequiredHours horas hoy",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity type selector
            if (availableActivities.isNotEmpty()) {
                ActivityTypeSelector(
                    availableActivities = availableActivities,
                    selectedActivity = selectedActivity,
                    onActivitySelected = onActivitySelected
                )
            }
            
            // Opción para usar calculadora de tiempo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar calculadora de tiempo")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = useTimeCalculator,
                    onCheckedChange = { onToggleTimeCalculator() }
                )
            }
            
            // Mostrar calculadora de tiempo o entrada manual
            if (useTimeCalculator) {
                Spacer(modifier = Modifier.height(8.dp))
                TimeCalculator(
                    onTimeCalculated = onTimeCalculated,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Hours input (siempre visible, se actualiza con la calculadora)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextFieldStyle.AnimatedOutlinedTextField(
                    value = hoursToday,
                    onValueChange = onHoursTodayChange,
                    label = { Text("Horas trabajadas hoy") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                ButtonStyle.AnimatedButton(
                    onClick = onRegisterHours,
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = hoursToday.isNotBlank() && selectedActivity != null
                ) {
                    Text("Registrar")
                }
            }
            
            // Opción para vincular estudio bíblico
            if (bibleStudies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vincular a un estudio bíblico")
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = linkBibleStudyToRecord,
                        onCheckedChange = { onToggleBibleStudyLink() }
                    )
                }
                
                // Selector de estudio bíblico
                if (linkBibleStudyToRecord) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextFieldStyle.AnimatedOutlinedTextField(
                            value = selectedBibleStudy?.name ?: "Seleccionar estudio",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            bibleStudies.forEach { study ->
                                DropdownMenuItem(
                                    text = { Text(study.name) },
                                    onClick = {
                                        onBibleStudySelected(study)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}