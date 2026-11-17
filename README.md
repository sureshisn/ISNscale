# ISNscale

**ISNscale** is an **ARCore** measuring tape for Android. ARCore supplies pose tracking and, on
supported phones, a depth map; ISNscale turns your taps into 3D world points, computes the
straight-line distance between them, and draws it over the camera feed. No calibration card, no
reference object, no ruler.

## How it works

1. ARCore tracks the phone's position and orientation in 3D space.
2. Each tap is turned into a real-world 3D point using ARCore hit testing, which uses the
   **Depth API** on supported phones to land on arbitrary objects (not just detected floors
   and walls).
3. The distance is computed between those two 3D points.
4. The virtual ruler is drawn between the two points and follows the object as you move the
   phone.

Each tap is resolved by a four-tier fallback: a depth hit if one is available, then a detected
plane, then a tracked feature point, and finally any hit ARCore returns. The last tier accepts
approximate surfaces, so a tap made while tracking is weak can land imprecisely.

Because ARCore supplies a depth estimate for each tapped point, no calibration card or reference
object is needed. That depth is inferred from motion and sensors rather than measured directly,
so the accuracy figures below are approximate.

## Features

- 📏 **Reference-free measurement** — tap two points, get centimetres
- 🥽 **AR overlay** — 3D ruler anchored to the real world
- 🔁 **Live tracking** — the ruler follows the object as you move around it
- 📐 **cm / inches** toggle, automatic `m` above one metre
- 🖤 **Full-screen AR UI** on top of the camera feed
- 📱 **Graceful fallbacks** — clear screens for missing camera permission, missing
  Google Play Services for AR, or unsupported hardware

## Requirements

| Item | Requirement |
|---|---|
| Android | 7.0 (`minSdk` 24) or newer, `targetSdk` 35 |
| Hardware | ARCore-capable phone with a back camera |
| Software | Google Play Services for AR (the app offers to install it) |

ARCore is supported on most recent phones from Samsung, Google, OnePlus, Xiaomi, Oppo, Vivo,
Motorola and others. Emulators do not support it, and neither do some low-end or "Go" devices —
those show an explanatory screen instead of crashing.

## Using the app

1. Open ISNscale and grant camera access.
2. If prompted, let it install **Google Play Services for AR** (one time only).
3. Move the phone slowly until the status pill reads **Tracking**.
4. Tap the first end of what you want to measure — a white marker appears.
5. Tap the second end — a cyan line joins the markers and the distance is shown.
6. Tap a surface to start a new measurement, or press **Reset**.
7. Use **Show inches / Show cm** to switch units.

Accuracy is typically within a centimetre or two at short range on phones with depth support;
on phones without the Depth API, taps only land on detected planes, so flat surfaces
(floor, table, wall) measure best. Accuracy degrades as the measured span grows.

## Limits

Low light, low-texture surfaces (blank walls, glass, gloss), fast movement, and reflective,
transparent, or moving objects all degrade tracking and depth. When tracking stops, the ruler and
readout disappear and the stored endpoints go stale — re-tap after tracking recovers. There is no
re-anchoring, no measurement history, and nothing is persisted between sessions.

## Installing on a phone

1. Open the latest successful run: <https://github.com/sureshisn/ISNscale/actions>
2. Download an artifact at the bottom of the run page:
   - **`debug-apk`** → `app-debug.apk` (recommended for testing)
   - **`release-apk`** → `app-release.apk` (same app, non-debuggable)
3. Unzip the artifact and copy the `.apk` to the phone.
4. On the phone, allow "Install unknown apps" for the app you use to open the file
   (Files/Chrome), then tap the APK.

Both APKs are signed with the same key, so a newer build installs directly over an older one.
If you install over a build made *before* this key existed, Android reports "App not installed"
(signature mismatch) — uninstall the old copy first:

```bash
adb uninstall com.example.isnscale
adb install app-debug.apk
```

## Building

```bash
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
./gradlew testDebugUnitTest # unit tests (measurement math)
```

JDK 17+ is required (Android SDK tooling refuses older JDKs). Android Studio's bundled JDK is fine.

## Project structure

```
ISNscale/
├── .github/workflows/build.yml   # CI: build debug + release APKs, run tests
├── app/src/main/java/com/example/isnscale/
│   ├── MainActivity.kt           # Compose screen: permission + AR status + AR view
│   ├── Measurement.kt            # Pure measurement math (unit tested)
│   ├── ar/
│   │   ├── ArMeasureView.kt      # ARCore session, tap hit testing, measurement state
│   │   ├── Renderers.kt          # GL: camera background + virtual ruler
│   │   └── ArSupport.kt          # ARCore availability / install helpers
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
