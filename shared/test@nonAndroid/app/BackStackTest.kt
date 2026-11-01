package app

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.test.Test
import kotlin.test.assertEquals

class BackStackTest {
    @Test
    fun everyRouteSurvivesSavedState() {
        val serializer = NavBackStackSerializer(Screen.serializer())
        val stack = NavBackStack<Screen>(Screen.Home, Screen.Settings)
        val restored = decodeFromSavedState(serializer, encodeToSavedState(serializer, stack))
        assertEquals(stack.toList(), restored.toList())
    }
}
