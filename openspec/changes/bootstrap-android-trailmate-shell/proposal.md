## Why

TrailMate has completed a clean product reset: it is now a production-grade Android native track-navigation app, not a planning, equipment, or community MVP. We need a disciplined Android foundation that encodes the new 5-tab information architecture, keeps legacy scope out, and gives future navigation, GPX import, and tracking work a stable build/test base.

## What Changes

- Create the first Android-native project foundation for Kotlin + Jetpack Compose + Material 3 development.
- Establish the fixed bottom navigation IA: `发现 / 路线 / 导航 / 记录 / 我的`.
- Add a minimal app shell with Chinese copy and explicit absence of legacy `规划`, equipment, community, marketplace, and complex pre-trip-check flows.
- Add foundational module boundaries for future domain, geo, offline, location, map, and tracking work without implementing those feature bodies in this change.
- Add build/test/lint entry points appropriate for Android development and real-device verification.
- Add documentation tying implementation to the Lovable prototype source at `D:\workSpace\trailguide-pro`.

## Capabilities

### New Capabilities

- `android-app-shell`: Defines the Android native project skeleton, 5-tab app shell, design reference contract, quality gates, and real-device smoke-test expectations.

### Modified Capabilities

- None.

## Impact

- Affected code: Gradle project files, Android app module, foundational package/module layout, README/developer docs.
- Affected tools: Gradle Wrapper, Android SDK 36, Kotlin, Jetpack Compose, Material 3, unit test tooling.
- Affected design process: Lovable prototype is a reference source, but `AGENTS.md` and OpenSpec are the implementation contract when conflicts exist.
- Affected QA: first PR must run local build/unit tests and include a real-device smoke-test path using `adb`.
