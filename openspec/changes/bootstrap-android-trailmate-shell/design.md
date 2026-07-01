## Context

The repository is intentionally clean after the rebuild reset. `AGENTS.md` now defines TrailMate as a production-grade Android native track-navigation app with five tabs: `发现 / 路线 / 导航 / 记录 / 我的`. The Lovable prototype lives outside this repo at `D:\workSpace\trailguide-pro` and provides visual/interaction reference, while implementation must follow the Android-first constraints in `AGENTS.md` and `TRAILMATE_CODEX_SPEC.md`.

The first Android PR must create a foundation that is safe to extend. It should establish build tooling, package/module boundaries, Chinese UI shell, and verification commands without implementing full GPX import, route matching, live GPS, Foreground Service tracking, or offline map packages yet.

## Goals / Non-Goals

**Goals:**

- Create a Kotlin + Jetpack Compose + Material 3 Android foundation.
- Provide a runnable app shell with exactly five tabs: `发现`, `路线`, `导航`, `记录`, `我的`.
- Encode the current information architecture and remove old IA affordances from the shell.
- Establish documented package/module boundaries for later `core.geo`, route import, navigation, tracking, offline data, and safety work.
- Add build and test commands that can run on the local Windows Android environment.
- Document how Lovable prototype references are used during Android implementation.

**Non-Goals:**

- No independent `规划` tab or feature package.
- No equipment recommendations, equipment catalog, marketplace, or equipment screens.
- No complex pre-trip checklist page or gatekeeping flow.
- No real GPS tracking, Foreground Service, GPX/KML parser, MapLibre integration, offline package download, or backend API implementation in this first change.
- No Replit dependency; navigation algorithms will be designed and tested in repo-local Kotlin modules in later changes.

## Decisions

### Decision 1: Start With a Native Android Shell PR

Use a small first PR for Gradle, Compose, Material 3, app identity, documented package layout, and 5-tab shell.

Rationale: This gives every later feature PR a reliable target for build/test/install and prevents product scope drift before the project compiles. It also makes code review practical.

Alternatives considered:

- Build navigation algorithms first: rejected because there is no checked-in Android/Kotlin test harness yet.
- Clone the Lovable React prototype into Android directly: rejected because Lovable is a design reference, not the production architecture.

### Decision 2: Treat Lovable as Design Evidence, Not Source Code

Reference `D:\workSpace\trailguide-pro` for screen names, Chinese copy tone, tab hierarchy, and navigation states, but do not import React, Tailwind, shadcn, web routing, or generated UI code into the Android app.

Rationale: The Android app must remain Kotlin/Compose-native, and direct web code reuse would create wrong abstractions.

### Decision 3: Keep Feature Boundaries Aligned With Current IA

Reserve feature boundaries such as `discover`, `routes`, `navigation`, `records`, `profile`, and `safety`. Do not create `planner`, `equipment`, `community`, or `pretrip_check`.

Rationale: Directory names are durable architecture signals. Keeping obsolete names out of the tree reduces future accidental scope expansion, and avoiding empty package marker code keeps the first PR clean.

### Decision 4: Build Verification Before Full Feature Work

The first implementation must run at least `assembleDebug` and unit tests. If an APK is produced, install or smoke-test on the connected Android device when feasible.

Rationale: The project already has a connected real device and Android SDK. Early verification avoids delaying toolchain failures until navigation work begins.

## Risks / Trade-offs

- Risk: A shell-only PR can feel thin for a production app. Mitigation: keep it small but real: runnable app, package boundaries, tests, docs, and real-device smoke path.
- Risk: Lovable prototype may contain UI ideas that conflict with `AGENTS.md`. Mitigation: `AGENTS.md` wins; record any conflict in PR notes.
- Risk: Adding too many dependencies in the first PR increases setup fragility. Mitigation: start with core Android Gradle Plugin, Kotlin, Compose, Material 3, and test tooling only.
- Risk: Worktree-generated files can pollute the main checkout. Mitigation: keep feature work in `.worktrees/<branch>` and keep `.worktrees/` ignored.
