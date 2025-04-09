package com.grilo.grafytimes.productivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grilo.grafytimes.productivity.components.ProgressStatusCard
import com.grilo.grafytimes.productivity.components.WorkHoursEntryCard
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility

@Composable
fun ProductivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductivityViewModel = viewModel()
) {
    val currentDate = LocalDate.now()
    val monthName = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = currentDate.year
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month and year header
            Text(
                text = "$monthName $year",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Estado de progreso
            if (viewModel.monthlyGoalHours.isNotEmpty()) {
                ProgressStatusCard(
                    progressStatus = viewModel.progressStatus,
                    totalWorkedHours = viewModel.totalWorkedHours,
                    goalHours = viewModel.monthlyGoalHours,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Registro de horas trabajadas
            if (viewModel.monthlyGoalHours.isNotEmpty() && viewModel.dailyRequiredHours > 0) {
                WorkHoursEntryCard(
                    hoursToday = viewModel.hoursToday,
                    onHoursTodayChange = { viewModel.hoursToday = it },
                    onRegisterHours = { viewModel.registerHoursToday() },
                    dailyRequiredHours = viewModel.dailyRequiredHours,
                    availableActivities = viewModel.availableActivities,
                    selectedActivity = viewModel.selectedActivity,
                    onActivitySelected = { viewModel.selectedActivity = it },
                    // Parámetros para estudios bíblicos
                    bibleStudies = viewModel.bibleStudies,
                    selectedBibleStudy = viewModel.selectedBibleStudy,
                    onBibleStudySelected = { viewModel.selectBibleStudy(it) },
                    linkBibleStudyToRecord = viewModel.linkBibleStudyToRecord,
                    onToggleBibleStudyLink = { viewModel.toggleBibleStudyLink() },
                    // Parámetros para calculadora de tiempo
                    useTimeCalculator = viewModel.useTimeCalculator,
                    onToggleTimeCalculator = { viewModel.toggleTimeCalculator() },
                    onTimeCalculated = { viewModel.setHoursFromCalculator(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Monthly Goal Input
            OutlinedTextField(
                value = viewModel.monthlyGoalHours,
                onValueChange = { 
                    viewModel.monthlyGoalHours = it
                    // Only calculate if there's valid input
                    if (it.isNotEmpty()) {
                        viewModel.calculateDailyHours()
                    }
                },
                label = { Text("Meta de horas mensuales") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Button to calculate
            Button(
                onClick = { viewModel.calculateDailyHours() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular horas diarias")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display result
            if (viewModel.dailyRequiredHours > 0) {
                ResultCard(
                    monthlyGoal = viewModel.monthlyGoalHours,
                    dailyHours = viewModel.dailyRequiredHours,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ResultCard(
    monthlyGoal: String,
    dailyHours: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Para cumplir tu meta de $monthlyGoal horas este mes:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Necesitas trabajar",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$dailyHours horas por día",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Get current date info
            val currentDate = remember { LocalDate.now() }
            val formattedDate = remember {
                currentDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es")))
            }
            
            Text(
                text = "Fecha: $formattedDate",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}