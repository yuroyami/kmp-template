# KmpTemplate

KmpTemplate is an app for Android and iOS with one Compose UI. Read [README.md](README.md) for the module layout and the build commands. Open work lives in GitHub issues.

## Build

- This project builds with the Kotlin Toolchain (`./kotlin`), not Gradle.
- Run one heavy build at a time. Two builds in one folder fail with "Timeout waiting to lock".
- `./kotlin build` never runs tests.
- Always pass `-d <serial>` to `./kotlin run -m androidApp`. Without `-d`, the toolchain downloads a system image of about 8 GB, even when a device is connected.
- `scripts/verify.sh` runs every check.

## Gotchas

- The package `app`, the style `Theme.App` and the file `app.preferences_pb` are generic on purpose. Do not rename them per app.
- Test class names end in `Test` or `Tests`. The JVM test runner skips other names.
- Test function names have no commas, because a comma breaks the iOS test compile.
- iOS tests link without friend modules, so they reach only public names.
- Do not read `Res` in a test. The iOS test binary has no Compose resources next to it.
- The tests live in `test@nonAndroid`, because an Android host test has no saved state and no files.
- Do not put an SVG in `composeResources`, because it crashes Android. Use vector XML.
- The toolchain has no core library desugaring. A Java API above minSdk 26 crashes on old devices.
- The debug and the release build share one application ID.
- The deployment target 15.0 is a manual edit in the Xcode project. Keep it when the toolchain changes the file. The toolchain writes `ENABLE_USER_SCRIPT_SANDBOXING = NO`. Do not accept an Xcode suggestion that sets it to YES, because the iOS build then stops.
- `shared` has no R class. Code reads resources by name, so the shrinker can remove them. [RECIPES.md](RECIPES.md) has a keep list.
- `Dispatchers.IO` and `runBlocking` compile in `src` only while no web platform exists.
- Keep the saveable decorator first in `NavDisplay`.
- Make a ViewModel with `viewModel {}`, not `remember {}`. With `remember`, `onCleared` never runs.
- Do not start an HTTP client or a database inside a composable.
- Do not keep the first back stack in a ViewModel. The composition owns it, and an Activity recreation builds a new one. [RECIPES.md](RECIPES.md) has two safe ways to navigate from a ViewModel.
- Do not put a secret in a route. Routes go to saved state.
- A public name that matches a C macro breaks the iOS header. `NAME_MAX` is one example.
- Add a library when the first file imports it, not before.
- Keep the background colors in step: `Theme.kt`, `androidApp/res/values*/colors.xml` and `LaunchBackground.colorset`.
- Delete `build-plugins/android-inputs` when a toolchain release has [KTC-6050](https://youtrack.jetbrains.com/issue/KTC-6050). That toolchain issue adds `res/`, the manifest and the R8 rules to the inputs of the APK build.
