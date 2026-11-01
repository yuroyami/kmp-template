# KmpTemplate

<!-- template:start -->
A starter for a Kotlin Multiplatform app on Android and iOS with one Compose UI. It builds with the [Kotlin Toolchain](https://github.com/JetBrains/kotlin-toolchain), the `./kotlin` command in the repository root. It has no Gradle files.

The template holds the parts that almost every app needs:

- Two screens on a back stack. On Android, the back stack survives process death. On iOS, the app starts at Home after process death.
- `ScreenFrame`, a page that handles the system bars, the cutouts and the keyboard in one place.
- Stored preferences, and a System, Light or Dark theme.
- An app-wide ViewModel, a per-screen ViewModel, and a snackbar that stays when the screen changes.
- One file for the app name, the version and the IDs. A check fails when a version or an ID differs from that file.
- Text in `strings.xml`, a splash screen, an icon tool, R8 rules, two tests and CI.

The template leaves out networking, images, a database, tabs, a desktop app and a web app. [RECIPES.md](RECIPES.md) has the steps to add them, and the steps to remove the parts that your app does not need.

## Start a new app

1. Create a repository from this template and enter its folder:

   ```bash
   gh repo create my-app --private --template yuroyami/kmp-template --clone
   cd my-app
   ```

2. Warning: the next command deletes the template part of this file, and steps 3 to 7 with it. Keep these steps open on the template page.

   Run the rename script with your app name and your application ID. A third argument sets the iOS bundle ID. Without it, the bundle ID is the application ID plus `.ios`.

   ```bash
   scripts/new-app.sh "My App" com.example.myapp
   ```

   The script also deletes `.github/workflows/template.yml` and itself. It does not change the Kotlin package, which stays `app`.

3. Run `scripts/verify.sh`.
4. Replace the app icon, as [App icon](#app-icon) describes.
5. Set the colors in `shared/src/app/Theme.kt`. Keep the background colors equal in `androidApp/res/values*/colors.xml` and `iosApp/src/Assets.xcassets/LaunchBackground.colorset`.
6. Replace `HomeScreen.kt` with your first screen.
7. Rewrite the sentence that starts with your app name in this file and in `CLAUDE.md`.
<!-- template:end -->

KmpTemplate is an app for Android and iOS. Replace this line with what the app does.

## What you need

- The Android SDK. The toolchain installs it if it is missing, and it asks you to accept the licenses. Set `ANDROID_HOME` to use an existing SDK. In a shell with no prompt, run `sdkmanager --licenses` first.
- Xcode, for iOS. Open Xcode once, or run `xcodebuild -runFirstLaunch`.

You do not need a JDK, Gradle or CocoaPods. The toolchain downloads its own JDK and Kotlin compilers.

## Build and run

Warning: always pass `-d` to `./kotlin run` on Android. Without `-d`, the toolchain installs a system image into the Android SDK, even when a device is connected. The image takes about 8 GB.

```bash
adb devices                                    # the device serials for -d
./kotlin run -m androidApp -d emulator-5554    # Android
./kotlin run -m iosApp                         # iOS simulator
./kotlin build -p jvm -p android               # compile, and make the debug APK
./kotlin test -m shared -p jvm                 # the tests, on the JVM
./kotlin test -m shared -p iosSimulatorArm64   # the same tests, on iOS
```

The `jvm` platform of `shared` exists only for fast tests. There is no desktop app.

To run on an iPhone, open `iosApp/module.xcodeproj` in Xcode and set your team on the app target.

## Add a screen

1. Add a route to `Screen` in `shared/src/app/Screen.kt`. A route is the value that the back stack stores, so it must be `@Serializable`.

   ```kotlin
   @Serializable
   data object Profile : Screen

   // A route can hold arguments. Do not put a secret here: routes go to saved state.
   @Serializable
   data class Detail(val id: Int) : Screen
   ```

2. Add one line for each route to `Screen.UI()` in the same file. The `when` has no `else`, so the build fails until each route has a line.

   ```kotlin
   Screen.Profile -> ProfileScreen()
   is Screen.Detail -> DetailScreen(id)
   ```

3. Write the screen in a new file, and add its text to `shared/composeResources/values/strings.xml`. `ScreenFrame` adds the top bar, the back arrow and the padding.

   ```kotlin
   @Composable
   fun ProfileScreen() {
       ScreenFrame(stringResource(Res.string.profile)) { padding ->
           Text(stringResource(Res.string.welcome), Modifier.padding(padding))
       }
   }
   ```

4. To open the route from another screen, read `val backStack = LocalBackStack.current` in that screen. Then call `backStack.open(Screen.Profile)` in the click action.
5. Add the route to the list in `shared/test@nonAndroid/app/BackStackTest.kt`.

## Add a preference

1. Add one line to `object Prefs` in `shared/src/app/Prefs.kt`:

   ```kotlin
   val sortNewest = Pref(booleanPreferencesKey("sort_newest"), true)
   ```

2. Read and write the value:

   ```kotlin
   val newest by Prefs.sortNewest.watch()  // Compose state
   Prefs.sortNewest.value()                // the current value on any thread, or the default until prefsLoaded is true
   Prefs.sortNewest.set(false)             // saves the value
   ```

## Check before a push

Run `scripts/verify.sh`. It checks the app identity, builds the debug and the release APK, and runs the tests. On macOS, it also runs the tests on iOS and builds the iOS app. CI runs the same script.

## App identity

[`app-identity.module-template.yaml`](app-identity.module-template.yaml) holds the app name, the version, the application ID and the iOS bundle ID. [KiteConfig](https://github.com/yuroyami/KiteConfig) is a toolchain plugin that reads this file. It generates `kiteconfig.generated.KiteBuildConfig` for the shared code. It also writes the iOS values and checks the Android values.

To release a new version:

1. Change `version` in `app-identity.module-template.yaml`.
2. Change `versionName` and `versionCode` in `androidApp/module.yaml`. The toolchain does not let a plugin set them. The build number comes from the version: 0.1.0 gives 1000001000.
3. Run `./kotlin do kiteSyncIos`. It writes the version, the build number and the bundle ID into the Xcode project.
4. Run `./kotlin check kiteAndroid kiteIos`. It fails when the Android or the Xcode values differ from the identity file, and it prints the values to write.

KiteConfig does not check the app name on each platform. To change the name later, change `appName` in the identity file, `android:label` in `androidApp/src/AndroidManifest.xml` and `PRODUCT_NAME` in the Xcode project by hand.

## App icon

1. Replace `art/logo_foreground.png` with a 1024 x 1024 logo on a transparent background.
2. Replace `art/logo_background.png` with a 1024 x 1024 background.
3. Run `./kotlin do syncAppIcons`. It writes the Android launcher images and the iOS icon. A build does not run this task.
4. Commit the new images.

## Release signing (Android)

Warning: keep the keystore and `keystore.properties` out of git. `.gitignore` covers both. Back up both files. Use one key for each app.

1. Write `androidApp/keystore.properties` with your own values:

   ```properties
   storeFile=release.keystore
   storePassword=<password>
   keyAlias=<alias>
   keyPassword=<password>
   ```

2. In the `androidApp` folder, run `../kotlin tool generate-keystore --properties-file keystore.properties`.
3. Run `./kotlin package -m androidApp`. The AAB goes to `build/tasks/_androidApp_bundleAndroid/gradle-project-release.aab`.

Without `keystore.properties`, the release build is not signed.

## Project layout

| Folder | Content |
|---|---|
| `shared/` | The app code and the Compose UI. `src` is common code. `src@android`, `src@ios` and `src@jvm` hold the platform code. `test@nonAndroid` holds the tests. `composeResources` holds the text and the images. |
| `androidApp/` | The Android app: manifest, launcher icon, splash theme and R8 rules. |
| `iosApp/` | The iOS app: SwiftUI host, `Info.plist` and Xcode project. |
| `kiteconfig/` | The installed KiteConfig plugin. Do not edit these files. |
| `build-plugins/app-icons/` | The `syncAppIcons` task. |
| `build-plugins/android-inputs/` | Reruns the Android build after a change in `res/`, the manifest or the R8 rules. The APK build of the toolchain does not track these files yet. |
| `art/` | The source images of the app icon. |
| `scripts/` | `verify.sh` runs every check. |

[RECIPES.md](RECIPES.md) has the steps to add common features and to remove parts of this project.
