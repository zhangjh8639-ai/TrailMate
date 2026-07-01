# Core Geo Route Matching

This slice adds the local, JVM-testable route matching foundation for TrailMate navigation.

## What It Does

- Projects a WGS84 coordinate onto a route polyline.
- Returns nearest route coordinate, matched segment, distance from route, route progress, segment bearing, and bearing from the user to the nearest route point.
- Calculates completed distance, remaining distance, remaining elevation gain, next waypoint, and nearest exit by absolute route-distance proximity.
- Supports forward and reverse navigation metrics.
- Evaluates off-route evidence with GPS accuracy filtering, suspected deviation, and sustained confirmed deviation.
- Stabilizes overlapping/out-and-back route matching by accepting a previous progress hint, while keeping physical distance as the primary match signal.

## What It Does Not Do

- It does not subscribe to real GPS.
- It does not depend on Android `Location`.
- It does not render MapLibre or any map layer.
- It does not parse GPX/KML.
- It does not generate a safe route, auto reroute, or direct-return instruction.

## Key Classes

- `GeoMath`: internal WGS84 math helpers.
- `RouteProjector`: nearest-segment projection and progress matching.
- `RouteProgressCalculator`: forward/reverse route metrics and anchors.
- `OffRouteDetector`: accuracy-aware off-route evidence.

## Off-Route Defaults

`OffRouteThresholds` defaults are intentionally conservative:

- suspected distance: `70 m`;
- maximum acceptable GPS accuracy: `30 m`;
- confirmation duration: `45 s`;
- confirmation samples: `3`.

Future field testing should tune these values by route risk, terrain, tree cover, speed, battery mode, and route data quality.

Confirmation requires both enough elapsed time and enough consecutive accurate off-route samples. This avoids turning bursty GPS updates into an immediate confirmed偏航 warning.

## Integration Notes

Future navigation code should call this package from a use case or navigation engine, not directly from Compose UI. The UI should map `OffRouteStatus` to safe Chinese copy that reminds users to combine the signal with terrain judgment.

For real GPS integration, pass only already-validated location samples into `OffRouteDetector`. Poor-accuracy samples return `GpsUnreliable`, which should avoid over-warning users in canyons, forests, tunnels, or weak GNSS conditions.

`RouteProjection.bearingToRouteDegrees` is the bearing from the current GPS coordinate to the nearest known route point. UI can turn that into compass text such as "西北方向约 110m", but it must not present this as a guaranteed safe straight-line return path.
