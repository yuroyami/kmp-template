package app

import androidx.compose.runtime.Composable

/** The private data folder of the app. The preferences file lives here. */
internal expect fun appDataDir(): String

/** True in a debug build. */
internal expect val isDebugBuild: Boolean

/** Tells the platform about the theme, so that the system bar icons stay readable. */
@Composable
internal expect fun ApplySystemAppearance(dark: Boolean, followSystem: Boolean)
