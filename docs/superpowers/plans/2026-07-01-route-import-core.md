# Route Import Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build pure Kotlin GPX/KML import parsing that turns route files into navigation-ready summaries and geometry.

**Architecture:** Add `com.trailmate.app.core.routeimport` inside the Android app module. The package parses GPX/KML text, builds `RouteGeometry`, reports quality warnings, and can create private imported `TrailRoute` objects. It must not access Android file pickers, Compose UI, MapLibre, persistence, network, or GPS.

**Tech Stack:** Kotlin, JUnit 4, Java XML DOM parser with external entities disabled, existing `core.model` and `core.geo`.

---

## File Structure

- Create: `app/src/main/java/com/trailmate/app/core/routeimport/RouteImportModels.kt` - result, status, format, warnings, options.
- Create: `app/src/main/java/com/trailmate/app/core/routeimport/RouteImportParser.kt` - public parser entry point and XML hardening.
- Create: `app/src/main/java/com/trailmate/app/core/routeimport/GpxRouteImportParser.kt` - GPX-specific parsing.
- Create: `app/src/main/java/com/trailmate/app/core/routeimport/KmlRouteImportParser.kt` - KML-specific parsing.
- Create: `app/src/main/java/com/trailmate/app/core/routeimport/RouteGeometryBuilder.kt` - cumulative distance, waypoint projection, validation, quality warnings.
- Modify: `app/src/main/java/com/trailmate/app/core/geo/GeoMath.kt` - expose public distance facade without making the internal helper public.
- Create: `app/src/test/java/com/trailmate/app/core/routeimport/RouteImportParserTest.kt` - GPX/KML happy paths.
- Create: `app/src/test/java/com/trailmate/app/core/routeimport/RouteImportValidationTest.kt` - quality warnings and rejection paths.
- Create: `docs/route-import-core.md` - import boundary and future integration notes.

## Task 1: GPX Parsing

- [ ] **Step 1: Write failing GPX tests**

Create `RouteImportParserTest.kt` with tests:

```kotlin
@Test
fun parsesGpxTrackIntoNavigationGeometry()

@Test
fun parsesGpxRoutePointsWhenTrackPointsAreAbsent()
```

- [ ] **Step 2: Verify GPX tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.routeimport.RouteImportParserTest" --console=plain
```

Expected: compilation fails because route import classes do not exist.

- [ ] **Step 3: Implement GPX parsing**

Create import models, XML parser shell, GPX parser, geometry builder, and `GeoDistance.between()`.

- [ ] **Step 4: Verify GPX tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 2: KML Parsing

- [ ] **Step 1: Add failing KML tests**

Extend `RouteImportParserTest.kt` with tests:

```kotlin
@Test
fun parsesKmlLineStringIntoNavigationGeometry()

@Test
fun parsesKmlPointPlacemarksAsWaypoints()
```

- [ ] **Step 2: Verify KML tests fail**

Run the same targeted Gradle command. Expected: KML tests fail because KML parser is not implemented.

- [ ] **Step 3: Implement KML parsing**

Create `KmlRouteImportParser.kt` and wire format dispatch.

- [ ] **Step 4: Verify KML tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 3: Quality, Validation, And Privacy

- [ ] **Step 1: Write failing validation tests**

Create `RouteImportValidationTest.kt` with tests:

```kotlin
@Test
fun missingElevationAddsQualityWarning()

@Test
fun sparseTrackAddsQualityWarning()

@Test
fun largePointGapAddsQualityWarning()

@Test
fun unsupportedExtensionReturnsRejectedResult()

@Test
fun missingTrackGeometryReturnsRejectedResult()

@Test
fun parsedImportCreatesPrivateTrackOnlyRoute()
```

- [ ] **Step 2: Verify validation tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.routeimport.RouteImportValidationTest" --console=plain
```

Expected: tests fail because validation behavior is incomplete.

- [ ] **Step 3: Implement validation and route conversion**

Add warning calculation, rejected statuses, and `RouteImportResult.toImportedRoute()`.

- [ ] **Step 4: Verify validation tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 4: Docs And Verification

- [ ] **Step 1: Add documentation**

Create `docs/route-import-core.md` describing parser inputs, outputs, warnings, non-goals, and future UI/repository integration points.

- [ ] **Step 2: Validate OpenSpec**

```powershell
openspec validate add-route-import-core
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
