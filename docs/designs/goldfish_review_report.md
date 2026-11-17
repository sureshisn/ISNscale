# Goldfish Annotated Review Report

**Status:** resolved (partial — Stage 8 acceptance list applied)

**Target:** `README.md` (`full file`, 1–118)
**Resolved at:** 2026-11-17
**Resolution authority:** Stage 8 terminal checkpoint gate (user selected "Resolve findings with agent")

---

## Scope decision (Stage 8 §1, recorded before remediation)

This document is a repository README: what the app is, what it needs, how to run it, and where the
caveats are. It is **not** a feature specification, and it is not going to become one. Findings
that demand a design-spec skeleton (Problem Statement, Goals & Non-Goals, Architecture contracts,
Alternatives Considered, Risks matrix) are recorded here as contested on genre grounds; they
belong in a separate design document if one is ever written.

Three genuinely non-obvious decisions are disclosed in place instead of in a full section set: the
ARCore-only engine choice (intro), the four-tier tap fallback (`## How it works`), and the demo
signing key (`## Signing`, unchanged — already correct at the right altitude).

---

## Applied fixes (5)

| # | Source | Change | Location |
| :--- | :--- | :--- | :--- |
| A-1 | Stage 8 §2; C-3; C-4; S5 §1 | Identity sentence: names ARCore as the engine, the app as the tap→distance→draw layer; drops the appeal-to-popularity clause and the "true centimetre measurement" absolutism | Intro, `## How it works` |
| A-2 | Stage 8 §2 | Four-tier tap fallback disclosed, including that the last tier accepts approximate surfaces | `## How it works` |
| A-3 | Stage 8 §3; C-2 | `## Limits` paragraph: failure conditions, tracking-loss behaviour, absence of re-anchoring/history/persistence. No mitigation column | new section after `## Using the app` |
| A-4 | Stage 8 §6; N-14 | "Tap anywhere" → "Tap a surface" (the implemented behaviour; a missed hit test leaves the completed measurement on screen) | `## Using the app` step 6 |
| A-5 | Stage 8 §5 | Deleted dead calibration helpers `distancePx`, `cmPerPixel`, `pxToCm` and their four tests; removed the now-unused `kotlin.math.hypot` import | `Measurement.kt`, `MeasurementTest.kt` |

