package app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

enum class ThemeMode { System, Light, Dark }

// Keep each background equal to androidApp/res/values*/colors.xml and the LaunchBackground color.
private val LightColors = lightColorScheme(primary = Color(0xFF3F5CA8), background = Color.White, surface = Color.White)
private val DarkColors = darkColorScheme(primary = Color(0xFFB3C5FF), background = Color(0xFF121212), surface = Color(0xFF121212))

/** Reads the stored [ThemeMode]. Change the two color schemes to the colors of the app. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val stored by Prefs.themeMode.watch()
    val mode = ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.System
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    ApplySystemAppearance(dark, followSystem = mode == ThemeMode.System)
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
