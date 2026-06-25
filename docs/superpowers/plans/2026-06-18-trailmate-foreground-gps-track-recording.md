# TrailMate Foreground GPS Track Recording Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add real foreground GPS positioning and local track recording to the TrailMate Android prototype while preserving the MVP light-navigation boundary.

**Architecture:** Keep Android framework access in a small `core/location` adapter, keep route geometry and track recording as pure Kotlin model code, and wire Route screen controls through callbacks/state owned by `HomeScreen`. Imported GPX routes will retain a sampled route geometry so GPS fixes can be projected to distance-along-route and cross-track error before feeding the existing `LocationBackedHikeSessionEngine`.

**Tech Stack:** Kotlin, Android framework `LocationManager`, Jetpack Compose, existing `SharedPreferences` snapshot codec, JUnit, Android manifest permissions. No Play Services dependency is required for the first implementation.

---

## File Structure

- Modify `openspec/changes/trailmate-foreground-gps-track-recording/*`: product/spec draft for foreground GPS and recording.
- Modify `android-app/src/main/AndroidManifest.xml`: foreground location permissions.
- Modify `android-app/src/main/java/com/trailmate/app/core/model/TrailMateModels.kt`: route geometry and recorded track models.
- Create `android-app/src/main/java/com/trailmate/app/core/model/RouteGeometryEngine.kt`: projection from raw GPS fix to route-relative fix.
- Create `android-app/src/main/java/com/trailmate/app/core/model/TrackRecordingEngine.kt`: start/pause/resume/finish and append-point rules.
- Create `android-app/src/main/java/com/trailmate/app/core/location/AndroidLocationTracker.kt`: foreground Android location listener adapter.
- Modify `android-app/src/main/java/com/trailmate/app/core/gpx/TargetRouteGpxParser.kt`: retain simplified route points from GPX.
- Modify `android-app/src/main/java/com/trailmate/app/core/persistence/*`: persist imported route geometry and latest recorded track.
- Modify `android-app/src/main/java/com/trailmate/app/TrailMateAppSession.kt` and `TrailMateApp.kt`: store track recording changes.
- Modify `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`: own GPS permission/tracker state and pass it to route details.
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: show GPS state, recording controls, and recorded metrics in Chinese.
- Add/modify tests under `android-app/src/test/java/...` and `android-app/src/androidTest/java/...`.

## Task 1: OpenSpec And Plan

- [ ] **Step 1: Add OpenSpec proposal/design/tasks/spec**

Create:

```text
openspec/changes/trailmate-foreground-gps-track-recording/proposal.md
openspec/changes/trailmate-foreground-gps-track-recording/design.md
openspec/changes/trailmate-foreground-gps-track-recording/tasks.md
openspec/changes/trailmate-foreground-gps-track-recording/specs/light-navigation/spec.md
```

These files define foreground-only location, explicit permission, local track recording, and no background/turn-by-turn guarantee.

- [ ] **Step 2: Verify docs exist**

Run:

```powershell
Get-ChildItem openspec\changes\trailmate-foreground-gps-track-recording -Recurse -File
```

Expected: all four OpenSpec files are listed.

## Task 2: Route Geometry Projection With TDD

- [ ] **Step 1: Write failing parser and projection tests**

Add tests proving imported GPX keeps route points and a fix near the first segment projects to about 0.6 km along route with small cross-track error.

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:testDebugUnitTest --tests "*TargetRouteGpxParserTest" --tests "*RouteGeometryEngineTest"
```

Expected: FAIL because `ImportedRoute.routePoints` and `RouteGeometryEngine` do not exist.

- [ ] **Step 2: Implement minimal route geometry**

Add `RoutePoint`, `RouteGeometryFix`, and `RouteGeometryEngine.projectToRoute`. Update the parser to keep route points and compute distance/ascent from the same points. Downsample persisted route geometry to keep local snapshots compact.

- [ ] **Step 3: Verify green**

Run the same Gradle test command.

Expected: PASS.

## Task 3: Track Recording Model And Persistence With TDD

- [ ] **Step 1: Write failing recording tests**

Add tests for start, pause, ignored low-accuracy point, appended good point, distance accumulation, finish, and snapshot round-trip.

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:testDebugUnitTest --tests "*TrackRecordingEngineTest" --tests "*TrailMateSnapshotCodecTest"
```

Expected: FAIL because recording models and persistence fields do not exist.

- [ ] **Step 2: Implement minimal recording model**

Add `RecordedTrackPoint`, `TrackRecordingState`, `TrackRecordingStatus`, and `TrackRecordingEngine`. Persist the latest track in `TrailMateSnapshotCodec` and route it through `TrailMateSessionRepository`.

- [ ] **Step 3: Verify green**

Run the same Gradle test command.

Expected: PASS.

## Task 4: Android Foreground Location And UI

- [ ] **Step 1: Write failing UI smoke test**

Add a Compose smoke test that opens the Route tab and expects Chinese copy for GPS permission, recording controls, and local track summary.

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:connectedDebugAndroidTest --tests "*routeTabShowsGpsAndTrackRecordingControls"
```

Expected: FAIL because the UI copy does not exist.

- [ ] **Step 2: Add manifest permissions and Android location adapter**

Add `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`. Implement a small `AndroidLocationTracker` that requests GPS/network foreground updates only while the composable route screen is active and permission is granted.

- [ ] **Step 3: Wire Route screen**

Route details should show:

```text
GPS 定位
未授权 / 定位中 / 已定位 / 精度较低 / 请核对路线
开始记录 / 暂停记录 / 继续记录 / 结束记录
已记录 X km / X 个点
```

Location fixes should append to the local track only when recording is active and accuracy is usable. When route geometry exists, project fixes and feed `LocationBackedHikeSessionEngine`.

- [ ] **Step 4: Verify UI green**

Run the connected Android test above.

Expected: PASS.

## Task 5: Final Verification

- [ ] **Step 1: Run unit tests**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 2: Build debug APK**

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; .\gradlew.bat :android-app:assembleDebug
```

Expected: PASS.

- [ ] **Step 3: Android emulator sanity**

Install and open the app, grant location permission if prompted, start a route session, and confirm the Route tab shows GPS/recording controls. Use emulator location injection if available.

Expected: app stays responsive and the recording UI reflects state changes.

## Self-Review

Spec coverage:

- Real GPS positioning: Android permissions and `LocationManager` adapter in Task 4.
- Track recording: pure model, persistence, and UI in Tasks 3 and 4.
- Light-navigation boundary: OpenSpec and UI copy avoid full navigation promises.
- Route-aware progress: imported route geometry and projection in Task 2.

Placeholder scan:

- No "TBD" or deferred implementation steps remain.

Type consistency:

- `ImportedRoute.routePoints` feeds `RouteGeometryEngine`.
- `TrackRecordingState` is stored in `TrailMateSnapshot.latestTrackRecording`.
- Android location fixes are converted to model-layer fixes before updating session state.
