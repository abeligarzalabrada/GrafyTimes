package com.grilo.grafytimes.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import com.grilo.grafytimes.statistics.data.ActivitySummary
import com.grilo.grafytimes.statistics.data.CalendarEntry
import com.grilo.grafytimes.statistics.data.MonthlyStatistics
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Enhanced animation imports
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = viewModel()
) {
    // Animation for the whole screen
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(500)) + 
                expandVertically(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var selectedTab by remember { mutableStateOf(StatisticsTab.MONTHLY) }
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Título y selector de mes
                MonthSelector(
                    currentYearMonth = viewModel.currentYearMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
                
                // Pestañas de navegación con animación suave
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    StatisticsTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { 
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            icon = { 
                                Icon(
                                    imageVector = tab.icon, 
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                ) 
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                // Animated content based on selected tab
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut())
                            .using(SizeTransform(clip = false))
                    },
                    label = "Tab Content"
                ) { targetTab ->
                    when (targetTab) {
                        StatisticsTab.MONTHLY -> MonthlyStatisticsContent(viewModel)
                        StatisticsTab.HISTORICAL -> HistoricalComparisonContent(viewModel)
                        StatisticsTab.CALENDAR -> CalendarViewContent(viewModel)
                        StatisticsTab.REPORT -> MonthlyReportContent(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthSelector(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
    val formattedMonth = currentYearMonth.format(formatter)
    
    // Add a transition animation for month changes
    val transition = updateTransition(targetState = formattedMonth, label = "Month Transition")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .clip(CircleShape)
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.ArrowBack, 
                contentDescription = "Mes anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Animated month text
        Box(contentAlignment = Alignment.Center) {
            val slideDistance = with(LocalDensity.current) { 50.dp.toPx().roundToInt() }
            
            transition.AnimatedContent(
                transitionSpec = {
                    (slideInHorizontally { distance -> distance } + fadeIn() with
                            slideOutHorizontally { distance -> -distance } + fadeOut())
                        .using(SizeTransform(clip = false))
                }
            ) { targetMonth ->
                Text(
                    text = targetMonth.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .clip(CircleShape)
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.ArrowForward, 
                contentDescription = "Mes siguiente",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthlyStatisticsContent(viewModel: StatisticsViewModel) {
    val statistics = viewModel.currentMonthStatistics
    
    // Animation for loading content
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(statistics) {
        contentVisible = false
        delay(100)
        contentVisible = true
    }
    
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Tarjeta de resumen con estilo minimalista
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Resumen del Mes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (statistics != null) {
                            // Horas totales y meta con animación
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Total de horas",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Animated counter for hours
                                    val hours = animateFloatAsState(
                                        targetValue = statistics.totalHours,
                                        animationSpec = tween(800)
                                    )
                                    Text(
                                        text = String.format("%.1f", hours.value),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Meta mensual",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${statistics.goalHours}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Barra de progreso animada
                            val progressAnimation = animateFloatAsState(
                                targetValue = statistics.goalPercentage / 100,
                                animationSpec = tween(1000, easing = EaseOutQuart)
                            )
                            
                            Text(
                                text = "Progreso",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            LinearProgressIndicator(
                                progress = { progressAnimation.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (statistics.goalPercentage >= 100) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Text(
                                text = "${String.format("%.1f", statistics.goalPercentage)}% completado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Additional stats with horizontal layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Días trabajados
                                StatInfoItem(
                                    label = "Días trabajados",
                                    value = "${statistics.daysWorked}",
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Estudios bíblicos
                                StatInfoItem(
                                    label = "Estudios visitados",
                                    value = "${statistics.bibleStudiesCount}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay datos disponibles para este mes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Tarjeta de actividades con estilo minimalista
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Actividades Realizadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (statistics != null && statistics.activitiesSummary.isNotEmpty()) {
                            // Animate items appearing one by one
                            statistics.activitiesSummary.entries.forEachIndexed { index, (_, summary) ->
                                key(summary.activityName) {
                                    var visible by remember { mutableStateOf(false) }
                                    
                                    LaunchedEffect(Unit) {
                                        delay(100L * index)
                                        visible = true
                                    }
                                    
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn() + expandVertically()
                                    ) {
                                        Column {
                                            ActivitySummaryItem(summary)
                                            if (index < statistics.activitiesSummary.size - 1) {
                                                Divider(
                                                    modifier = Modifier.padding(vertical = 12.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay actividades registradas para este mes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActivitySummaryItem(summary: ActivitySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = summary.activityName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${summary.sessionCount} salidas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Animated hour counter
        val animatedHours = animateFloatAsState(
            targetValue = summary.totalHours,
            animationSpec = tween(800, easing = EaseOutQuart)
        )
        
        Text(
            text = "${String.format("%.1f", animatedHours.value)} horas",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun HistoricalComparisonContent(viewModel: StatisticsViewModel) {
    val historicalStats = viewModel.historicalStatistics
    
    // Animation for loading content
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(historicalStats) {
        contentVisible = false
        delay(100)
        contentVisible = true
    }
    
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Comparativa de los últimos meses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            if (historicalStats.isNotEmpty()) {
                // Gráfico de barras animado para horas totales
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Horas por Mes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Encontrar el valor máximo para escalar el gráfico
                            val maxHours = historicalStats.maxOfOrNull { it.totalHours } ?: 0f
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                historicalStats.forEachIndexed { index, stats ->
                                    val yearMonth = YearMonth.parse(stats.yearMonth)
                                    val monthName = yearMonth.month.getDisplayName(
                                        TextStyle.SHORT,
                                        Locale("es", "ES")
                                    ).take(3).uppercase()
                                    
                                    // Animation for each bar
                                    val barHeightPct = animateFloatAsState(
                                        targetValue = if (maxHours > 0) stats.totalHours / maxHours else 0f,
                                        animationSpec = tween(
                                            durationMillis = 1000,
                                            delayMillis = 100 * index,
                                            easing = EaseOutBounce
                                        )
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Value on top of the bar
                                        Text(
                                            text = "${stats.totalHours}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Animated bar
                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height((barHeightPct.value * 170).dp)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(
                                                    color = if (stats.goalPercentage >= 100) {
                                                        MaterialTheme.colorScheme.primary // Verde si cumplió la meta
                                                    } else {
                                                        MaterialTheme.colorScheme.tertiary // Color terciario si no cumplió
                                                    }
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Month name below the bar
                                        Text(
                                            text = monthName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Tabla comparativa mejorada
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Comparativa Detallada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Encabezados de la tabla con estilo mejorado
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Mes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "Horas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Meta",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Estudios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Filas de datos con animación estilo cascada
                            historicalStats.forEachIndexed { index, stats ->
                                // Animation for each row
                                var rowVisible by remember { mutableStateOf(false) }
                                
                                LaunchedEffect(Unit) {
                                    delay(100L * index)
                                    rowVisible = true
                                }
                                
                                AnimatedVisibility(
                                    visible = rowVisible,
                                    enter = fadeIn() + expandVertically()
                                ) {
                                    val yearMonth = YearMonth.parse(stats.yearMonth)
                                    val monthName = yearMonth.month.getDisplayName(
                                        TextStyle.SHORT,
                                        Locale("es", "ES")
                                    ).replaceFirstChar { it.uppercase() }
                                    
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$monthName ${yearMonth.year}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1.2f)
                                            )
                                            Text(
                                                text = "${stats.totalHours}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (stats.goalPercentage >= 100) 
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "${stats.goalHours}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "${stats.bibleStudiesCount}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        
                                        if (index < historicalStats.size - 1) {
                                            Divider(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    EmptyStateMessage(message = "No hay datos históricos disponibles")
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CalendarViewContent(viewModel: StatisticsViewModel) {
    val currentYearMonth = viewModel.currentYearMonth
    val calendarEntries = viewModel.calendarEntries
    val selectedDate = viewModel.selectedDate
    val selectedDateEntries = viewModel.selectedDateEntries
    val bibleStudiesMap = viewModel.bibleStudiesMap
    
    // Animation for the content
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentYearMonth) {
        contentVisible = false
        delay(100)
        contentVisible = true
    }
    
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Calendario mensual con animación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    MonthCalendarGrid(
                        yearMonth = currentYearMonth,
                        calendarEntries = calendarEntries,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> viewModel.selectDate(date) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detalles del día seleccionado con animación
            AnimatedVisibility(
                visible = selectedDate != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (selectedDate != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Fecha seleccionada con formato mejorado
                            val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
                            val formattedDate = selectedDate.format(formatter).replaceFirstChar { it.uppercase() }
                            
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Registros del día con animación
                            if (selectedDateEntries.isNotEmpty()) {
                                Text(
                                    text = "Actividades registradas:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // Animate entries appearing one by one
                                selectedDateEntries.forEachIndexed { index, entry ->
                                    key(entry) {
                                        var visible by remember { mutableStateOf(false) }
                                        
                                        LaunchedEffect(selectedDate) {
                                            delay(100L * index)
                                            visible = true
                                        }
                                        
                                        AnimatedVisibility(
                                            visible = visible,
                                            enter = fadeIn() + expandVertically(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${entry.hours} horas",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    
                                                    if (entry.activityType.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = entry.activityType,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    
                                                    if (entry.bibleStudyId.isNotBlank() && bibleStudiesMap.containsKey(entry.bibleStudyId)) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Estudio: ${bibleStudiesMap[entry.bibleStudyId]?.name ?: ""}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No hay registros para este día",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Message when no date is selected
            AnimatedVisibility(
                visible = selectedDate == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecciona un día para ver detalles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    calendarEntries: List<CalendarEntry>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    
    // Determinar el día de la semana del primer día del mes (0 = lunes, 6 = domingo)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    
    // Agrupar entradas por fecha
    val entriesByDate = calendarEntries.groupBy { entry ->
        try {
            LocalDate.parse(entry.date).dayOfMonth
        } catch (e: Exception) {
            -1 // Valor inválido para fechas que no se pueden parsear
        }
    }
    
    // Nombres de los días de la semana
    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Encabezado con nombres de días - estilo mejorado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Animación para todo el calendario
        var calendarVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(yearMonth) {
            calendarVisible = false
            delay(100)
            calendarVisible = true
        }
        
        AnimatedVisibility(
            visible = calendarVisible,
            enter = fadeIn() + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            // Cuadrícula del calendario
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Espacios vacíos para el inicio del mes
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.padding(4.dp))
                }
                
                // Días del mes
                items(lastDayOfMonth.dayOfMonth) { day ->
                    val date = yearMonth.atDay(day + 1)
                    val isSelected = selectedDate?.isEqual(date) == true
                    val hasEntries = entriesByDate.containsKey(day + 1)
                    val totalHours = entriesByDate[day + 1]?.sumOf { it.hours.toDouble() }?.toFloat() ?: 0f
                    
                    // Delay animation for each day
                    var visible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(yearMonth) {
                        delay(15L * (day + firstDayOfWeek))
                        visible = true
                    }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        CalendarDay(
                            day = day + 1,
                            isSelected = isSelected,
                            hasEntries = hasEntries,
                            totalHours = totalHours,
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    hasEntries: Boolean,
    totalHours: Float,
    onClick: () -> Unit
) {
    // Animation for selection
    val animatedScale = animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Background color animation
    val backgroundColor = animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            hasEntries -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else -> Color.Transparent
        },
        animationSpec = tween(300)
    )
    
    // Border color animation
    val borderColor = animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            hasEntries -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor.value)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = borderColor.value,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .scale(animatedScale.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasEntries) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    hasEntries -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (hasEntries) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${totalHours}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@Composable
fun MonthlyReportContent(viewModel: StatisticsViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Animation for content
    var contentVisible by remember { mutableStateOf(false) }
    
    // Generar informe si no existe
    LaunchedEffect(viewModel.currentYearMonth) {
        if (viewModel.monthlyReport == null) {
            viewModel.generateMonthlyReport()
        }
        delay(100)
        contentVisible = true
    }
    
    val report = viewModel.monthlyReport
    
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Informe Mensual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (report != null) {
                            // Resumen con animaciones
                            Text(
                                text = "Resumen",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                            )
                            
                            // Animated stats grid
                            var statsVisible by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(report) {
                                delay(300)
                                statsVisible = true
                            }
                            
                            AnimatedVisibility(
                                visible = statsVisible,
                                enter = fadeIn() + expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    // Grid layout for stats
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Total horas
                                        ReportStatItem(
                                            label = "Total de horas",
                                            value = "${report.totalHours}",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        // Meta mensual
                                        ReportStatItem(
                                            label = "Meta mensual",
                                            value = "${report.goalHours}",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Porcentaje completado
                                        ReportStatItem(
                                            label = "Completado",
                                            value = "${String.format("%.1f", report.goalPercentage)}%",
                                            color = if (report.goalPercentage >= 100) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        // Estudios bíblicos
                                        ReportStatItem(
                                            label = "Estudios",
                                            value = "${report.bibleStudiesCount}",
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            
                            // Actividades con animación
                            Text(
                                text = "Actividades",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            )
                            
                            // Animate activity items appearing one by one
                            report.activitiesSummary.entries.forEachIndexed { index, (_, summary) ->
                                key(summary.activityName) {
                                    var visible by remember { mutableStateOf(false) }
                                    
                                    LaunchedEffect(Unit) {
                                        delay(100L * index + 500L)
                                        visible = true
                                    }
                                    
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn() + expandVertically()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${summary.activityName}:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            Text(
                                                text = "${summary.totalHours} horas en ${summary.sessionCount} salidas",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Botones de acción con animación
                            var actionsVisible by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(report) {
                                delay(800)
                                actionsVisible = true
                            }
                            
                            AnimatedVisibility(
                                visible = actionsVisible,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Copy button with ripple effect
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(viewModel.reportText))
                                            // Toast.makeText(context, "Informe copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Share, 
                                            contentDescription = "Compartir",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Copiar", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    
                                    // Save button
                                    Button(
                                        onClick = {
                                            // viewModel.exportReportToFile(context)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Download, 
                                            contentDescription = "Descargar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Guardar", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        } else {
                            // Loading indicator
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Generando informe...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // Pulsating animation for progress indicator
                                val infiniteTransition = rememberInfiniteTransition(label = "progress")
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = EaseInOutQuad),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .scale(scale)
                                        .padding(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Animated value
        val animatedValue = remember(value) {
            Animatable(0f)
        }
        
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = EaseOutQuart)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = animatedValue.value
                scaleX = animatedValue.value
                scaleY = animatedValue.value
            }
        )
    }
}
