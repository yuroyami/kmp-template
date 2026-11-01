package app

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import platform.UIKit.UIViewController

/** The entry point for Swift: `MainKt.MainViewController()`. */
fun MainViewController(): UIViewController {
    startApp()
    // The first frame needs the stored theme. prefsLoaded turns true after one second at most.
    runBlocking { prefsLoaded.first { it } }
    return ComposeUIViewController { App() }
}
