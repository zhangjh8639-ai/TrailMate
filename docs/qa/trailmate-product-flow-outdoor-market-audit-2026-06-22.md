# TrailMate Outdoor Product Flow Audit

Date: 2026-06-22

## Market Baseline

- AllTrails frames offline maps as downloadable trail/area data for viewing detailed map information and navigating with built-in GPS when the user has no data signal.
- Gaia GPS frames offline maps as a prerequisite for use out of cell service.
- Komoot separates planned route download and regional map download, and explicitly recommends downloading ahead of time on stable Wi-Fi.
- AMap Android 3D Map SDK states that offline maps satisfy no-network map viewing needs and the SDK prioritizes local offline map data when it exists.

Sources:

- https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use
- https://help.gaiagps.com/hc/en-us/articles/360047131513-Download-Maps-for-Offline-Use
- https://support.komoot.com/hc/en-us/articles/10356476920986-Download-routes-and-maps-for-offline-use
- https://lbs.amap.com/api/android-sdk/guide/create-map/offline-map

## TrailMate Flow Principle

TrailMate should keep the user's visible path simple:

1. Pick or import a route.
2. Decide whether the route fits the user.
3. Finish preparation: route pack, target-region offline base map, reliable GPS, critical gear.
4. Start light navigation and track recording.
5. Finish with review and exportable evidence.

Internal evidence, AI prompt inputs, raw SDK diagnostics, and historical-profile reasoning should stay behind expanded diagnostics or copied QA reports. They should inform AI and release gates, not dominate the production UI.

## Offline Base Map Rationale

TrailMate should explain offline base maps as a safety preparation, not a technical checkbox:

- GPX/local route packs preserve the planned line, checkpoints, and imported geometry.
- Target-region offline base maps preserve the surrounding map context: roads, place names, water features, junctions, and retreat references.
- Recommended routes may treat missing offline base maps as a preparation suggestion.
- Caution or not-recommended routes should require target-region offline base-map coverage plus network-disabled visible tile proof before TrailMate presents the hike as ready to start.
- The primary UI should keep this rationale concise; detailed proof requirements belong in expanded diagnostics and QA reports.

## Current Fit

- Route detail already separates `评估`, `路线`, `计划`, and `装备`.
- Route cockpit gates start actions behind readiness repairs instead of starting navigation blindly.
- Real GPS readiness is now proven on Samsung SM-S9480 with `LOCATED` and `3 m` accuracy in the copied diagnostic.
- AMap offline base-map readiness is still not proven: the phone report shows `离线底图=未下载`, so airplane-mode tile proof cannot be accepted yet.
- Route diagnostics now distinguish:
  - never downloaded offline base maps,
  - pending/unfinished AMap offline download tasks,
  - downloaded regions that do not cover the active route,
  - route-matched offline base maps that still need network-disabled tile proof.
- Required departure readiness copy now says the local route pack only saves the track, while offline base maps preserve roads, place names, water features, junctions, and retreat references in weak network conditions.

## Remaining Production Gaps

- A successful target-region offline base-map download must be captured on a physical phone.
- The route must show that the downloaded city/province/adcode covers the active GPX route.
- The same route must be reopened with network disabled and visible AMap base-map tiles before `我已断网并看到底图` is recorded.
- A 30-minute physical-device field walk is still required for foreground GPS recording, screen-lock behavior, weak-signal behavior, battery impact, and safety-share honesty.

## Design Implication

The app should not describe GPX import as an offline map. GPX is the route geometry; offline base maps are the surrounding roads, labels, terrain context, water features, and retreat references. The user-facing UI should say this only when it helps action, while copied diagnostics keep the full evidence trail for QA.
