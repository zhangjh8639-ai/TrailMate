# TrailMate Map Readiness UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the hard-coded light-navigation sketch with a real GPX-backed route projection and a production-facing map readiness state that can later host AMap SDK.

**Architecture:** Keep the current Compose screen intact, but move map capability decisions and route projection into small pure Kotlin units under `core/map`. The UI consumes those units to show whether TrailMate is using local GPX preview or a production AMap provider.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit, current Android app module.

---

### Task 1: Route Map Projection

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/map/TrailMapProjection.kt`
- Test: `android-app/src/test/java/com/trailmate/app/core/map/TrailMapProjectionTest.kt`

- [x] **Step 1: Write failing tests**
  - Test multiple `RoutePoint` values project inside the padded viewport.
  - Test empty input returns an empty list.

- [x] **Step 2: Run projection tests**
  - Command: `.\gradlew.bat :android-app:testDebugUnitTest --tests "*TrailMapProjectionTest"`
  - Expected red state before implementation: unresolved `TrailMapProjection`.

- [x] **Step 3: Implement minimal projection**
  - Normalize longitude/latitude bounds into `MapScreenPoint`.
  - Preserve `distanceAlongRouteKm` for progress/checkpoint overlays.
  - Handle vertical or horizontal routes without divide-by-zero.

- [x] **Step 4: Verify green**
  - Command: `.\gradlew.bat :android-app:testDebugUnitTest --tests "*TrailMapProjectionTest"`

### Task 2: Map Readiness State

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/map/TrailMapReadiness.kt`
- Test: `android-app/src/test/java/com/trailmate/app/core/map/TrailMapReadinessEngineTest.kt`

- [x] **Step 1: Write failing tests**
  - No AMap key returns local preview with a "高德底图待配置" caption.
  - GPS enabled and offline pack saved returns a field-ready caption.

- [x] **Step 2: Run readiness tests**
  - Command: `.\gradlew.bat :android-app:testDebugUnitTest --tests "*TrailMapReadinessEngineTest"`

- [x] **Step 3: Implement minimal readiness engine**
  - Represent provider, title, caption, layer chips, and action label.
  - Keep AMap disabled until an app key and SDK integration are intentionally configured.

- [x] **Step 4: Verify green**
  - Command: `.\gradlew.bat :android-app:testDebugUnitTest --tests "*TrailMapReadinessEngineTest"`

### Task 3: Compose Integration

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Modify: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [x] **Step 1: Add UI test assertions**
  - Route tab should show `本地路线预览`, `高德底图待配置`, and `GPX 折线`.

- [x] **Step 2: Run focused UI test for red state**
  - Command: `.\gradlew.bat :android-app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeTabShowsGpsAndTrackRecordingControls"`

- [x] **Step 3: Wire projection/readiness into `ReferenceRouteSurface`**
  - Pass the actual `ImportedRoute`.
  - Draw real route points when available.
  - Render a compact map readiness floating panel.

- [x] **Step 4: Verify UI test green**
  - Command: `.\gradlew.bat :android-app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest#routeTabShowsGpsAndTrackRecordingControls"`

### Task 4: OpenSpec And Verification

**Files:**
- Create: `openspec/changes/trailmate-map-readiness-ux/proposal.md`
- Create: `openspec/changes/trailmate-map-readiness-ux/design.md`
- Create: `openspec/changes/trailmate-map-readiness-ux/tasks.md`
- Create: `openspec/changes/trailmate-map-readiness-ux/specs/light-navigation/spec.md`

- [x] **Step 1: Document the behavior**
  - Capture local GPX preview, AMap deferred provider, and field-ready GPS/offline route states.

- [x] **Step 2: Run final verification**
  - Commands: `:android-app:testDebugUnitTest`, `:android-app:assembleDebug`, focused connected UI test.