A-1 through A-4 also retire, as a side effect: the unqualified "point the camera at anything"
claim (style #1), the "same technology behind commercial AR measuring apps" adjacency (style #3),
"stays glued to the object" (N-3), and the causally wrong "because the measurement comes from real
depth … are irrelevant" (design #3).

---

## Prioritized Findings & AI Suggestions

### C-1 — No `Alternatives Considered` section [Structural, Design] — Severity: Blocker

- **Affected scope:** whole file
- **Discrepancy:** The document commits to a single path and justifies it by association rather
  than documented rejection of alternatives; the Alternatives Rule therefore fails.
- ***AI Suggestion:*** CONTEST
- ***Justification:*** The rule applies to specifications. The artefact under review is the
  repository README, evidenced by its own content: it opens with a product pitch, carries APK
  install steps, and documents a `.github/workflows/build.yml` file tree — none of which are
  specification elements. Stage 8 §1 ruled this remediation direction over-scoped. Recording five
  rejected alternatives would add roughly a third to the document's length for a reader who is
  trying to install an APK.
- ***Proposed Edit:*** None. If a design document is ever created under `docs/designs/`, port
  this finding there verbatim — it is a legitimate requirement for that artefact.

### C-2 — No `Risks & Limits` section [Structural, Design] — Severity: Blocker

- **Affected scope:** `## Requirements`, plus scattered caveats
- **Discrepancy:** Failure modes, security implications, scalability limits, and mitigations are
  never consolidated into an auditable section.
- ***AI Suggestion:*** PROPOSE FIX (shape contested, substance applied)
- ***Justification:*** Stage 8 §3 established that the requested condition → behaviour →
  mitigation table cannot be filled truthfully: `ArMeasureView.kt` has no re-anchoring and no
  measurement persistence, so a mitigation column would force invented behaviour or a speculative
  promise. The substance of the finding is valid and is now covered by `## Limits`, stated as
  admission rather than mitigation.
- ***Proposed Edit:*** **Applied** — see A-3.

### C-3 — Accuracy is unbacked and internally contradicted [Design, Style, Comprehension] — Severity: Major

- **Affected scope:** intro, `## How it works`, `## Using the app`
- **Discrepancy:** The only quantification is "within a centimetre or two at short range"; "short
  range" is undefined; "true centimetre measurement" is contradicted by the plane-only regime;
  the "real depth ⇒ no calibration" causality is wrong because the Depth API is inferred.
- ***AI Suggestion:*** PROPOSE FIX
- ***Justification:*** Valid. Fixed by attribution rather than by adding an accuracy table: the
  app no longer claims the accuracy, ARCore does, and the inference is disclosed. "Accuracy
  degrades as the measured span grows" removes the undefined "short range" as the load-bearing
  term.
- ***Proposed Edit:*** **Applied** — see A-1 and the closing sentence added to the accuracy
  paragraph.

### C-4 — No problem statement or differentiation [Structural, Design, Style] — Severity: Major

- ***AI Suggestion:*** CONTEST (partially absorbed)
- ***Justification:*** The differentiation sentence would be pure marketing copy in an install
  README, and Stage 8 §2 showed the honest version is shorter: state who computes what. That is
  now A-1. The residual point — why build this at all when Google Measure exists — is a product
  question with no evidence in the repository to answer it. ESCALATE-class content, not a
  documentation gap.

### C-5 — No Goals & Non-Goals [Structural, Design] — Severity: Major

- ***AI Suggestion:*** CONTEST
- ***Justification:*** Genre. A README that declares non-goals is inventing scope authority the
  document does not have. The practical ambiguity the finding identified ("is history a
  deliberate exclusion or just unbuilt?") is now answered factually in `## Limits`.

### C-6 — Architecture is a file tree, not a design [Design] — Severity: Major

- ***AI Suggestion:*** CONTEST
- ***Justification:*** Checkable contradiction: every contract the finding asks for already exists
  in the code the reviewer could read — `ArMeasureView.kt:224` ("must be called ON THE GL THREAD"),
  `:107–121` (texture re-registration on GL context loss), `:362–367` (retry-until-success),
  `:488–494` (hit-test ordering), and `Measurement.format` (rounding and unit rules). Copying them
  into prose creates a second source of truth. Stage 8 §4 reached the same conclusion
  independently.

### C-7 — No privacy/data-handling statement [Design] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred — outside the approved scope)
- ***Justification:*** Valid and cheap. Not applied because the Stage 8 gate authorised a
  five-item list. One line in `## Requirements` would close it, provided the manifest is checked
  first: if there is no `INTERNET` permission, say so explicitly, because "camera app with no
  network permission" is a verifiable claim, not a promise.

### C-8 — Public signing key implies unmitigated supply-chain risk [Design] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid. The section correctly warns against publishing with the key but does
  not state the consequence: anyone holding the public certificate can sign an APK that installs
  as an in-place upgrade. Publishing the certificate SHA-256 fingerprint and the key's validity
  end-date, and instructing users to trust only this repo's CI output, would close it.

### C-9 — Distribution under-specified, non-publishable as written [Design] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid. `com.example.isnscale` is a reserved namespace Play rejects, and
  artefacts are ephemeral and login-gated. Worth one short "Distribution" paragraph stating this
  is a sideload-only demo and that `applicationId` must change before any store release.

### C-10 — `## Requirements` heading collides with the standard's umbrella term [Structural] — Severity: Major

- ***AI Suggestion:*** CONTEST
- ***Justification:*** In a README, `## Requirements` unambiguously means prerequisites — the
  table beneath it is hardware and OS versions. The collision exists only inside a standard that
  this document is not being held to. Renaming it to satisfy a template check would make it worse
  for the actual reader.

### C-11 — Device support unverifiable [Style, Comprehension] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid and high value per word: replace the vendor list with a link to
  Google's ARCore supported-devices page. Deferred only because it was outside the approved list.

### C-12 — Build prerequisites incomplete [Comprehension, Design, Style] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid. A clean machine following the three documented commands fails at
  SDK discovery. Worth adding: Android SDK presence / `local.properties`, `gradlew.bat` for
  Windows, and a note that the unit tests cover only the measurement maths.

### C-13 — Release signing instructions non-actionable [Comprehension] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid. "Supply it through CI secrets" names no secret, no mechanism, and no
  indication of how the demo key is currently wired in.

### C-14 — `adb` recovery path unexplained [Comprehension] — Severity: Major

- ***AI Suggestion:*** PROPOSE FIX (deferred)
- ***Justification:*** Valid. The section is aimed at non-developers and strands them at the one
  point where they need help. Either link Android platform-tools or offer the Settings → Apps
  uninstall alternative.

### Nits (N-1 … N-17)

- ***AI Suggestion:*** DECLINE for N-1, N-2, N-6, N-8, N-9, N-12, N-16 — editorial preference or
  already satisfied at the right altitude; fixing them adds churn without adding information.
- ***AI Suggestion:*** PROPOSE FIX (deferred) for the three carrying real information:
  - **N-7 / Stage 8 §7** — Features and usage steps imply a global metre rule; the implementation
    switches to `m` only in metric, and inches stay inches indefinitely. One clause fixes it.
  - **N-10 / Stage 8 §8** — `## License` says "MIT" but no `LICENSE` file exists at the repository
    root. The document asserts a legal status the project does not carry.
  - **Stage 8 §9** — `versionCode = 3` / `versionName = "2.1"` are hardcoded, so the install story
    promises distinguishable upgrades that Android cannot show. State plainly that artefacts are
    unversioned.
- ***AI Suggestion:*** PROPOSE FIX (deferred) for **N-4** (unit control named inconsistently:
  "cm / inches toggle" vs "Show inches / Show cm") and **N-5** ("status pill" undefined).

---

## Handoff

| Outcome | Count | Items |
| :--- | :--- | :--- |
| Accepted and applied | 5 | A-1 … A-5 |
| Contested | 5 | C-1, C-4, C-5, C-6, C-10 |
| Escalated | 1 | C-4 residual (product rationale for existing) |
| Deferred, valid | 8 | C-7, C-8, C-9, C-11, C-12, C-13, C-14, plus the information-bearing nits |
| Declined | 7 | N-1, N-2, N-6, N-8, N-9, N-12, N-16 |

Remaining unaddressed issues in the document: 8 deferred items. The document is internally
consistent as it stands — every deferred item is an omission, not a contradiction.
