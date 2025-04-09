package com.grilo.grafytimes.productivity.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.grilo.grafytimes.ui.theme.AnimationDuration
import com.grilo.grafytimes.user.data.ServiceActivity
import com.grilo.grafytimes.user.getActivityName

@Composable
fun ActivityTypeSelector(
    availableActivities: Set<ServiceActivity>,
    selectedActivity: ServiceActivity?,
    onActivitySelected: (ServiceActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Tipo de actividad:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (availableActivities.isEmpty()) {
            Text(
                text = "No hay actividades configuradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            availableActivities.forEach { activity ->
                val isSelected = activity == selectedActivity
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = tween(durationMillis = AnimationDuration.SHORT),
                    label = "activityScale"
                )
                
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(durationMillis = AnimationDuration.MEDIUM),
                    label = "textColor"
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { onActivitySelected(activity) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp)
                        .scale(scale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null // null porque ya se maneja en el selectable
                    )
                    Text(
                        text = getActivityName(activity),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}