package app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

/** Lives as long as the process. One failed write must not cancel the others. */
private val prefsScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

private val store: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(
        // A corrupt file fails every later read and write. Starting over from the defaults is better.
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { "${appDataDir()}/app.preferences_pb".toPath() },
    )
}

/** True after the first read, or after one second if the disk does not answer. */
val prefsLoaded: StateFlow<Boolean>
    field = MutableStateFlow(false)

/** Every stored preference. A [Pref] reads only this, so a read never waits on the disk. */
private val snapshot: StateFlow<Preferences> by lazy {
    val state = MutableStateFlow(emptyPreferences())
    prefsScope.launch {
        store.data
            // One failed read must not end the only live view of the file.
            .retryWhen { failure, _ ->
                Logger.w(failure) { "Preferences read failed. Retrying." }
                delay(1.seconds)
                true
            }
            .collect {
                state.value = it
                prefsLoaded.value = true
            }
    }
    prefsScope.launch {
        delay(1.seconds)
        prefsLoaded.value = true
    }
    state
}

/** Starts the first read. It does not wait. [startApp] calls it. */
fun warmUpPrefs() {
    snapshot
}

/** One preference with its default. Declare each one in [Prefs]. */
class Pref<T>(private val key: Preferences.Key<T>, private val default: T) {
    /** The current value, or the default until [prefsLoaded] is true. It never blocks, so any thread can call it. */
    fun value(): T = snapshot.value[key] ?: default

    fun flow(): Flow<T> = snapshot.map { it[key] ?: default }.distinctUntilChanged()

    /** The value as Compose state. */
    @Composable
    fun watch(): State<T> = remember(this) { flow() }.collectAsState(value())

    /** Saves the value on the process scope, so the write outlives the screen that called it. */
    fun set(value: T): Job = prefsScope.launch {
        runCatchingNonCancel { store.edit { it[key] = value } }
            .onFailure { Logger.e(it) { "${key.name} was not saved." } }
    }
}

/** Every preference of the app. */
object Prefs {
    val themeMode = Pref(stringPreferencesKey("theme_mode"), ThemeMode.System.name)
}
