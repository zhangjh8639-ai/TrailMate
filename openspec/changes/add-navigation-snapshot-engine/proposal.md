## Why

TrailMate needs a production-grade navigation core that can turn each GPS update into a safe, explainable navigation snapshot before the app wires in real location services. Existing route projection, progress, and off-route logic are useful pieces, but there is no single engine that composes them into the state the Navigation tab and future foreground service will consume.

## What Changes

- Add a pure navigation snapshot engine that accepts route geometry, session context, a location sample, battery level, prior off-route evidence, and configurable thresholds.
- Generate `NavigationSnapshot` values with progress, remaining distance, remaining elevation, next waypoint, nearest exit, GPS accuracy, battery, deviation, and nearest route point guidance.
- Map off-route evidence into the conservative navigation state contract: on-route, GPS unreliable, suspected off-route, confirmed off-route, and returning/on-track boundaries.
- Preserve safety wording boundaries in data: nearest route point guidance is direction and distance only, not a safe straight-line route.
- Do not start GPS, request permissions, create Android services, render maps, or record tracks in this change.

## Capabilities

### New Capabilities
- `navigation-snapshot-engine`: Computes a single navigation snapshot from route geometry and a location sample without Android service side effects.

### Modified Capabilities
- None.

## Impact

- Adds core navigation/geo code and unit tests under the Android app module.
- Uses existing `RouteProjection`, `RouteProgressCalculator`, `OffRouteDetector`, and navigation model types.
- No dependency, manifest, permission, background service, network, database, or UI changes are expected.
