# Core Navigation Domain Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first pure Kotlin TrailMate domain layer for routes, navigation sessions, safety copy, records, feedback, and privacy defaults.

**Architecture:** Keep this slice inside the existing Android app module under `com.trailmate.app.core.model`. The code must be framework-free and JVM-testable. It must not implement GPS, GPX/KML parsing, geometry projection, MapLibre, Room, DataStore, backend API, or UI screens.

**Tech Stack:** Kotlin, JUnit 4, Android Gradle Plugin 9, JVM unit tests.

---

## File Structure

- Create: `app/src/main/java/com/trailmate/app/core/model/Units.kt` - small unit/value classes.
- Create: `app/src/main/java/com/trailmate/app/core/model/RouteModels.kt` - route metadata and route geometry models.
- Create: `app/src/main/java/com/trailmate/app/core/model/NavigationModels.kt` - navigation state, event, session, reducer, snapshot, guidance models.
- Create: `app/src/main/java/com/trailmate/app/core/model/SafetyRecordModels.kt` - emergency card, track point, record, feedback, safety copy.
- Create: `app/src/test/java/com/trailmate/app/core/model/RouteModelsTest.kt` - route/privacy tests.
- Create: `app/src/test/java/com/trailmate/app/core/model/NavigationModelsTest.kt` - reducer/session tests.
- Create: `app/src/test/java/com/trailmate/app/core/model/SafetyRecordModelsTest.kt` - safety/record/scope tests.
- Create: `docs/core-navigation-domain.md` - model boundary and future PR ownership.

## Task 1: Route Models

- [ ] **Step 1: Write failing route tests**

Create `app/src/test/java/com/trailmate/app/core/model/RouteModelsTest.kt` with tests that assert:

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.Duration

class RouteModelsTest {
    @Test
    fun importedRouteDefaultsToPrivateTrackOnlyVisibility() {
        val route = TrailRoute.imported(
            id = RouteId("r-gpx"),
            name = "龙井环线导入轨迹",
            region = "杭州 · 西湖群山",
            geometry = sampleGeometry(),
            importedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(PrivacyVisibility.Private, route.visibility)
        assertEquals(RouteSourceType.ImportedGpx, route.sourceType)
        assertEquals(RouteOfflineStatus.TrackOnly, route.offlineStatus)
    }

    @Test
    fun routeGeometryKeepsNavigationInputsSeparateFromUiCopy() {
        val geometry = sampleGeometry()

        assertEquals(2, geometry.coordinates.size)
        assertEquals(Distance.meters(1250.0), geometry.cumulativeDistances.last())
        assertTrue(geometry.hasElevation)
        assertEquals(WaypointType.Water, geometry.waypoints.single().type)
        assertEquals(RiskPointType.Slippery, geometry.riskPoints.single().type)
        assertEquals(ExitPointType.RoadAccess, geometry.exitPoints.single().type)
    }

    private fun sampleGeometry(): RouteGeometry =
        RouteGeometry(
            coordinates = listOf(
                GeoCoordinate(latitude = 30.245, longitude = 120.116, elevation = Elevation.meters(72.0)),
                GeoCoordinate(latitude = 30.248, longitude = 120.121, elevation = Elevation.meters(118.0)),
            ),
            cumulativeDistances = listOf(Distance.ZERO, Distance.meters(1250.0)),
            waypoints = listOf(RouteWaypoint("wp-water", "补水点", WaypointType.Water, Distance.meters(800.0))),
            riskPoints = listOf(RouteRiskPoint("risk-slip", "雨后湿滑", RiskPointType.Slippery, Distance.meters(420.0))),
            exitPoints = listOf(RouteExitPoint("exit-road", "最近公路", ExitPointType.RoadAccess, Distance.meters(1100.0))),
        )
}
```

- [ ] **Step 2: Verify route tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteModelsTest" --console=plain
```

Expected: compilation fails because domain classes do not exist.

- [ ] **Step 3: Implement route models**

Create `Units.kt` and `RouteModels.kt` with only the types required by the tests plus fields from OpenSpec.

- [ ] **Step 4: Verify route tests pass**

Run the same targeted Gradle command. Expected: BUILD SUCCESSFUL.

## Task 2: Navigation Models

- [ ] **Step 1: Write failing navigation tests**

Create `NavigationModelsTest.kt` with tests for private session default, reducer start, reducer refusing confirmed off-route without suspected state, returning on track from suspected/confirmed, and ended terminal behavior.

- [ ] **Step 2: Verify navigation tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.model.NavigationModelsTest" --console=plain
```

Expected: compilation fails because navigation classes do not exist.

- [ ] **Step 3: Implement navigation models**

Create `NavigationModels.kt` with `NavigationState`, `NavigationEvent`, `NavigationSession`, `NavigationStateReducer`, `NavigationSnapshot`, and `NearestRoutePointGuidance`.

- [ ] **Step 4: Verify navigation tests pass**

Run the same targeted command. Expected: BUILD SUCCESSFUL.

## Task 3: Safety, Records, Feedback

- [ ] **Step 1: Write failing safety/record tests**

Create `SafetyRecordModelsTest.kt` with tests for emergency helper copy avoiding rescue promises, confirmed off-route helper copy avoiding unsafe direct-return promises, route record default privacy, feedback categories, and absence of deprecated model/package names in `app/src/main/java`.

- [ ] **Step 2: Verify tests fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.core.model.SafetyRecordModelsTest" --console=plain
```

Expected: compilation fails because safety and record classes do not exist.

- [ ] **Step 3: Implement safety/record models**

Create `SafetyRecordModels.kt` with `EmergencyCard`, `SafetyCopy`, `TrackPoint`, `RouteRecord`, `RouteFeedback`, and `FeedbackCategory`.

- [ ] **Step 4: Verify tests pass**

Run the same targeted command. Expected: BUILD SUCCESSFUL.

## Task 4: Docs And Full Verification

- [ ] **Step 1: Create docs**

Create `docs/core-navigation-domain.md` explaining this slice, future owners, and non-goals.

- [ ] **Step 2: Run full tests**

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

- [ ] **Step 3: Run build**

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

- [ ] **Step 4: Run OpenSpec validation**

```powershell
openspec validate add-core-navigation-domain
```

- [ ] **Step 5: Review staged scope**

Run:

```powershell
git diff --check
git status --short
```

Expected: no whitespace errors, no build artifacts, and no deprecated feature packages.
