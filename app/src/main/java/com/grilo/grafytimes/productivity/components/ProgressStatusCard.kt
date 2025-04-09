package com.grilo.grafytimes.productivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.grilo.grafytimes.ui.theme.CardStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grilo.grafytimes.R
import com.grilo.grafytimes.productivity.ProgressStatus

@Composable
fun ProgressStatusCard(
    progressStatus: ProgressStatus,
    totalWorkedHours: Float,
    goalHours: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, statusText, statusIcon, statusColor) = when (progressStatus) {
        ProgressStatus.AHEAD -> Quadruple(
            Color(0xFFE8F5E9),
            "¡Vas adelantado!",
            R.drawable.ic_trending_up,
            Color(0xFF4CAF50)
        )
        ProgressStatus.BEHIND -> Quadruple(
            Color(0xFFFFEBEE),
            "Vas un poco atrasado",
            R.drawable.ic_trending_down,
            Color(0xFFF44336)
        )
        ProgressStatus.COMPLETED -> Quadruple(
            Color(0xFFE3F2FD),
            "¡Meta completada!",
            R.drawable.ic_check_circle,
            Color(0xFF2196F3)
        )
        ProgressStatus.NOT_SET -> Quadruple(
            Color(0xFFF5F5F5),
            "Define tu meta mensual",
            R.drawable.ic_info,
            Color(0xFF9E9E9E)
        )
    }
    
    CardStyle.AnimatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = statusIcon),
                        contentDescription = null,
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Horas Trabajadas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$totalWorkedHours",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Meta Mensual",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = goalHours,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Helper class for destructuring in when expression
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)