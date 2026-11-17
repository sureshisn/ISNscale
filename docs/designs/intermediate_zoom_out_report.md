# Stage 8 — Intermediate Zoom Out Review

- **Target:** `D:\open\ISNscale\README.md` (full file, lines 1–118)
- **Stage:** 8 — Intermediate Zoom Out
- **Date:** 2026-11-17
- **Reviewer:** `egm-zoom-out` (Skeptical Architect)
- **Context supplied:** Stage 5, 6, and 7 reports; the implementation the README describes (`MainActivity.kt`, `ar/ArMeasureView.kt`, `ar/ArSupport.kt`, `ar/Renderers.kt`, `Measurement.kt`, `MeasurementTest.kt`, `app/build.gradle.kts`, `.github/workflows/build.yml`, `AndroidManifest.xml`)

## Findings

### 1. 🔴 Blocker — Accumulated remediation plan is over-scoped and internally contradictory; audience never decided

- **Affected scope:** whole document; remediation demanded by C-1, C-4, C-5, C-6, C-7, C-13
- **Rationale:** Nothing in Stages 5–7 establishes what this document *is*. It
  is currently a competent 118-line user-and-operator README. Stage 6 measures
  it as a self-contained explainer; Stage 7 measures it against the Feature
  Spec Standard and therefore demands Problem Statement, Goals/Non-Goals,
  Architecture contracts, Alternatives, Risks, and privacy — roughly doubling
  it into a hybrid spec. Those demands conflict with Stage 5 §1 (tighten the
  promise, remove repetition). Applied literally, the result is a long document
  with two audiences, two abstraction levels, and no owner for either. Record
  one scope decision before writing fixes: README = "what it is, what you need,
  how to run it, where the caveats are"; the three genuinely non-obvious
  decisions (ARCore-only, demo signing key, four-tier tap fallback) get one
  short paragraph or a single decision record, not the standard's full section
  set.

### 2. 🟡 Major — Architecture story omits what the app contributes and its non-obvious design decision

- **Affected scope:** "How it works" (lines 7–21), Features line 25, caveat lines 55–57
- **Rationale:** The document presents reference-free depth measurement as the
  app's own mechanism, then walks it back at line 55. The honest and shorter
  story is one sentence: ARCore supplies pose tracking and depth; this app
  turns taps into world points, computes one straight-line distance, and draws
  it. That makes the caveats self-explanatory and stops readers attributing
  accuracy to the app. Separately, the implementation's defining behaviour is
  invisible: `ArMeasureView.hitTest` is a four-tier fallback (depth → plane →
  feature point → any hit with a pose, including non-TRACKING artifacts). A
  maintainer would never guess the last resort can return an arbitrary hit.
  That is a one-sentence disclosure, not a table.

### 3. 🟡 Major — C-2's "Risks & failure modes" table cannot be filled truthfully

- **Affected scope:** proposed `## Known limits & failure modes` section (C-2); caveat lines 55–57
- **Rationale:** The requested shape is condition → user-visible behaviour →
  mitigation. The code has no re-anchoring, no measurement persistence across
  tracking loss, and no recovery path: on `TrackingState.STOPPED` the render
  loop returns before drawing, so the ruler and readout vanish and stored
  endpoints go stale (`ArMeasureView.kt` 398–418). A mitigation column forces
  invented behaviour or a speculative promise. Prefer a two-sentence "Limits"
  paragraph (low light / low texture / fast motion / reflective or transparent
  surfaces / no re-anchoring: re-tap after tracking recovers) with no
  mitigation column.

### 4. 🟡 Major — C-6 asks the README to duplicate contracts already in the code

- **Affected scope:** proposed `Architecture` section (C-6); "Project structure" lines 88–106
- **Rationale:** C-6 demands threading/ownership, Compose-over-GL integration,
  state-machine edge cases, and rounding/epsilon rules. All of that is already
  documented next to the code it constrains: `ArMeasureView.kt` 42–48,
  107–121, 224–229 ("must be called ON THE GL THREAD"), 362–367, 488–494;
  `Measurement.format` 56–73. Copying it into prose creates a second source of
  truth that will rot. The existing four-step data flow plus file tree is the
  correct altitude; link the files rather than restating them.

