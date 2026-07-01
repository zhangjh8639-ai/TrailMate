## Context

`core.geo` is the local algorithm layer between route geometry and navigation UI/state. It receives route geometry from `core.model` and future location samples from a location provider abstraction. It must stay framework-free so it can run in JVM tests and inside future background tracking services.

## Goals

- Match a WGS84 coordinate to the nearest point on a route polyline.
- Calculate route progress without relying on map SDK snapping.
- Calculate remaining distance and remaining positive elevation gain.
- Select the next waypoint and nearest exit point by route distance.
- Detect off-route evidence with GPS accuracy filtering and duration-based confirmation.
- Keep route matching stable on overlapping/out-and-back lines by considering previous progress.

## Non-Goals

- No Android `Location` dependency.
- No MapLibre rendering.
- No GPX/KML parsing.
- No live GPS subscription or foreground service.
- No automatic reroute and no "safe return path" generation.
- No UI copy beyond enum/status names that upper layers can map to safe Chinese copy.

## Design

### Route Projection

`RouteProjector.project()` iterates through route segments, converts segment endpoints and the current coordinate into a local tangent plane around the segment start, clamps the projection ratio to `[0, 1]`, and calculates:

- nearest route coordinate;
- segment index;
- distance from route;
- progress from route start;
- bearing of the matched segment;
- bearing from the current coordinate to the nearest route point.

When `previousProgress` is provided, the projector still ranks candidates by physical distance first. It only uses progress closeness to break near-ties within a narrow tolerance, which helps overlapping or out-and-back routes without keeping the user stuck to an old segment when another segment is clearly closer.

### Progress Metrics

`RouteProgressCalculator.calculate()` uses `RouteProjection` and `RouteGeometry` to produce completed distance, remaining distance, remaining elevation gain, next waypoint, and nearest exit.

Remaining elevation is calculated from the matched segment forward. If the route has no elevation values, remaining elevation is zero. This is intentionally conservative; later GPS/barometer smoothing can refine it.

The next waypoint follows travel direction. The nearest exit is selected by absolute route-distance difference from the current progress, because a retreat point slightly behind the user can be safer and more relevant than a much farther point ahead. This package only identifies the closest known exit point; it does not decide whether the user should move forward or return.

### Off-Route Evidence

`OffRouteDetector.evaluate()` uses:

- `LocationSample.coordinate`;
- `LocationSample.accuracy`;
- `LocationSample.recordedAt`;
- projection result;
- optional previous off-route evidence.

Baseline thresholds are configurable through `OffRouteThresholds`:

- suspected when distance from route is greater than the threshold and GPS accuracy is acceptable;
- confirmed when suspected off-route continues for the configured duration and enough consecutive samples;
- unreliable when GPS accuracy is worse than allowed, so the app does not over-warn from weak fixes.

The detector returns evidence, not instructions. UI and safety copy must still tell users to combine the signal with terrain judgment.

## Risks

- Local tangent projection is suitable for hiking-scale route segments, not country-scale geodesic analysis. Route import should keep segments reasonably dense.
- Elevation gain from sparse route points is only as good as the imported/platform geometry.
- Confirmation thresholds are defaults and must later be tuned with field traces and battery constraints.
