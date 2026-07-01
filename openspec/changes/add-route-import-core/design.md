## Context

The import core sits between Android file selection/UI and route navigation models. It receives a filename plus text content from a future repository/use case, validates the format, parses coordinates, and returns a route import summary that can drive the route tab's compact import result panel.

## Goals

- Parse GPX and KML into WGS84 route geometry.
- Preserve imported file boundaries: the file contains route track and waypoints, not commercial basemap tiles.
- Return structured status and quality warnings instead of localized UI paragraphs.
- Keep imported routes private by default by reusing `TrailRoute.imported()`.
- Keep parsing framework-light and JVM-testable.
- Harden XML parsing against DTD and external entity resolution.

## Non-Goals

- No Android file picker.
- No Compose UI.
- No Room/DataStore persistence.
- No route asset list UI.
- No MapLibre rendering.
- No PMTiles/MBTiles or offline basemap packaging.
- No arbitrary route planning or auto-rerouting.
- No large-file background worker in this slice.

## Design

### Public API

`RouteImportParser.parse(fileName, content)` returns `RouteImportResult`.

`RouteImportResult` contains:

- `fileName`;
- `format`;
- `status`;
- `routeName`;
- `geometry`;
- `trackPointCount`;
- `waypointCount`;
- `hasElevation`;
- `warnings`;
- `sourceType`.

Successful results can call `toImportedRoute(id, region, importedAt)` to create a `TrailRoute` with `PrivacyVisibility.Private` and `RouteOfflineStatus.TrackOnly`.

### GPX Parsing

For GPX, the parser prefers track points from `trkpt`. If fewer than two usable track points exist, it falls back to `rtept`. It parses `wpt` as route waypoints by projecting each waypoint onto the route and using the nearest route progress.

### KML Parsing

For KML, the parser reads the first `LineString` coordinate sequence as the navigable route. It reads `Point` placemarks as waypoints and projects them onto the route.

### Geometry Building

Coordinates are validated through `GeoCoordinate`. Cumulative distance uses a small public `GeoDistance.between()` facade that delegates to the existing geo math. Elevation gain is calculated by `RouteGeometry`.

### Quality Warnings

Warnings are additive:

- `MissingElevation`: no route coordinate includes elevation.
- `SparseTrack`: route has too few track points.
- `LargePointGap`: a segment gap is too large for stable navigation.
- `UnsupportedFormat`: extension/root cannot be handled.
- `MissingTrackGeometry`: no usable route line exists.

The route tab can map these warnings to Chinese UI copy. This core layer avoids deciding button states or building an out-of-flow pre-trip check.

### XML Safety

The parser rejects DTD/doctype input and disables external entity resolution. XML parser hardening is fail-closed: unsupported security features prevent parsing rather than silently falling back to a weaker parser configuration.

## Risks

- DOM parsing is not ideal for very large files; future production import should move parsing to a background worker and can stream large files.
- KML in the wild can contain many geometry variants; this slice supports the common `LineString` and `Point` shapes first.
- Imported waypoint projection depends on route density; very sparse files already receive quality warnings.
