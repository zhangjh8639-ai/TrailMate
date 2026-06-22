# TrailMate Production UX Flow And Map Readiness Audit

Date: 2026-06-22
Scope: Main mobile flow, route cockpit, GPS, AMap online/offline map readiness, and outdoor production claims.

## Product Direction

TrailMate's production flow should behave like a field-preparation tool, not a marketing prototype:

1. Home: choose today's route and see the current route preparation state.
2. Route: import/replace GPX, inspect route, repair departure blockers, enter field navigation.
3. Gear: turn route recommendations into owned, packable equipment.
4. Data: review completed recordings and import historical GPX as capability background.
5. Profile: manage baseline body/outdoor profile and local data.

This matches the broad pattern in common outdoor apps: route discovery/import first, map/navigation as a focused route surface, offline maps as pre-trip preparation, and recorded activity/history as a later review loop.

## Market Baseline

- AllTrails documents downloading maps to the phone for offline use when data signal is unavailable.
- Gaia GPS positions downloaded maps as necessary for use outside cell service.
- Komoot separates route download from map download and recommends downloading before leaving, usually on stable Wi-Fi.
- AMap Android SDK supports offline map packages so map display can continue when the network is unavailable, and TrailMate should treat that as base-map context rather than GPX route geometry.

Reference URLs:

- https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use
- https://help.gaiagps.com/hc/en-us/articles/360047131513-Download-Maps-for-Offline-Use
- https://support.komoot.com/hc/en-us/articles/10356476920986-Download-routes-and-maps-for-offline-use
- https://lbs.amap.com/api/android-sdk/guide/create-map/offline-map

## Current TrailMate Fit

- Main bottom tabs are clear: `首页`, `路线`, `装备`, `数据`, `我的`.
- Home now works as a daily entry point rather than a dump of internal evidence.
- Route workspace owns GPX import and current route entry.
- Route detail separates `评估`, `路线`, `计划`, `装备`, with the route cockpit acting as the field-preparation surface.
- Gear has route checklist, owned inventory, and detail tabs.
- Data has recording review and historical GPX import, keeping AI/evidence context out of the first viewport.
- Expanded route diagnostics keep SDK, key, GPS, offline-map, and QA evidence available without making them primary user content.

## Offline Map Design Decision

TrailMate should not say that every route always requires an offline map. It should be risk-based:

- `推荐尝试`: offline base map is recommended preparation, but the user may start if route pack, GPS, and critical gear are ready.
- `谨慎尝试` / `不建议尝试`: target-region offline base map plus airplane-mode visible tile proof is required before the app presents the hike as ready.

The reason is product safety, not technical ceremony:

- GPX/local route packs save the planned line, distance, checkpoints, and imported geometry.
- Offline base maps preserve the surrounding context: roads, place names, water features, junctions, and retreat references.
- Count-only downloaded regions are not enough; the downloaded region must cover the active route.
- Coverage alone is still not enough; TrailMate needs network-disabled visible tile proof before treating it as field-ready evidence.

## Current Real-Device Evidence

Samsung SM-S9480 diagnostics on 2026-06-22 show:

- Package: `com.trailmate.app`
- Runtime SHA1: `DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88`
- AMap Key injected, SDK linked, privacy accepted.
- Download network validated.
- Precise location permission ready.
- Android system GPS ready.
- Location status `LOCATED`, accuracy `3 m`.

This resolves the earlier "real phone cannot enable location" blocker for this device. It does not resolve offline map readiness.

## Remaining Production Gaps

- ADB currently exposes only `emulator-5554`; the Samsung phone is not available to this workstation session for direct verification.
- Target-region AMap offline base-map download has not been proven on the phone.
- Route-region matching after download has not been captured.
- Airplane-mode visible AMap tile proof has not been captured.
- A 30-minute physical field walk is still required for background recording, screen lock, notification controls, weak-signal behavior, battery behavior, safety share, and crash/recording-loss checks.

## Next Product Tasks

1. On phone, open route diagnostics and use `打开高德离线底图管理`.
2. Download the city/province matching the active GPX route.
3. Return to TrailMate and confirm diagnostics no longer show `离线底图=未下载`.
4. Confirm the downloaded region covers the current route, not just any saved map package.
5. Enable airplane mode or disable network, reopen the same route, and visually confirm AMap base-map tiles.
6. Record the in-app offline tile proof only after tiles are visible while offline.
7. Run the physical field QA checklist before any production-readiness claim.

## Code Evidence

- Main shell and tab flow: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
- Home dashboard: `android-app/src/main/java/com/trailmate/app/feature/home/HomeDashboardScreen.kt`
- Route workspace: `android-app/src/main/java/com/trailmate/app/feature/route/RouteWorkspaceScreen.kt`
- Route detail and diagnostics: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Gear flow: `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt`
- Data flow: `android-app/src/main/java/com/trailmate/app/feature/data/DataScreen.kt`
- Offline readiness policy: `android-app/src/main/java/com/trailmate/app/core/model/DepartureReadinessEngine.kt`
- AMap launch diagnostics: `android-app/src/main/java/com/trailmate/app/core/map/AmapLaunchDiagnostics.kt`
