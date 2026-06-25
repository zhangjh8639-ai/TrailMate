# TrailMate Production Outdoor Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make TrailMate's Android app flow and map/GPS behavior credible for production outdoor use, not only prototype review.

**Architecture:** Keep the existing local-first Android Compose architecture. Improve UX contracts and test coverage around onboarding completion, AMap readiness, full-screen navigation, and foreground track recording without adding a backend or live-tracking service in this slice.

**Tech Stack:** Kotlin, Jetpack Compose, Android LocationManager, foreground services, AMap Android SDK, Gradle/JUnit/Compose instrumentation.

---

### Task 1: AMap Loading And Fallback State

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/AmapRouteMap.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Test: `android-app/src/test/java/com/trailmate/app/core/map/TrailMapReadinessEngineTest.kt`

- [x] Add an explicit loading state for AMap surfaces before first map loaded callback.
- [x] Show a lightweight `在线底图加载中` overlay instead of a blank gray surface.
- [x] If map loaded callback does not fire within a short timeout, keep route actions available and show `底图加载较慢，可继续查看本地路线`.
- [x] Verify route polyline remains visible after tile load.
- [x] Run `.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.map.TrailMapReadinessEngineTest --no-daemon`.

### Task 2: Onboarding Permission Completion Regression

**Files:**
- Modify: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`
- Modify only if needed: `android-app/src/main/java/com/trailmate/app/feature/onboarding/OnboardingScreens.kt`

- [x] Add an instrumentation test that completes account/profile/map-service flow.
- [x] Grant location permission through the test rule or system dialog helper.
- [x] Assert the app lands on the main tab shell after permission acceptance.
- [x] If the test exposes a callback bug, fix the onboarding pending-state handling minimally.
- [x] Run `.\gradlew.bat :android-app:connectedDebugAndroidTest --no-daemon` or the focused test class.

### Task 3: Route Cockpit Inset And First Viewport QA

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] Ensure the default route cockpit action panel is never hidden by bottom navigation.
- [x] Add bottom padding/insets only where needed.
- [x] Keep the route tab first viewport limited to map, checkpoint panel, status chips, primary action, full-screen, and safety copy.
- [ ] Capture screenshots for 1080x1920 and at least one shorter-height emulator if available.

### Task 4: Data Page Evidence Language Cleanup

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/data/DataScreen.kt`
- Test: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] Replace primary copy such as `供 AI 评估使用` with user-facing data-control language.
- [x] Keep history GPX import available, but frame it as `用于下次路线评估` or `能力背景`.
- [x] Verify the page still has a clear import action and no internal AI/evidence language in the first viewport.

### Task 5: Offline Map Readiness Proof

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/AmapLaunchDiagnostics.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/map/AmapOfflineMapLauncher.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Test: `android-app/src/test/java/com/trailmate/app/core/map/AmapLaunchDiagnosticsEngineTest.kt`

- [x] Keep the AMap offline map manager behind Key + SDK + privacy gates.
- [x] Add UI copy that distinguishes `离线路线包` from true AMap offline base map data.
- [x] Add a manual QA checklist for opening AMap offline manager and returning to TrailMate.
- [x] Add an instrumentation smoke test proving `OfflineMapActivity` can start from app context.
- [x] Add an SDK reader for downloaded/saved-region availability.
- [x] Verify the SDK reader returns saved-region status on emulator.
- [x] Block departure readiness when downloaded offline base-map regions have not been matched to the target route.
- [x] Match downloaded offline city/province metadata against the active route's reverse-geocoded target region.
- [x] Apply the same target-route coverage gate to AMap launch diagnostics.
- [x] Apply target-route coverage and network-disabled tile proof to the outdoor production release gate.
- [x] Apply network-disabled tile proof to AMap diagnostics, departure readiness, and route cockpit start.
- [x] Store network-disabled tile proof locally by route key and target adcode.
- [x] Keep the route cockpit start action blocked until GPS has a reliable fix.
- [x] Do not claim offline base maps are saved until a target region is downloaded and verified offline.

### Task 6: Physical Device Field QA Checklist

**Files:**
- Create: `docs/qa/trailmate-physical-device-field-qa.md`

- [x] Define a 30-minute outdoor walk test with screen on/off intervals.
- [x] Include GPS accuracy, route projection, track point count, notification controls, battery drain, pause/resume, finish/save, and safety share checks.
- [x] Include weak-signal and airplane-mode expectations.
- [x] Include pass/fail criteria for production release.

### Verification Commands

- [x] `.\gradlew.bat :android-app:testDebugUnitTest --no-daemon`
- [x] `.\gradlew.bat :android-app:compileDebugAndroidTestKotlin --no-daemon`
- [x] Focused connected onboarding/data smoke test on emulator.
- [x] Focused connected full-screen navigation and dock inset smoke test on emulator.
- [x] Focused connected AMap offline map manager Activity smoke test on emulator.
- [x] Focused connected AMap offline base-map saved-region reader smoke test on emulator.
- [x] Manual emulator screenshots saved under `outputs/qa/production-outdoor-readiness/`.
