package app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kiteconfig.generated.KiteBuildConfig

/** The back stack of the app. Call [open] on it to go to a screen. */
val LocalBackStack = staticCompositionLocalOf<NavBackStack<Screen>> { error("App() provides the back stack.") }

/** The route of the screen that reads this value. */
val LocalScreen = staticCompositionLocalOf<Screen> { error("App() provides the route.") }

/** The app-wide ViewModel. */
val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> { error("App() provides the app ViewModel.") }

/** Process start. Each entry point calls it once, before any UI. Add other one-time setup here. */
fun startApp() {
    Logger.setTag(KiteBuildConfig.appName)
    // Release logs reach the device log, so they keep warnings and errors only.
    Logger.setMinSeverity(if (isDebugBuild) Severity.Verbose else Severity.Warn)
    warmUpPrefs()
}

/**
 * The root of the UI. Each entry point calls it after [startApp].
 *
 * [onViewModel] gives the entry point the [AppViewModel] of the UI, for example to forward an intent.
 * Do not make a second [AppViewModel]. Do not keep this one in a singleton, because a singleton
 * outlives the cleared ViewModel.
 */
@Composable
fun App(onViewModel: (AppViewModel) -> Unit = {}) {
    // viewModel {} keeps one instance across a configuration change, and calls onCleared when the store owner ends.
    val appViewModel = viewModel { AppViewModel() }
    LaunchedEffect(appViewModel) { onViewModel(appViewModel) }

    // On Android, saved state returns the app to the same screen after process death. iOS keeps this state in memory only.
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(Screen.serializer())) {
        NavBackStack<Screen>(Screen.Home)
    }

    CompositionLocalProvider(LocalAppViewModel provides appViewModel, LocalBackStack provides backStack) {
        AppTheme {
            // This Surface paints the root background, so the window background never shows between two screens.
            Surface(Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    // Keep this order. The saveable decorator must come before the ViewModel decorator.
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = { screen ->
                        NavEntry(screen) { CompositionLocalProvider(LocalScreen provides it) { it.UI() } }
                    },
                )
            }
        }
    }
}
