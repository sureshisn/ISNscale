# Stage 6 — Comprehension Test

- **Target:** `D:\open\ISNscale\README.md`
- **Stage:** 6 — Comprehension Test (self-containment, no document standard)
- **Date:** 2026-11-17
- **Reviewer:** `egm-comprehension` (Naive Reader)
- **Ranges:** FeatureRange 1–118 (full file); DetailedRange N/A (no implementation plan section)

#### What this proposes

ISNscale is an Android phone app that works as an AR measuring tape: the user
points the rear camera at a scene, taps two points on the screen, and the app
displays the real-world distance between them in centimetres (with an inches
toggle and automatic switch to metres above 1 m). Measurement is done in 3D
rather than in screen pixels: ARCore tracks the phone's pose, each tap is
converted to a 3D world point via ARCore hit testing (preferring the Depth API
on phones that support it), the distance between the two 3D points is computed,
and a virtual ruler is rendered between them, staying anchored to the object as
the phone moves. The README is a single user-and-developer document: it lists
features, phone requirements, in-app usage steps, how to install prebuilt APKs
from GitHub Actions artifacts, how to build with Gradle, the repository layout,
and the signing arrangement (a deliberately committed demo keystore so CI
output has a stable signature).

#### How the existing system works, per this document

The "existing system" is the app plus its build/CI pipeline, as the document
itself describes it. The app is a Jetpack-Compose Android app whose
`MainActivity.kt` hosts a screen combining camera-permission handling, an AR
status indicator, and an AR view; `Measurement.kt` holds pure, unit-tested
distance math; `ar/ArMeasureView.kt` owns the ARCore session, tap hit testing
and measurement state; `ar/Renderers.kt` contains the GL rendering of camera
background and ruler; and `ar/ArSupport.kt` handles ARCore availability checks
and installation prompts. A GitHub Actions workflow (`.github/workflows/build.yml`)
builds debug and release APKs and runs unit tests, both APKs signed with the
committed `keystore/isnscale-demo.jks` demo key so successive builds install
over each other. Unsupported devices and missing camera permission or missing
Play Services for AR are handled by explanatory screens rather than crashes.

## Findings

### 1. Major — Undefined core terms: ARCore and Depth API

- **Affected scope:** "How it works" (lines 9–21)
- **Finding:** ARCore is introduced only by analogy — "the same technology
  behind commercial AR measuring apps" (line 9) — which defines nothing for a
  reader who does not already know those apps or what ARCore is (a Google
  SDK/plane-and-pose-tracking runtime, not an app). No link to Google's ARCore
  documentation is given anywhere. **Depth API** is bolded as if it were a
  defined term (line 13) but is never explained (a per-frame depth map from
  supported devices' ToF/stereo sensors that lets hit tests land on non-planar
  geometry) and is unlinked. The core mechanism of the whole product rests on
  these two undefined, unlinked terms, so a fresh reader cannot follow *why*
  the measurement is claimed to be accurate.

### 2. Major — "ARCore hit testing" never explained

- **Affected scope:** line 12
- **Finding:** "Hit testing" is the pivot of the entire "tap → 3D point"
  conversion yet is described only as a name. The document never states what a
  hit test does (cast a ray from the screen tap into the tracked scene and
  return the intersection with a detected plane or depth surface), so the naive
  reader cannot connect step 2 to step 3, nor understand why taps "only land on
  detected planes" on non-Depth phones (line 56) — that later caveat is a
  non-sequitur without the hit-test explanation.

### 3. Major — Build prerequisites incomplete

- **Affected scope:** "Building" (lines 78–86)
- **Finding:** Only "JDK 17+" is stated as a build prerequisite. Android
  SDK/command-line tools, `ANDROID_HOME`/`local.properties` SDK location, and
  the Gradle wrapper's first-run download are all assumed. A reader following
  the three `./gradlew` commands on a clean machine will fail with
  SDK-not-found and get no guidance from the document. Additionally, the
  commands are POSIX-shell form only (`./gradlew`); Windows users need
  `gradlew.bat`, which is not mentioned.

### 4. Major — No way to verify device support

