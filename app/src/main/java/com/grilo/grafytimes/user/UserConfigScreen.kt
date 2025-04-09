package com.grilo.grafytimes.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grilo.grafytimes.user.data.ServiceActivity
import com.grilo.grafytimes.user.data.ServicePrivilege

@Composable
fun UserConfigScreen(
    onConfigurationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
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
            // Título
            Text(
                text = "Configuración Inicial",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Nombre
            OutlinedTextField(
                value = viewModel.userName,
                onValueChange = { viewModel.userName = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Privilegios de servicio
            ServicePrivilegeSelector(
                selectedPrivilege = viewModel.selectedPrivilege,
                onPrivilegeSelected = { viewModel.selectedPrivilege = it }
            )
            
            // Actividades
            ServiceActivitiesSelector(
                selectedActivities = viewModel.selectedActivities,
                onActivityToggle = { viewModel.toggleActivity(it) }
            )
            
            // Meta mensual
            OutlinedTextField(
                value = viewModel.monthlyGoalHours,
                onValueChange = { viewModel.monthlyGoalHours = it },
                label = { Text("Meta de horas mensuales") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    viewModel.saveUserConfiguration()
                    onConfigurationComplete()
                },
                enabled = viewModel.userName.isNotBlank() && 
                         viewModel.selectedActivities.isNotEmpty() && 
                         viewModel.monthlyGoalHours.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar configuración")
            }
        }
    }
}

@Composable
fun ServicePrivilegeSelector(
    selectedPrivilege: ServicePrivilege,
    onPrivilegeSelected: (ServicePrivilege) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Privilegio de servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(modifier = Modifier.selectableGroup()) {
                ServicePrivilege.values().forEach { privilege ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (privilege == selectedPrivilege),
                                onClick = { onPrivilegeSelected(privilege) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (privilege == selectedPrivilege),
                            onClick = null // null porque ya se maneja en el selectable
                        )
                        Text(
                            text = getPrivilegeName(privilege),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceActivitiesSelector(
    selectedActivities: Set<ServiceActivity>,
    onActivityToggle: (ServiceActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Actividades que realizas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                ServiceActivity.values().forEach { activity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedActivities.contains(activity),
                            onCheckedChange = { onActivityToggle(activity) }
                        )
                        Text(
                            text = getActivityName(activity),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// Función para obtener el nombre legible del privilegio de servicio
fun getPrivilegeName(privilege: ServicePrivilege): String {
    return when (privilege) {
        ServicePrivilege.UNBAPTIZED_PUBLISHER -> "Publicador no bautizado"
        ServicePrivilege.BAPTIZED_PUBLISHER -> "Publicador bautizado"
        ServicePrivilege.AUXILIARY_PIONEER -> "Precursor auxiliar"
        ServicePrivilege.REGULAR_PIONEER -> "Precursor regular"
        ServicePrivilege.SPECIAL_PIONEER -> "Precursor especial"
        ServicePrivilege.OTHER -> "Otros privilegios"
    }
}

// Función para obtener el nombre legible de la actividad
fun getActivityName(activity: ServiceActivity): String {
    return when (activity) {
        ServiceActivity.FIELD_SERVICE -> "Servicio del campo"
        ServiceActivity.FAMILY_WORSHIP -> "Adoración en familia"
        ServiceActivity.PUBLIC_WITNESSING -> "Predicación pública"
        ServiceActivity.INFORMAL_WITNESSING -> "Informal"
        ServiceActivity.ISOLATED_TERRITORIES -> "Territorios aislados"
        ServiceActivity.LETTER_WRITING -> "Predicación por cartas"
    }
}