package app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.uikit.LocalUIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIUserInterfaceStyle

@OptIn(ExperimentalForeignApi::class)
internal actual fun appDataDir(): String = requireNotNull(
    NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path,
) { "The Application Support folder is not available." }

@OptIn(ExperimentalNativeApi::class)
internal actual val isDebugBuild: Boolean = Platform.isDebugBinary

@Composable
internal actual fun ApplySystemAppearance(dark: Boolean, followSystem: Boolean) {
    val controller = LocalUIViewController.current
    SideEffect {
        controller.view.window?.overrideUserInterfaceStyle = when {
            followSystem -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            dark -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        controller.setNeedsStatusBarAppearanceUpdate()
    }
}
