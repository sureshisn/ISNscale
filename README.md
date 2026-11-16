# ISNscale

**ISNscale** is an Android app that measures real-world object lengths using the phone's camera.

> **Status:** the camera preview, measurement UI and measurement math are implemented and
> tested. The computer-vision calibration step (turning pixels into centimetres) is still a
> documented placeholder in `analyzeImage()`.

## Features

- Real-time camera preview via **CameraX**
- Material Design 3 UI (light/dark, Compose)
- Calibration-based measurement math (`Measurement.kt`) with unit tests
- Automated CI build that publishes installable APKs

## Installing on a phone

1. Open the latest successful run: <https://github.com/sureshisn/ISNscale/actions>
2. Download an artifact at the bottom of the run page:
   - **`debug-apk`** → `app-debug.apk` (recommended for testing)
   - **`release-apk`** → `app-release.apk` (same app, non-debuggable)
3. Unzip the artifact and copy the `.apk` to the phone.
4. On the phone, allow "Install unknown apps" for the app you use to open the file
   (Files/Chrome), then tap the APK.

Both APKs are signed with the same key, so a newer build installs directly over an
older one. If you install over a build made *before* this key existed, Android reports
`App not installed` / signature mismatch — uninstall the old copy first:

```bash
adb uninstall com.example.isnscale
adb install app-debug.apk
```

## Requirements

- `minSdk` 24 (Android 7.0), `targetSdk` 35
- A physical device with a camera (the emulator has no usable camera)

## Building

```bash
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
./gradlew testDebugUnitTest # unit tests
```

JDK 17+ is required (Android SDK tooling refuses older JDKs). Android Studio's bundled
JDK is fine.

## Permissions

- `CAMERA` — required to measure objects

## Project structure

```
ISNscale/
├── .github/workflows/build.yml   # CI: build debug + release APKs, run tests
├── app/src/main/java/com/example/isnscale/
│   ├── MainActivity.kt           # Activity, Compose UI, CameraX binding
│   ├── Measurement.kt            # Pure measurement math (unit tested)
│   └── ui/theme/                 # Material 3 colour scheme and typography
├── app/src/test/java/...         # JUnit tests
├── keystore/isnscale-demo.jks    # Public demo signing key (see below)
├── build.gradle.kts              # Project-level Gradle
├── settings.gradle.kts
└── app/build.gradle.kts          # App-level Gradle
```

## Signing

`keystore/isnscale-demo.jks` is a **throwaway demo key** committed on purpose so that CI
produces installable APKs whose signature never changes between runs. Its password is
`isnscale`. Do not reuse it for anything you actually publish.

For a real release, create your own keystore and supply it through CI secrets instead.

## License

MIT
