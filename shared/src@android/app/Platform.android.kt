package app

import android.content.pm.ApplicationInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

internal actual fun appDataDir(): String = appContext.filesDir.path

internal actual val isDebugBuild: Boolean
    get() = appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

@Composable
internal actual fun ApplySystemAppearance(dark: Boolean, followSystem: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
