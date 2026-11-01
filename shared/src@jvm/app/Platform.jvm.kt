package app

import androidx.compose.runtime.Composable
import kotlin.io.path.createTempDirectory

// The jvm platform runs only tests. Each run starts empty and never touches the user profile.
internal actual fun appDataDir(): String = createTempDirectory("app-test").toString()

internal actual val isDebugBuild: Boolean = true

@Composable
internal actual fun ApplySystemAppearance(dark: Boolean, followSystem: Boolean) = Unit
