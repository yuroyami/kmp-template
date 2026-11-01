package app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Every route of the app. The saved back stack stores these values, so each one must be @Serializable. */
@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Settings : Screen
}

/** Draws a route. The `when` has no else, so a new route with no screen fails at compile time. */
@Composable
fun Screen.UI() = when (this) {
    Screen.Home -> HomeScreen()
    Screen.Settings -> SettingsScreen()
}

/** Opens [screen]. A double tap must not add the same route twice. */
fun NavBackStack<Screen>.open(screen: Screen) {
    if (lastOrNull() != screen) add(screen)
}
