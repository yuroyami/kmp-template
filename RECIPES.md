# Recipes

This file lists no library versions, so use the newest version of each library.

To add a library:

1. Add it to `libs.versions.toml`.
2. Name it in `shared/module.yaml` as `$libs.<name>`. A dash in the name becomes a dot, so `ktor-client-core` is `$libs.ktor.client.core`.

`dependencies` reaches every platform. `dependencies@android` and `dependencies@ios` reach one platform. `test-dependencies` reaches the tests.

## Remove a part

| Part | Steps |
|---|---|
| Settings screen | Delete `SettingsScreen.kt`. Delete the `Settings` route and its `when` line. In `HomeScreen.kt`, delete the action and `backStack`. Remove the route from `BackStackTest`. |
| Theme choice (light only) | Follow [Remove the theme choice](#remove-the-theme-choice). |
| Snackbar | Delete `snackbar`, `message` and `snack` in `AppViewModel`. Delete the `snackbarHost` line in `ScreenFrame`. In `HomeScreen.kt`, delete the button, `appViewModel` and `message`. |
| Icon tool | Run `rm -rf build-plugins/app-icons art`. Delete the two `app-icons` lines in `project.yaml`, and the `app-icons` block in `androidApp/module.yaml`. |
| `jvm` platform | Delete `jvm` from `platforms` and from the `nonAndroid` alias in `shared/module.yaml`. Run `rm -rf shared/src@jvm`. In `scripts/verify.sh`, delete the `./kotlin test -m shared -p jvm` line, and change `-p jvm -p android` to `-p android`. The tests then run only on the iOS simulator. |
| `android-inputs` | Do this after a toolchain release has [KTC-6050](https://youtrack.jetbrains.com/issue/KTC-6050). That toolchain issue makes this plugin unnecessary. Run `rm -rf build-plugins/android-inputs`. Delete the two `android-inputs` lines in `project.yaml`, and the `android-inputs` block in `androidApp/module.yaml`. |
| CI | Run `rm -rf .github`. |

After a removal, delete the strings, the imports, the comments, and the lines of `README.md` and `CLAUDE.md` that have no use now.

### Remove the theme choice

1. In `Theme.kt`, delete `DarkColors`, and reduce the body of `AppTheme` to `MaterialTheme(colorScheme = LightColors, content = content)`.
2. Delete `ThemeMode`, `Prefs.themeMode`, the `expect` and the three `actual` forms of `ApplySystemAppearance`.
3. In `SettingsScreen.kt`, delete `current`, the `items` line, `ThemeRow` and `label`.
4. In `AppActivity`, after `enableEdgeToEdge`, set `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars` to true on `WindowCompat.getInsetsController(window, window.decorView)`.
5. Delete `androidApp/res/values-night`.
6. Delete the dark entry in `LaunchBackground.colorset`.
7. Add `UIUserInterfaceStyle` with the value `Light` to `Info.plist`.
8. Remove `uiMode` from `android:configChanges`.
9. Delete `PrefTest`, or point it at a new `Pref`.

## Add a library with no setup

| Need | Library |
|---|---|
| JSON | `$kotlin.serialization.json`. The toolchain has this name, so `libs.versions.toml` needs no line. |
| Dates and times | `org.jetbrains.kotlinx:kotlinx-datetime` |
| Bytes and files | `org.jetbrains.kotlinx:kotlinx-io-core` |
| File picker | `io.github.vinceglb:filekit-dialogs-compose` |
| Android custom tabs | `androidx.browser:browser`, in `dependencies@android` |
| Compose previews | `$compose.preview`. The toolchain has this name too. |

Warning: do not add `material-icons-extended`. Its version is frozen at 1.7.3, and it adds an 18 MB klib to each iOS platform. Copy the icons that you need into `shared/composeResources/drawable` as vector XML.

## Add networking

1. Add these libraries:

   | Where | Library |
   |---|---|
   | `dependencies` | `io.ktor:ktor-client-core`, `io.ktor:ktor-client-content-negotiation`, `io.ktor:ktor-serialization-kotlinx-json`, `$kotlin.serialization.json` |
   | `dependencies@android` and `dependencies@jvm` | `io.ktor:ktor-client-okhttp` |
   | `dependencies@ios` | `io.ktor:ktor-client-darwin` |
   | `test-dependencies` | `io.ktor:ktor-client-mock` |

2. Add `<uses-permission android:name="android.permission.INTERNET" />` to `androidApp/src/AndroidManifest.xml`. A manifest in `shared` does not reach the app.
3. Make one client outside composition, and set all three timeouts:

   ```kotlin
   val httpClient = HttpClient {
       install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
       install(HttpTimeout) {
           connectTimeoutMillis = 10_000
           socketTimeoutMillis = 30_000
           requestTimeoutMillis = 30_000
       }
   }
   ```

4. For a plain HTTP server on your own machine, add a network security config for debug use. Android blocks plain HTTP by default.

## Add images

1. Add networking first, as [Add networking](#add-networking) describes.
2. Add `io.coil-kt.coil3:coil-compose` and `io.coil-kt.coil3:coil-network-ktor3`.
3. Set one loader at the top of `App()`:

   ```kotlin
   setSingletonImageLoaderFactory { context ->
       ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory(httpClient)) }.build()
   }
   ```

4. Add these lines to `androidApp/proguard-rules.pro`, because Coil loads its decoders by name:

   ```
   -keep class coil3.** { *; }
   -dontwarn coil3.**
   ```

## Add a database

Use Room 3. The toolchain has no SQLDelight compiler.

1. Add `androidx.room3:room3-runtime` and `androidx.sqlite:sqlite-bundled` to `dependencies`.
2. Add `androidx.room3:room3-compiler` to `libs.versions.toml` as `room-compiler`.
3. Add this block under `settings.kotlin` in `shared/module.yaml`. KSP is the tool that runs the Room compiler. KSP runs once for each platform.

   ```yaml
   ksp:
     version: <KSP version>
     processors:
       - $libs.room.compiler
     processorOptions:
       room.schemaLocation: schemas
   ```

4. Declare the database in `shared/src/app`. Room generates the `actual` object on each platform.

   ```kotlin
   @Database(entities = [Note::class], version = 1)
   @ConstructedBy(AppDatabaseConstructor::class)
   abstract class AppDatabase : RoomDatabase() {
       abstract fun notes(): NoteDao
   }

   @Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
   expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
       override fun initialize(): AppDatabase
   }

   internal expect fun databaseBuilder(): RoomDatabase.Builder<AppDatabase>

   val database: AppDatabase by lazy { databaseBuilder().setDriver(BundledSQLiteDriver()).build() }
   ```

5. Write `databaseBuilder` for each platform. Android passes the context. iOS and the JVM do not.

   ```kotlin
   // src@android
   internal actual fun databaseBuilder() = Room.databaseBuilder<AppDatabase>(appContext, "${appDataDir()}/app.db")

   // src@ios and src@jvm
   internal actual fun databaseBuilder() = Room.databaseBuilder<AppDatabase>(name = "${appDataDir()}/app.db")
   ```

6. Use `suspend` functions and `Flow` in each DAO. Blocking DAO functions do not work on iOS.

The first build of a new schema version can fail with a `JsonDecodingException` on `schemas/.../1.json`. KSP runs for each platform at the same time, so one run can read the file while another run writes it. Run the build again, and commit the `schemas` folder.

## Navigate from a ViewModel

The composition owns the back stack, because `rememberSerializable` restores the stack after process death on Android. On iOS, the app always starts at Home after process death. An Activity recreation makes a new stack object, so a ViewModel must not keep the first one.

Warning: a restored route can need a live state that is gone after process death, for example a login session. Check for that state in the screen, and go back to Home when it is gone.

Pick one way:

- **Keep the restore.** Add `var backStack: NavBackStack<Screen>? = null` to `AppViewModel`. Add `SideEffect { appViewModel.backStack = backStack }` to `App()`.
- **Give up the restore.** Make the stack in `AppViewModel` with `NavBackStack<Screen>(Screen.Home)`, and pass it to `NavDisplay`. The app then starts at Home after process death.

Put a back action with side effects in one function. Call that function from the `onBack` of `NavDisplay` and from the `IconButton` in `ScreenFrame.kt`, inside the `lastOrNull() == self` check.

## Add tabs

Use one stack with the home route at the bottom:

```kotlin
fun NavBackStack<Screen>.openTab(tab: Screen) {
    retainAll { it == Screen.Home }
    if (tab != Screen.Home) add(tab)
}
```

Then check these three traps:

- Add the height of the tab bar to the padding of each screen.
- Test the entry to a screen from another tab.
- Test the back action at the root of each tab.

Warning: a swap of the stack on one `NavDisplay` clears the ViewModels of the old routes. To keep them, call `rememberDecoratedNavEntries` once for each tab, and pass the entries of the current tab to `NavDisplay`. Test the swap in your app.

To keep the state of each tab, use one `NavBackStack` for each tab.

## Add design tokens

Add a `Tokens.kt` with an `object` when a second screen repeats a value. Do not add it before that.

## Add a font

1. Put a static TTF file in `shared/composeResources/font`.
2. Make a `FontFamily` in a `@Composable` getter, because `Font(Res.font.name)` is a composable call.
3. Pass a `Typography` with that family to `MaterialTheme` in `Theme.kt`.

## Use a vector launcher icon

1. Add `androidApp/res/drawable/ic_launcher_foreground.xml` and `ic_launcher_background.xml`.
2. Point the three layers in `androidApp/res/mipmap-anydpi-v26/ic_launcher.xml` at the new files.
3. Delete `androidApp/res/mipmap-xxxhdpi`.
4. Keep the iOS file `AppIcon-1024.png`.
5. Remove the icon tool.

## Add an in-app language

Compose resources read the platform default locale, so the app sets that locale.

Warning: do not replace `strings.xml` with a large Kotlin class of strings. The constructor of that class can pass the argument limit of dex, and the app then crashes at launch.

1. Add a folder for each language, for example `shared/composeResources/values-fr/strings.xml`.
2. Add `val language = Pref(stringPreferencesKey("language"), "")` to `Prefs`. An empty value means the system language.
3. Add `expect object PlatformLocale { fun apply(tag: String?) }` to `shared/src/app`, with these `actual` forms:

   ```kotlin
   // src@android. The jvm form keeps the start locale in a val. It sets that locale when tag is null.
   // Otherwise it sets Locale.forLanguageTag(tag).
   actual object PlatformLocale {
       actual fun apply(tag: String?) {
           val system = Resources.getSystem().configuration.locales
           val locales = if (tag == null) system else LocaleList(Locale.forLanguageTag(tag))
           Locale.setDefault(locales[0])
           LocaleList.setDefault(locales)
       }
   }

   // src@ios
   actual object PlatformLocale {
       actual fun apply(tag: String?) {
           val defaults = NSUserDefaults.standardUserDefaults
           if (tag == null) defaults.removeObjectForKey("AppleLanguages")
           else defaults.setObject(listOf(tag), "AppleLanguages")
       }
   }
   ```

4. Add this wrapper, and call it inside `AppTheme` in `App()`:

   ```kotlin
   @Composable
   fun LanguageScope(content: @Composable () -> Unit) {
       val tag by Prefs.language.watch()
       // The locale must change before a string reads it, so this runs here and not in an effect.
       remember(tag) { PlatformLocale.apply(tag.ifEmpty { null }) }
       key(tag) { content() }
   }
   ```

5. For a right-to-left language, provide `LocalLayoutDirection` in the same wrapper.
6. Add `locales: [ en, fr ]` under `kiteconfig` in `app-identity.module-template.yaml`, then run `./kotlin do kiteSyncIos`.
7. Add the `NSUserDefaults` API with the reason `CA92.1` to `iosApp/src/PrivacyInfo.xcprivacy`.

## Add deep links

Warning: a link is untrusted input. Check each value before you use it.

Write each link to `PendingLink` before composition, so that the app does not lose a link at a cold start:

```kotlin
object PendingLink {
    val waiting: StateFlow<String?>
        field = MutableStateFlow(null)

    fun post(link: String) {
        waiting.value = link
    }

    /** Takes the waiting link, so that it runs once. */
    fun take(): String? = waiting.getAndUpdate { null }
}
```

1. On Android, set `android:launchMode="singleTask"` and add the intent filter. Call `post` in `onCreate` and in `onNewIntent`.
2. On iOS, add `CFBundleURLTypes` to `iosApp/src/Info.plist`, or add an Associated Domains entitlement for universal links. Call `post` from `onOpenURL`. Also handle an `NSUserActivity` at a cold start.
3. Read `PendingLink` in a screen with `LaunchedEffect`.

## Add push notifications

You need a Firebase project and an APNs key.

1. Put `google-services.json` next to `androidApp/module.yaml`. The toolchain finds it there.
2. In `libs.versions.toml`, add `com.google.firebase:firebase-bom` with a version, and `com.google.firebase:firebase-messaging` with no version. Add `- bom: $libs.firebase.bom` and `- $libs.firebase.messaging` to `dependencies@android` in `shared/module.yaml`.
3. Add the messaging service and the `POST_NOTIFICATIONS` permission to the Android manifest.
4. Ask for the permission at run time.
5. Add the `aps-environment` entitlement on iOS. Use the `production` value only in the release build.

## Add a desktop app

1. Add `desktopApp` to `project.yaml`, and add `org.jetbrains.kotlinx:kotlinx-coroutines-swing` to `libs.versions.toml`.
2. Add `desktopApp/module.yaml`. `shared` does not export its dependencies, so this module names the libraries of the types that it touches.

   ```yaml
   product: jvm/app

   apply:
     - //common.module-template.yaml
     - //compose.module-template.yaml

   dependencies:
     - //shared
     - $compose.desktop.currentOs
     - $libs.kotlinx.coroutines.swing
     - $libs.lifecycle.viewmodel.navigation3

   settings:
     jvm:
       mainClass: app.desktop.MainKt
   ```

3. Add `desktopApp/src/app/desktop/Main.kt`:

   ```kotlin
   package app.desktop

   import androidx.compose.ui.window.Window
   import androidx.compose.ui.window.application
   import app.App
   import app.startApp

   fun main() {
       startApp()
       application {
           Window(onCloseRequest = ::exitApplication, title = "App name") { App() }
       }
   }
   ```

4. Warning: the JVM form of `appDataDir` returns a new temp folder, so the desktop app loses its preferences on each run. Replace it with a folder for each OS. Read a system property first, so that the tests do not use the real folder.

   ```kotlin
   internal actual fun appDataDir(): String {
       System.getProperty("app.data.dir")?.let { return it }
       val home = System.getProperty("user.home")
       val os = System.getProperty("os.name").lowercase()
       val dir = when {
           "windows" in os -> File(System.getenv("APPDATA") ?: "$home\\AppData\\Roaming", "AppName")
           "mac" in os -> File(home, "Library/Application Support/AppName")
           else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "appname")
       }
       return dir.apply { mkdirs() }.path
   }
   ```

5. Set the property for the tests in `shared/module.yaml`. Each run of the tests then starts from the preferences of the last run, so `PrefTest` must not read a default.

   ```yaml
   test-settings:
     jvm:
       test:
         systemProperties:
           app.data.dir: build/test-data
   ```

6. Set `isDebugBuild` in `Platform.jvm.kt` to a value that is false in a packaged app.
7. Run `./kotlin run -m desktopApp`. Compose hot reload is on by default.

The toolchain has no installer task for a `jvm/app`. An installer needs a local plugin that calls `jpackage`. `jpackage` rejects a first version number of 0, so map a 0.x version to 1.x. Test the installed app, because `./kotlin run` uses a full Java runtime and an installer does not.

## Add a web app

This recipe needs the most work, because a browser has no files and cannot block a thread.

1. Add `wasmJs` to `platforms` in `shared/module.yaml`.
2. Add the alias `nonWeb: [ android, iosArm64, iosSimulatorArm64, jvm ]`.
3. Add `actual` forms of `isDebugBuild` and `ApplySystemAppearance` to `shared/src@wasmJs`.
4. Move the `expect` of `appDataDir` to `shared/src@nonWeb`.
5. Change `store` in `Prefs.kt` to `internal expect val store: DataStore<Preferences>`. Put one `actual` in `shared/src@nonWeb`, with the file store of `Prefs.kt`. Put the other `actual` in `shared/src@wasmJs`.
6. Move any `Dispatchers.IO` or `runBlocking` call that your app added to `shared/src` into `shared/src@nonWeb`.
7. In `shared/src@wasmJs`, write a class that implements `DataStore<Preferences>` on `localStorage`. Keep the values in a `MutableStateFlow`, and rewrite one JSON string on each change. Add `$kotlin.serialization.json` to `dependencies@wasmJs`.
8. Add `webApp` to `project.yaml`, and add `org.jetbrains.kotlinx:kotlinx-browser` to `libs.versions.toml`.
9. Add `webApp/module.yaml`. Do not use `js/app`.

   ```yaml
   product: wasm-js/app

   apply:
     - //common.module-template.yaml
     - //compose.module-template.yaml

   # Keep kotlinx-browser first. Without this order, the link fails on the DOM calls of Compose.
   dependencies:
     - $libs.kotlinx.browser
     - //shared
     - $compose.runtime
     - $compose.ui
     - $libs.kotlinx.coroutines.core
     - $libs.lifecycle.viewmodel.navigation3
   ```

10. In `webApp/src/main.kt`, wait for the preferences, then start the UI. A page cannot block a thread, so the wait must suspend:

    ```kotlin
    @OptIn(ExperimentalComposeUiApi::class)
    fun main() {
        startApp()
        CoroutineScope(Dispatchers.Main).launch {
            prefsLoaded.first { it }
            ComposeViewport(document.body!!) { App() }
        }
    }
    ```

11. Run `./kotlin run -m webApp`.

## Add Android Lint

The toolchain has no lint task. A small JVM module can call lint directly.

1. Add `tools/android-lint` to `project.yaml`. Give the module `product: jvm/app`, and apply `//common.module-template.yaml`. Its dependencies are `com.android.tools.lint:lint` and `$kotlin.reflect`. Lint brings an old `kotlin-reflect`, which fails on the Kotlin 2.4 standard library. Use the lint version that matches the Android Gradle plugin of the toolchain: lint 32.x is AGP 9.x.
2. Make `main` write a lint project file, then call `com.android.tools.lint.Main().run(arrayOf("--project", file, "--sdk-home", sdk, "--exitcode"))`. Put `main` in `src/main.kt`, or set `settings.jvm.mainClass`. Run the module with `./kotlin run -m android-lint`.
3. Write the project file in this shape. List each `.kt` file of `shared/src` and `shared/src@android` as one `<src>` line, because lint skips a folder with no warning. List each file of `androidApp/res` as one `<resource>` line. Lint takes the folder two levels above each entry as the resource root, so a line for `androidApp/res` gives the wrong root.

   ```xml
   <project>
     <sdk dir="ANDROID_HOME"/>
     <root dir="PROJECT_ROOT"/>
     <module name="androidApp" android="true" library="false" compile-sdk-version="37">
       <manifest file="COPY_OF_THE_MANIFEST"/>
       <resource file="androidApp/res/values/colors.xml"/>
       <src file="shared/src/app/App.kt"/>
       <classpath jar="LIBRARY.jar"/>
       <aar file="LIBRARY.aar" extracted="UNPACKED_FOLDER"/>
     </module>
   </project>
   ```

4. Add `minSdk`, `targetSdk` and the version to the copy of the manifest. Lint reads them only from the manifest. This project keeps them in `android.module-template.yaml` and `androidApp/module.yaml`.
5. Build the debug APK.
6. Read the library list from `build/tasks/_androidApp_buildAndroidDebug/gradle-project/settings.gradle.kts`, which lists each library as an absolute path.

## Check Java APIs against minSdk

The toolchain has no core library desugaring, so a Java API above minSdk crashes on old devices with `NoSuchMethodError`. Lint reads only the app code, so check the release APK:

1. Run `dexdump -d` from the Android build tools on each `classes*.dex` of the APK.
2. Collect each `invoke-*` call on a `java/` or `javax/` class.
3. Look up each call in `platforms/android-<level>/data/api-versions.xml` of the Android SDK.
4. Fail when the `since` value is above minSdk. Skip the calls inside `$$ExternalSyntheticApiModelOutline` classes, because they run only where the API exists. Also skip an `invoke-super` call inside an override of the same method. Keep a short list of library classes that check for the API first, each with a reason. Start with `kotlinx.serialization.internal.ClassValueReferences`.
5. Add the script to `scripts/verify.sh`, after the release build.

## Add release guards

Warning: do not add this line to `scripts/verify.sh`. CI runs that script with no `keystore.properties`.

To stop an upload of an unsigned build, add this line to your upload script, before `./kotlin package -m androidApp`:

```bash
[[ -f androidApp/keystore.properties ]] || { echo "androidApp/keystore.properties is missing." >&2; exit 1; }
```

`shared` has no R class, so code reads Android resources by name, and the shrinker can remove them. Name such resources in `androidApp/res/raw/keep.xml`:

```xml
<resources xmlns:tools="http://schemas.android.com/tools" tools:keep="@drawable/my_icon" />
```

## Add crash hooks

Warning: chain to the previous handler on Android, and stop the app on iOS. A hook that swallows a crash leaves a frozen app, and the crash report never arrives.

```kotlin
// Android, in AppApplication.onCreate
val previous = Thread.getDefaultUncaughtExceptionHandler()
Thread.setDefaultUncaughtExceptionHandler { thread, error ->
    Logger.e(error) { "Uncaught exception." }
    previous?.uncaughtException(thread, error)
}

// iOS, in MainViewController
@OptIn(ExperimentalNativeApi::class)
setUnhandledExceptionHook { error ->
    Logger.e(error) { "Uncaught exception." }
    terminateWithUnhandledException(error)
}
```

## Add a second module

1. Add the folder to `project.yaml`.
2. Give the module a `module.yaml` with `product: kmp/lib`, the same `platforms` as `shared`, and the same three `apply` lines for `common`, `compose` and `android`.
3. Set its own `android.namespace`.
4. Set its own `compose.resources.packageName`. Two modules with one package fail.
5. Add `- //<module>: exported` to `dependencies` in `shared/module.yaml`.

## Later patterns

Add these when the app needs them:

- Width classes, for a layout that changes on a tablet.
- A sealed load state (`Loading`, `Ready`, `Failed`) with a retry action.
- A log screen in the app, for reports from testers.
- A check for reduced motion.
- Render tests with `$compose.uiTest`.
