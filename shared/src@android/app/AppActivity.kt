package app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // This call must run before super.onCreate. The splash hold stops after one second at most.
        installSplashScreen().setKeepOnScreenCondition { !prefsLoaded.value }
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContent {
            // Wait like iOS does, so that the first read of a Pref gets the stored value.
            val loaded by prefsLoaded.collectAsState()
            if (loaded) App()
        }
    }
}
