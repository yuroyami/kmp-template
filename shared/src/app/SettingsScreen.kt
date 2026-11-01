package app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.generated.resources.Res
import app.generated.resources.settings
import app.generated.resources.theme_dark
import app.generated.resources.theme_light
import app.generated.resources.theme_system
import app.generated.resources.version
import kiteconfig.generated.KiteBuildConfig
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The theme choice and the app version. */
@Composable
fun SettingsScreen() {
    val current by Prefs.themeMode.watch()

    ScreenFrame(stringResource(Res.string.settings)) { padding ->
        LazyColumn(contentPadding = padding) {
            items(ThemeMode.entries) { mode -> ThemeRow(mode, selected = mode.name == current) }
            item {
                Text(stringResource(Res.string.version, KiteBuildConfig.versionName), Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ThemeRow(mode: ThemeMode, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton) { Prefs.themeMode.set(mode.name) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The whole row takes the tap, so the button has no click action of its own.
        RadioButton(selected = selected, onClick = null)
        Text(stringResource(mode.label))
    }
}

private val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.System -> Res.string.theme_system
        ThemeMode.Light -> Res.string.theme_light
        ThemeMode.Dark -> Res.string.theme_dark
    }
