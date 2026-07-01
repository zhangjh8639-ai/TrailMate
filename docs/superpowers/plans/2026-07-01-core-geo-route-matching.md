# Core Geo Route Matching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build pure Kotlin route matching and off-route evidence algorithms for TrailMate navigation.

**Architecture:** Add a framework-free `com.trailmate.app.core.geo` package inside the current Android app module. The package consumes `core.model` route geometry and returns algorithm outputs for future navigation UI/services. It must not access Android Location, MapLibre, storage, network, or Compose.

**Tech Stack:** Kotlin, JUnit 4, Android Gradle Plugin 9, JVM unit tests.

---

## File Structure

- Create: `app/src/main/java/com/trailmate/app/core/geo/GeoMath.kt` - WGS84 distance, bearing, interpolation, and local-plane helpers.
- Create: `app/src/main/java/com/trailmate/app/core/geo/RouteProjection.kt` - nearest route point and progress projection.
- Create: `app/src/main/java/com/trailmate/app/core/geo/RouteProgressCalculator.kt` - remaining route metrics and navigation anchors.
- Create: `app/src/main/java/com/trailmate/app/core/geo/OffRouteDetector.kt` - GPS accuracy filtering and suspected/confirmed off-route evidence.
- Create: `app/src/test/java/com/trailmate/app/core/geo/RouteProjectionTest.kt` - projection and overlap stability tests.
- Create: `app/src/test/java/com/trailmate/app/core/geo/RouteProgressCalculatorTest.kt` - distance/elevation/anchor tests.
- Create: `app/src/test/java/com/trailmate/app/core/geo/OffRouteDetectorTest.kt` - accuracy and deviation evidence tests.
- Create: `docs/core-geo-route-matching.md` - algorithm boundary and future integration notes.

## Task 1: Route Projection

- [ ] **Step 1: Write failing projection tests**

Create `RouteProjectionTest.kt` with tests for:

```kotlin
@Test
fun projectsCoordinateOntoNearestRouteSegment()

@Test
fun previousProgressPreventsJumpOnOverlappingRoute()
```

- [ ] **Step 2: Verify projection tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.geo.RouteProjectionTest" --console=plain
```

Expected: compilation fails because `RouteProjector` does not exist.

- [ ] **Step 3: Implement projection**

Create `GeoMath.kt` and `RouteProjection.kt` with `RouteProjector.project(geometry, coordinate, previousProgress)`.

- [ ] **Step 4: Verify projection tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 2: Progress Metrics

- [ ] **Step 1: Write failing progress tests**

Create `RouteProgressCalculatorTest.kt` with tests for:

```kotlin
@Test
fun calculatesCompletedAndRemainingDistanceFromProjection()

@Test
fun remainingElevationGainOnlyCountsFutureClimbs()

@Test
fun selectsNextWaypointAndNearestExitAfterCurrentProgress()
```

- [ ] **Step 2: Verify progress tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.geo.RouteProgressCalculatorTest" --console=plain
```

Expected: compilation fails because `RouteProgressCalculator` does not exist.

- [ ] **Step 3: Implement progress calculation**

Create `RouteProgressCalculator.kt` using `RouteProjection`, `RouteGeometry.cumulativeDistances`, route elevations, waypoints, and exit points.

- [ ] **Step 4: Verify progress tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 3: Off-Route Evidence

- [ ] **Step 1: Write failing off-route tests**

Create `OffRouteDetectorTest.kt` with tests for:

```kotlin
@Test
fun poorGpsAccuracyDoesNotTriggerOffRouteWarning()

@Test
fun singleAccurateFarPointIsSuspectedOffRoute()

@Test
fun sustainedAccurateDeviationBecomesConfirmedOffRoute()
```

- [ ] **Step 2: Verify off-route tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.geo.OffRouteDetectorTest" --console=plain
```

Expected: compilation fails because `OffRouteDetector` does not exist.

- [ ] **Step 3: Implement off-route detector**

Create `OffRouteDetector.kt` with `LocationSample`, `OffRouteThresholds`, `OffRouteStatus`, `OffRouteEvidence`, and `OffRouteDetector.evaluate()`.

- [ ] **Step 4: Verify off-route tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 4: Docs And Verification

- [ ] **Step 1: Add documentation**

Create `docs/core-geo-route-matching.md` describing inputs, outputs, thresholds, non-goals, and future integration points.

- [ ] **Step 2: Validate OpenSpec**

```powershell
openspec validate add-core-geo-route-matching
```

- [ ] **Step 3: Run full unit tests**

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

- [ ] **Step 4: Run debug build**

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

- [ ] **Step 5: Review scope**

```powershell
git diff --check
git status --short
```

Expected: no whitespace errors, no build artifacts, and no old `planner`, `equipment`, `community`, `marketplace`, or `pretrip_check` feature additions.
