package app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class PrefTest {
    @Test
    fun savedValueReachesValueAndFlow() = runTest {
        startApp()
        prefsLoaded.first { it }
        val wanted = if (Prefs.themeMode.value() == ThemeMode.Dark.name) ThemeMode.Light.name else ThemeMode.Dark.name
        Prefs.themeMode.set(wanted).join()
        Prefs.themeMode.flow().first { it == wanted }
        assertEquals(wanted, Prefs.themeMode.value())
    }
}
