## Why

Route assets can now be inspected in detail, but there is still no product bridge from a chosen route into the Navigation tab. A production hiking app needs a clear pre-navigation handoff so users can confirm which track will be used before active GPS navigation, foreground service tracking, and offline map work are added.

## What Changes

- Add a Navigation tab ready state that can receive a selected route asset from route detail.
- Add a read-only "待开始轨迹导航" surface showing the selected route name, source, offline/track status, distance, elevation gain, estimated duration, difficulty, confidence, and risk tags.
- Add route-detail action copy that hands the selected asset to the Navigation tab without starting GPS, tracking, or a navigation session.
- Preserve imported-route boundaries: local private, track-only, unverified, and no commercial/full offline basemap.
- Keep the Navigation tab idle fallback for when no route has been selected.
- Do not add real GPS location, active navigation, foreground service, MapLibre map rendering, route recording, off-route alerts, emergency card, server sync, or commercial map basemap download behavior in this change.

## Capabilities

### New Capabilities

- `navigation-route-handoff`: Route asset selection handoff into the Navigation tab's pre-navigation ready state.

### Modified Capabilities

- None.

## Impact

- Android app shell state and Navigation tab UI.
- Route detail state/actions to select a route for navigation readiness.
- Unit tests for handoff state, imported-route boundary copy, fake-navigation exclusion, and deprecated-surface exclusion.
- No new Android runtime permissions, location requests, foreground service, network calls, database schema, map tile source, or server dependency.
