package app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.generated.resources.Res
import app.generated.resources.clicks
import app.generated.resources.count
import app.generated.resources.message
import app.generated.resources.settings
import app.generated.resources.show_message
import kiteconfig.generated.KiteBuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A ViewModel for one route. It survives rotation. It clears when Home leaves the back stack. */
class HomeViewModel : ViewModel() {
    val clicks: StateFlow<Int>
        field = MutableStateFlow(0)

    fun click() {
        clicks.update { it + 1 }
    }
}

/** The sample screen. Replace it with the first real screen of the app. */
@Composable
fun HomeScreen() {
    val home = viewModel { HomeViewModel() }
    val appViewModel = LocalAppViewModel.current
    val backStack = LocalBackStack.current
    val clicks by home.clicks.collectAsState()
    val message = stringResource(Res.string.message)

    ScreenFrame(
        title = KiteBuildConfig.appName,
        actions = {
            TextButton(onClick = { backStack.open(Screen.Settings) }) {
                Text(stringResource(Res.string.settings))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(pluralStringResource(Res.plurals.clicks, clicks, clicks))
            Button(onClick = home::click) { Text(stringResource(Res.string.count)) }
            OutlinedButton(onClick = { appViewModel.snack(message) }) {
                Text(stringResource(Res.string.show_message))
            }
        }
    }
}
