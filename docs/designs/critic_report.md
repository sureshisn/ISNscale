# Stage 7 — Critic Review

- **Target:** `D:\open\ISNscale\README.md` (lines 1–118, FeatureRange)
- **Stage:** 7 — Critic Review
- **Date:** 2026-11-17
- **Reviewers:** `egm-design` (Hostile Design Critic), `egm-structural` (Structural Layout Validator), `egm-style` (Technical Copy Editor)
- **Standard:** Feature Spec Standard (design, structural); persona mandates (style)

## Consolidated findings

### 🔴 Blocker

#### C-1 — No `Alternatives Considered` section (Standard: Alternatives Rule)

- **Sources:** structural #1, design #1
- **Finding:** The document commits to a single path (ARCore hit testing + Depth
  API) and justifies it by association ("the same technology behind commercial
  AR measuring apps") rather than documented rejection of alternatives.
  Viable options never addressed: plane-only hit testing as universal baseline;
  calibration-card photogrammetry; IMU/trigonometry approaches; not building
  (Google's free "Measure" app); raw ARCore+GL vs SceneView/Filament.
- **Fix:** Add a terse `## Design decisions & alternatives` table: option →
  rejected because (concrete reason). One line per option suffices.

#### C-2 — No `Risks & Limits` / failure-mode section

- **Sources:** structural #4, design #2
- **Finding:** The standard requires failure modes, security implications,
  scalability limits, and mitigations. Only install-time conditions are
  covered. Unmentioned runtime failures: tracking loss/degradation (low light,
  low texture, motion blur), session drift and re-anchoring policy,
  unmeasurable geometry (transparent/reflective/dark/thin/moving), lifecycle
  failures (backgrounding, config change, GL surface loss).
- **Fix:** Add a `## Known limits & failure modes` table: condition →
  user-visible behaviour → mitigation.

### 🟡 Major

#### C-3 — Accuracy is an unbacked assertion and internally contradicted

- **Sources:** design #3, style #1/#2/#5, comprehension #10, zoom-out #1
- **Finding:** Accuracy is the product, yet the only quantification is
  "typically within a centimetre or two at short range." "Short range" is
  undefined; no device matrix, method, or error distribution is given. Line 15's
  "true centimetre measurement" is contradicted by the plane-only regime at
  lines 56–57. "Because the measurement comes from real depth …" is causally
  wrong: the Depth API is an inferred depth map (motion stereo + ML), not
  calibrated ground truth.

#### C-4 — No Problem Statement or differentiation

- **Sources:** structural #2, design #9, style #3, zoom-out #2
- **Finding:** The doc opens with what the app is, never what problem it solves
  or who it serves, and gives no reason to choose it over free incumbents.

#### C-5 — No Goals & Non-Goals; scope unbounded

- **Sources:** structural #3, design #4
- **Finding:** No non-goals statement anywhere. It is unknowable whether
  history, area/volume, multi-segment, export, or iOS are deliberately excluded
  or simply unbuilt.

#### C-6 — Architecture is a file tree, not a design

- **Sources:** design #5
- **Finding:** Missing behavioural contracts: threading/ownership of the ARCore
  session; Compose-over-GL integration; measurement state machine edge cases
  (missed tap, tap while paused, zero-length); rounding/precision/epsilon rules;
  where the metre-threshold display rule lives.

#### C-7 — No privacy/data-handling statement

- **Sources:** design #6
- **Finding:** A camera-first app that never states whether frames stay
  on-device, are logged, persisted, or transmitted.

#### C-8 — Public signing key implies an unmitigated supply-chain risk

- **Sources:** design #7
- **Finding:** A committed, public certificate plus sideload-from-artifact
  workflow means anyone can produce an installable upgrade APK. "Do not reuse
  for publishing" limits blast radius but does not mitigate. No fingerprint,
  alias, algorithm, or validity end-date is given; password is public.

#### C-9 — Distribution path under-specified and non-publishable as written

- **Sources:** design #8
- **Finding:** `com.example.isnscale` is a reserved namespace Play rejects;
  changing it later breaks the advertised in-place upgrade path. Artifacts are
  ephemeral, login-gated, and branch/PR runs are indistinguishable from trusted
  builds. `targetSdk 35` edge-to-edge enforcement is unmentioned against the
  "full-screen AR UI" claim.

#### C-10 — `## Requirements` heading collides with the standard's umbrella term

- **Sources:** structural #5
- **Finding:** The standard uses "Requirements" for Problem Statement, Goals,
  Architecture, Alternatives, Risks. The doc uses it for hardware prerequisites,
  so heading-based compliance checks register a false pass.

#### C-11 — Device support unverifiable

- **Sources:** style #4, comprehension #4
- **Finding:** Vendor list with no link to Google's official supported-devices
  list; no way for a reader to check their model or depth support.

#### C-12 — Build prerequisites incomplete and unsafe

- **Sources:** comprehension #3, design #11, style #12
- **Finding:** Only "JDK 17+" stated (and "+" invites AGP/JDK incompatibilities);
  no Android SDK platform/build-tools/`ANDROID_HOME` guidance; no Gradle/AGP/
  Kotlin/ARCore pins; Windows `gradlew.bat` unmentioned; no statement of what
  the unit tests do not cover.

#### C-13 — Real-release signing instructions non-actionable

- **Sources:** comprehension #5
- **Finding:** "Supply it through CI secrets" names no secrets, no mechanism,
  no link, and no indication of how the demo key is wired into the build.

#### C-14 — `adb` recovery path unexplained

- **Sources:** comprehension #6
- **Finding:** The signature-mismatch recovery requires `adb`, which is never
  introduced, linked, or installed; no Settings-based alternative offered.

### ⚪ Nit

| ID | Finding | Sources |
| :--- | :--- | :--- |
| N-1 | "Graceful fallbacks"/"clear screens" are subjective; fallback behaviour stated twice (Features vs Requirements) | style #6, #7 |
| N-2 | Reference-free claim repeated three times (lines 4–5, 21, 25) | style #8 |
| N-3 | "stays glued to the object" vs "follows the object" — redundant and informal | style #9, zoom-out #3 |
| N-4 | Unit control named inconsistently ("cm / inches toggle" vs "Show inches / Show cm"); automatic metres vs inches precedence unspecified | style #10, design #12a |
| N-5 | "status pill" undefined; only the "Tracking" state is named | style #11, comprehension #9 |
| N-6 | "Android SDK tooling refuses older JDKs" — anthropomorphic and vague | style #12 |
| N-7 | "(one time only)" is an unsupported promise | style #13 |
| N-8 | Grammar mismatch in "the phone's own size, camera height, or any reference object are irrelevant" | style #14 |
| N-9 | `## Project structure` fence is untagged while the other two fences are tagged | structural #7 |
| N-10 | License section is a bare "MIT" with no holder, year, or link | structural #8, zoom-out #4 |
| N-11 | Features bullet fuses two behaviours (unit toggle + auto-metres), breaking list symmetry | structural #9 |
| N-12 | No status/scope marker distinguishing shipped from intended behaviour | structural #10 |
| N-13 | ARCore vs Google Play Services for AR relationship never explained | comprehension #7 |
| N-14 | Measurement state machine only implied (tap-anywhere vs Reset semantics) | comprehension #8 |
| N-15 | "short range" never quantified (absorbed into C-3) | comprehension #10 |
| N-16 | Mixed user/developer audience without signposting; `minSdk`/`targetSdk` jargon shown to phone users | comprehension #11 |
| N-17 | GitHub Actions vocabulary unglossed; no instruction to use the default branch's run | comprehension #12, design #12b |

### Positive observations

- **design #13:** Cost/quota economics are clean by construction — on-device
  inference only, no backend, no storage, no token spend. Worth stating
  explicitly as an architectural advantage.
- **style:** Lines 69–71 and 110–114 are properly calibrated: each caveat names
  a concrete reader action backed by a repository artifact.

## Traceability

| Consolidated | Raw findings |
| :--- | :--- |
| C-1 | structural 1, design 1 |
| C-2 | structural 4, design 2 |
| C-3 | design 3, style 1/2/5, comprehension 10, zoom-out 1 |
| C-4 | structural 2, design 9, style 3, zoom-out 2 |
| C-5 | structural 3, design 4 |
| C-6 | design 5 |
| C-7 | design 6 |
| C-8 | design 7 |
| C-9 | design 8 |
| C-10 | structural 5 |
| C-11 | style 4, comprehension 4 |
| C-12 | comprehension 3, design 11, style 12 |
| C-13 | comprehension 5 |
| C-14 | comprehension 6 |

## Stage summary

| Severity | Count |
| :--- | :--- |
| 🔴 Blocker | 2 |
| 🟡 Major | 12 |
| ⚪ Nit | 17 |
