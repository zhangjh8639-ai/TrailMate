# Change: TrailMate Route Cockpit

## Why

TrailMate already has the core pieces for route assessment, AMap rendering, real GPS location, and track recording. The current route experience still reads like a prototype because too much supporting information appears at the same level: map setup, diagnostics, readiness, GPS panels, route assessment, and tracking controls compete for attention.

The production app needs a route-first cockpit for the "路线 / 轻导航" tab. The first viewport should answer where the hiker is, what comes next, whether the route is still reasonable to continue, and which action is primary right now.

## What Changes

- Redesign the light-navigation route tab into a map-first route cockpit.
- Promote current location, next checkpoint, route progress, and session action into the first viewport.
- Replace scattered diagnostic/readiness panels with a compact field-readiness strip and expandable details.
- Keep user profile, historical evidence, and AI evidence bundles out of the real user-facing route flow.
- Preserve the existing boundaries: light navigation only, no turn-by-turn guarantee, no automatic reroute, and no emergency promise.

## Impact

- `RouteDetailScreen` route tab composition.
- Route cockpit presentation state and tests.
- Existing AMap, GPX, GPS, track recording, route assessment, readiness, and gear engines.
- Compose UI smoke coverage for the route cockpit first viewport.
