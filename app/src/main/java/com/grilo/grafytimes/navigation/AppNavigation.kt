package com.grilo.grafytimes.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grilo.grafytimes.biblestudy.BibleStudyScreen
import com.grilo.grafytimes.notes.NotesScreen
import com.grilo.grafytimes.productivity.ProductivityScreen
import com.grilo.grafytimes.settings.SettingsScreen
import com.grilo.grafytimes.statistics.StatisticsScreen
import com.grilo.grafytimes.ui.theme.CombinedAnimations

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { 
                CombinedAnimations.fadeInWithSlideUp
            },
            exitTransition = {
                CombinedAnimations.fadeOutWithScale
            },
            popEnterTransition = {
                CombinedAnimations.fadeInWithScale
            },
            popExitTransition = {
                CombinedAnimations.fadeOutWithSlideDown
            }
        ) {
            composable(Screen.Home.route) {
                ProductivityScreen()
            }
            composable(Screen.BibleStudy.route) {
                BibleStudyScreen()
            }
            composable(Screen.Notes.route) {
                NotesScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.BibleStudy,
        Screen.Notes,
        Screen.Statistics,
        Screen.Settings
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Definir las rutas de navegación
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object BibleStudy : Screen("bible_study", "Estudios", Icons.Default.Book)
    object Notes : Screen("notes", "Notas", Icons.Default.Note)
    object Statistics : Screen("statistics", "Estadísticas", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Configuración", Icons.Default.Settings)
}