### 5. 🟡 Major — Dead calibration path contradicts the product premise

- **Affected scope:** README lines 83, 95; `Measurement.kt` 12–27; `MeasurementTest.kt` 8–26
- **Rationale:** `distancePx`, `cmPerPixel`, and `pxToCm` are unused — no caller
  exists — and their own comment says they are "kept for reference-object
  mode", a mode that is not implemented and directly contradicts "no
  calibration card, no reference object". Four of eleven unit tests exist only
  to keep the dead path green, so the README's "unit tested" claim partly
  certifies speculative code. Correct fix: delete the three helpers and their
  tests — not document the phantom mode.

### 6. 🟡 Major — "Tap anywhere to start a new measurement" is not implemented

- **Affected scope:** "Using the app" step 6 (line 52)
- **Rationale:** In `processTap`, a third tap starts a new measurement only if
  the hit test resolves; a miss returns early with "No surface there" and
  leaves the completed measurement on screen (`ArMeasureView.kt` 441–465).
  "Tap anywhere" documents a clearance gesture that does not exist. State: tap
  a surface to begin a new measurement, or press Reset.

### 7. ⚪ Nit — Unit contract ambiguous; implementation resolves it silently

- **Affected scope:** Features line 28, usage step 7 line 53
- **Rationale:** “Automatic `m` above one metre” reads as a global rule. The
  implementation applies it only in metric; with inches selected a 500 cm
  measurement renders as "196.9 in" and never switches (`Measurement.format`
  64–73, `MainActivity.kt` 224). One clause fixes it: "metric switches to `m`
  above one metre; inches stay inches."

### 8. ⚪ Nit — Licence claim unsupported by the repository

- **Affected scope:** License section, lines 116–118
- **Rationale:** A bare "MIT" is not a licence grant, and there is no `LICENSE`
  file at the repo root. The document asserts a legal status the project does
  not carry. Fix: add the file plus holder/year.

### 9. ⚪ Nit — Install story implies build progression the build does not provide

- **Affected scope:** "Installing on a phone" (lines 59–69)
- **Rationale:** The doc explains signatures are stable so "a newer build
  installs directly over an older one", but `versionCode = 3` / `versionName =
  "2.1"` are hardcoded, so every CI artifact is indistinguishable in Android's
  app info. Either surface the version or state plainly that artifacts are
  unversioned.

## Verdict

The README's high-altitude story — reference-free 3D measurement, ARCore as the
engine, CI-signed sideload distribution — is coherent and stays coherent after
the comprehension and critic passes; nothing was papered over because the
document has not changed since Stage 5. The risk at this stage is entirely in
the remediation direction: Stage 7's Blocker/Major set, applied literally,
would add a design-spec skeleton (alternatives, goals, risks matrix,
architecture contracts) to a document whose subject is a thin ARCore client,
duplicating contracts already documented beside the code and forcing mitigation
claims the code cannot support.

Accept only: one identity sentence naming ARCore as the engine and the app as
the tap→distance→draw layer; one sentence on the four-tier tap fallback; one
short Limits paragraph without a mitigation column; the "tap anywhere"
correction; and deletion of the dead calibration helpers so the "reference-free,
unit tested" claim becomes true. Everything else in C-1…C-14 is either already
satisfied at the right altitude or belongs in code or a future decision record.

## Stage summary

| Severity | Count | Notes |
| :--- | :--- | :--- |
| 🔴 Blocker | 1 | Undecided document audience / over-scoped remediation |
| 🟡 Major | 4 | Architecture story, un-fillable risk table, contract duplication, dead calibration path, false tap-anywhere contract |
| ⚪ Nit | 3 | Unit precedence, licence, unversioned artifacts |
