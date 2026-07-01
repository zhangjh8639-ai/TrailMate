# Route Import Core

This slice adds the route import parsing foundation for TrailMate.

## What It Does

- Parses GPX text into route geometry using `trkpt`, with `rtept` fallback when track geometry is absent or insufficient.
- Parses KML text into route geometry using the first `LineString`.
- Parses GPX `wpt` and KML `Point` placemarks as route waypoints.
- Calculates cumulative route distance, total distance, and elevation gain.
- Ignores non-finite elevation values such as `NaN` or `Infinity`.
- Reports route-tab summary fields: file name, format, route name, track point count, waypoint count, elevation availability, and quality warnings.
- Converts a parsed import result into a private `TrailRoute` with `TrackOnly` offline status.
- Disables external XML entities and DTD loading in the parser setup.
- Rejects unsafe doctype/external-entity input as `InvalidXml`.

## What It Does Not Do

- It does not open Android file pickers.
- It does not save imported routes to Room/DataStore.
- It does not render MapLibre maps.
- It does not create the route tab import UI.
- It does not parse every KML geometry variant.
- It does not package offline basemaps.
- It does not plan or reroute between arbitrary points.

## Key Classes

- `RouteImportParser`: public entry point for GPX/KML text.
- `RouteImportResult`: structured result for route-tab import summary.
- `RouteImportWarning`: additive quality warnings for UI copy.
- `RouteGeometryBuilder`: cumulative distance, waypoint projection, and quality checks.

## Quality Warnings

- `MissingElevation`: no route coordinate contains elevation.
- `SparseTrack`: point count is below the reliable-navigation baseline.
- `LargePointGap`: adjacent route points are too far apart for stable guidance.
- `UnsupportedFormat`: filename extension is not GPX or KML.
- `MissingTrackGeometry`: fewer than two usable route coordinates.
- `InvalidXml`: XML parsing failed or rejected unsafe XML.

The route tab should map these warnings into concise Chinese copy. It should not turn them into a complex pre-trip checklist or block all navigation with a rigid gate.

## Integration Notes

Future UI should show the required import explanation:

```text
导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。
```

Future repository/use-case work should handle large files off the main thread and persist only after explicit user action such as "保存到路线". Parsed routes remain private by default through `TrailRoute.imported()`.
