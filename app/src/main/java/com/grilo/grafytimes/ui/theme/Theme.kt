package com.grilo.grafytimes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.grilo.grafytimes.settings.data.SettingsDataStore
import com.grilo.grafytimes.settings.data.ThemeMode

// Función para convertir un código de color hexadecimal a Color
fun hexToColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        PrimaryColor // Color por defecto si hay error
    }
}

@Composable
fun GrafyTimesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivado por defecto para usar colores personalizados
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Obtener configuraciones de tema desde DataStore
    val settingsDataStore = SettingsDataStore(context)
    val settings by settingsDataStore.settingsFlow.collectAsState(initial = null)
    
    // Determinar si usar tema oscuro basado en las preferencias del usuario
    val useDarkTheme = when (settings?.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> darkTheme
        else -> darkTheme
    }
    
    // Obtener color primario personalizado
    val primaryColorHex = settings?.primaryColorHex ?: "#3F51B5" // Indigo por defecto
    val primaryColor = hexToColor(primaryColorHex)
    
    // Crear esquemas de colores personalizados
    val darkColorScheme = darkColorScheme(
        primary = primaryColor,
        secondary = SecondaryColor,
        tertiary = TertiaryColor,
        background = SurfaceDark,
        surface = SurfaceDark,
        error = ErrorColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    val lightColorScheme = lightColorScheme(
        primary = primaryColor,
        secondary = SecondaryColor,
        tertiary = TertiaryColor,
        background = Background,
        surface = SurfaceLight,
        error = ErrorColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black
    )
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> darkColorScheme
        else -> lightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}