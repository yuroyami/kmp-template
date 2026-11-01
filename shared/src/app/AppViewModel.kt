package app

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** App-wide state. Put state here that outlives every screen. */
class AppViewModel : ViewModel() {
    /** ScreenFrame shows this host, so a message outlives the screen that sent it. */
    val snackbar = SnackbarHostState()

    private var message: Job? = null

    /** Shows [text]. It replaces the message on screen. */
    fun snack(text: String) {
        message?.cancel()
        message = viewModelScope.launch { snackbar.showSnackbar(text) }
    }
}
