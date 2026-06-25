# TrailMate Production Map UX Research

Date: 2026-06-18

## Benchmarks

- AllTrails frames the outdoor loop around trail discovery, activity recording, offline exploration, and custom route creation. TrailMate should keep the route-first structure but make recording/offline readiness visible before the user starts.
- Komoot emphasizes precision planning before navigation: elevation profiles, custom waypoints, surface breakdown, route detail, offline maps, voice guidance, and rerouting. TrailMate should preserve its personal-risk angle while treating map readiness, GPS status, checkpoints, and route geometry as first-class navigation controls.
- AllTrails, komoot, and Gaia GPS all treat offline map preparation as a field-safety baseline, not a cosmetic enhancement. The common pattern is: plan the route, download the relevant map area, verify GPS/navigation behavior, then record or navigate in the field.
- Android core app quality guidance reinforces the production baseline: preserve state, support predictable navigation, keep technical quality high, and avoid UI states that feel like demos.

## AMap SDK Notes

- AMap Android Map SDK includes map display, map interaction, overlays, POI/geocoding, and offline maps. This maps well to TrailMate's route polyline, checkpoints, offline pack, and future supplement/rest-stop layers.
- AMap documents a privacy-compliance requirement before calling SDK APIs. Map SDK 8.1.0+ requires privacy calls before SDK usage; otherwise white screens or SDK exceptions can occur.
- AMap Android keys are bound to Android platform SDK, SHA1 values, and package name. TrailMate should not commit a placeholder Key or add SDK initialization until the package/signing/key path is ready.
- Maven metadata for `com.amap.api:3dmap-location-search` lists `11.2.000_loc11.2.000_sea9.8.0` as the current release as of 2026-06-18. TrailMate pins this release instead of using `latest.integration`.

## Current Product Decision

This iteration links the AMap SDK but still avoids committed secrets. It makes the route screen production-ready in guarded steps:

1. Render the actual imported GPX geometry instead of a hard-coded sample line.
2. Surface a map readiness state that says whether the app is using local GPX preview or a future AMap-backed production map.
3. Keep AMap behind three explicit gates: non-empty project Key, linked SDK/provider, and privacy consent accepted before SDK calls.
4. Persist AMap privacy consent locally and show an authorization action from light navigation only when an AMap Key is configured and consent is missing.
5. Construct AMap `MapView` only after the gates pass; otherwise keep the local Canvas route preview.
6. Project hike-plan checkpoints onto route geometry so supplement, rest, and risk guidance appears as a map layer, not only as a separate plan list.
7. Let users open checkpoint details from map hints, with ETA, distance, suggested action, and a current-focus action.
8. Place a scan-first `现场状态` summary directly after the route map so hikers can see GPS reliability, track recording, base map readiness, and notification support before reading detailed panels.
9. Show a non-secret `高德上线检查` panel in light navigation so Package/SHA1, SDK, privacy authorization, online map readiness, package name, and GPS checks are visible without exposing the actual AMap Key.
10. Include `离线底图` in the same launch diagnostics, because the AMap 3D Map SDK supports offline map data and the app must show whether that entry is ready before presenting it as a field tool.
11. Keep `路线包` and `离线底图` separate. The saved GPX route lets TrailMate draw the planned path offline, but it cannot provide surrounding roads, place names, terrain context, water features, junctions, or retreat options. Target-region offline base maps are required before high-risk route readiness can be considered field-ready.
12. Keep location-repair actions in the route cockpit. `授权定位`, `打开系统定位`, and `等待定位稳定` must execute permission/settings/calibration work directly instead of only switching to fullscreen navigation.

## Next SDK Slice

- Test the AMap route screen with a real project Key supplied through `TRAILMATE_AMAP_API_KEY`, with debug/release SHA1 and `com.trailmate.app` bound in AMap console.
- Verify online tiles, route polyline, start/end markers, and location layer on an emulator or physical device with network.
- Open the AMap offline map manager only after Android Key, SDK linkage, and privacy consent are ready; before that, keep `离线底图` as a diagnostic state instead of a fake action.
- Verify AMap `MyLocationStyle` blue-dot behavior against the existing field status summary so the UI does not claim GPS/map readiness before the map layer is actually usable.
- Extend checkpoint details into richer AMap overlays after base map QA: supply/rest candidate POIs, distance-to-next, and trail-condition notes.
- Add a release checklist for SHA1, package name, Key ownership, and privacy policy copy.

## Sources

- AMap Android Map SDK overview: https://lbs.amap.com/api/android-sdk/summary
- AMap Android SDK Key guide: https://lbs.amap.com/api/android-sdk/guide/create-project/get-key
- AMap Android Studio integration: https://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project
- AMap show map guide: https://lbs.amap.com/api/android-sdk/guide/create-map/show-map
- AMap SDK privacy compliance reference: https://lbs.amap.com/compliance-center/check-and-reference/sdkhgsy
- Maven metadata for AMap SDK bundle: https://repo1.maven.org/maven2/com/amap/api/3dmap-location-search/maven-metadata.xml
- Android core app quality guidelines: https://developer.android.com/docs/quality-guidelines/core-app-quality
- AllTrails product site: https://www.alltrails.com/
- Komoot features: https://www.komoot.com/features
