package app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.generated.resources.Res
import app.generated.resources.back
import app.generated.resources.ic_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The page of every screen. It handles system bars, cutouts and the keyboard here, once.
 * [content] gets the padding. Pass it to a LazyColumn as contentPadding. [topBar] replaces the stock bar.
 */
@Composable
fun ScreenFrame(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    topBar: @Composable () -> Unit = { ScreenTopBar(title, actions) },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = { SnackbarHost(LocalAppViewModel.current.snackbar) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(Modifier.consumeWindowInsets(padding)) { content(padding) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTopBar(title: String, actions: @Composable RowScope.() -> Unit) {
    val backStack = LocalBackStack.current
    val self = LocalScreen.current
    // Read once. A live read changes the arrow on the outgoing screen during the transition.
    // Ask about this route, not the stack size: a back gesture composes the screen below before the pop.
    val canGoBack = remember { backStack.lastIndexOf(self) > 0 }
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canGoBack) {
                // A second tap during the transition reaches the outgoing screen, so only the top route pops.
                IconButton(onClick = { if (backStack.size > 1 && backStack.lastOrNull() == self) backStack.removeLastOrNull() }) {
                    Icon(painterResource(Res.drawable.ic_back), stringResource(Res.string.back))
                }
            }
        },
        actions = actions,
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    )
}