- **Affected scope:** "Requirements" (lines 33–43)
- **Finding:** "ARCore-capable phone" is the gating requirement, but the
  document offers only a vendor list ("Samsung, Google, OnePlus, Xiaomi, …")
  with no link to Google's official supported-devices list. A reader cannot
  verify whether *their* specific model is supported, which is the single
  question this section exists to answer. The same applies to line 55's
  "phones with depth support" — the reader has no way to find out whether
  their phone is one.

### 5. Major — Real-release signing instructions are not actionable

- **Affected scope:** "Signing" (lines 108–114)
- **Finding:** "For a real release, create your own keystore and supply it
  through CI secrets instead" is a hand-wave. It names no secret(s), no
  mechanism (environment variables read by `app/build.gradle.kts`, Gradle
  properties, base64-encoded keystore), and no link to GitHub Actions secrets
  documentation. A reader told to do this cannot act on it, and there is no
  indication of how the current demo key is wired into the build, so they
  cannot tell which parts of the build config they must change.

### 6. Major — `adb` recovery path unexplained

- **Affected scope:** "Installing on a phone," lines 70–76
- **Finding:** The recovery path for an "App not installed" signature mismatch
  requires `adb`, a tool the document never introduces, never links (Android
  platform-tools), and never says how to obtain or connect. No alternative
  (uninstall through the phone's Settings → Apps) is offered, so a
  non-developer following the user-facing installation section is stranded
  exactly at the point where they need help.

### 7. Nit — ARCore vs Google Play Services for AR

- **Affected scope:** lines 9 vs 39/48
- **Finding:** Two names are used for what a naive reader cannot tell apart.
  The document never states the relationship (the Play Store-delivered runtime
  that provides ARCore on-device, versus the SDK the app is compiled against).
  Step 2's "let it install Google Play Services for AR" reads as optional (a
  one-time prompt), while the Requirements table treats it as mandatory
  software.

### 8. Nit — Measurement state machine only implied

- **Affected scope:** "Using the app," steps 5–6 (lines 51–52) and the **Reset**
  button
- **Finding:** "Tap anywhere to start a new measurement, or press **Reset**"
  leaves unclear whether the two are equivalent, whether tapping anywhere
  *before* the second point cancels the in-progress measurement, and whether
  Reset clears only the current measurement or everything on screen. The
  reader has to guess the tap behaviour for each state.

### 9. Nit — "Status pill" undefined; only one state named

- **Affected scope:** line 49
- **Finding:** The term "status pill" is not defined (the rounded status badge
  in the AR screen); only one of its possible states is named ("Tracking"), so
  the reader has no idea what other labels they might see, what they mean, or
  what to do about them.

### 10. Nit — "Short range" never quantified

- **Affected scope:** line 55
- **Finding:** "Typically within a centimetre or two at short range" carries
  the entire accuracy claim yet "short range" is never quantified, and no note
  explains that accuracy degrades with distance. A reader cannot tell at what
  range the quoted accuracy stops applying.

### 11. Nit — Mixed audience without signposting

- **Affected scope:** whole document; Requirements table (lines 37–38) and
  "Project structure" (line 94)
- **Finding:** The document alternates between end-user instructions (install
  APK, tap two ends) and developer material, without signposting the audience.
  Because of the overlap, the requirements table presents build-time jargon
  (`minSdk 24`, `targetSdk 35`) to phone users who cannot use it. In the
  "Project structure" listing, "Compose screen" assumes knowledge of Jetpack
  Compose, which is neither introduced nor linked.

### 12. Nit — GitHub Actions vocabulary unglossed

- **Affected scope:** lines 61–62
- **Finding:** GitHub Actions vocabulary ("run", "artifact", "successful") is
  used without a one-line gloss or link to GitHub's Actions docs, and nothing
  mentions that artifacts require being signed in to GitHub and are retained
  only for a limited period — both common points of confusion for a first-time
  user.

## Stage summary

| Severity | Count | Notes |
| :--- | :--- | :--- |
| 🔴 Blocker | 0 | — |
| 🟡 Major | 6 | Undefined core terms, unexplained hit testing, incomplete build prerequisites, unverifiable device support, non-actionable signing, unexplained `adb` |
| ⚪ Nit | 6 | Terminology, state machine, undefined UI terms, unquantified accuracy, mixed audience, Actions vocabulary |
