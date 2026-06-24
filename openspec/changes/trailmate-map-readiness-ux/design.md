# Design: TrailMate Map Readiness UX

## Context

The app already supports foreground GPS and track recording. The map surface now needs to become route-faithful before a third-party SDK is introduced.

## Decisions

### Local Preview First

TrailMate will render the actual imported GPX geometry with a pure Kotlin projection engine and Compose Canvas. This keeps the app functional without network, SDK keys, or map-provider lifecycle complexity.

### AMap SDK Provider

The app links the AMap Android SDK and uses it only when readiness gates pass:

- Android platform Key is configured through Gradle property or environment variable.
- Privacy consent is accepted and persisted locally.
- AMap `MapView` class is available on the app classpath.

The app calls AMap privacy APIs before constructing `MapView`, then owns `MapView` lifecycle through the route screen. If any gate is missing, the app stays on local GPX Canvas preview.

These project-owned production inputs are still required before claiming real online map operation:

- Debug/release SHA1 and package name bound in AMap console.
- A real project Key supplied outside source control.
- Device QA with the real Key, network, and privacy policy copy.

### Explicit Readiness

The route screen should tell users what map mode they are in:

- `本地路线预览`: GPX geometry and checkpoints are drawn locally.
- `定位与记录`: offline route pack and foreground GPS are both active.
- `高德地图`: future provider state after SDK/key configuration.

### Checkpoint Details

Map checkpoint details should behave like a lightweight field assistant, not a static route note. When a user opens a supplement, rest, or risk hint, the panel combines the checkpoint plan with current track progress and route gear recommendations:

- Remaining or absolute checkpoint distance.
- Expected arrival time from the plan.
- Readiness status such as water/supply coverage or missing risk gear.
- A short action prompt and the original checkpoint note.

## Non-Goals

- No committed map key.
- No background location, voice navigation, turn-by-turn routing, rerouting, or rescue promise.
