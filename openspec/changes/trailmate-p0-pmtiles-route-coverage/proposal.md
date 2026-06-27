# TrailMate P0 PMTiles Route Coverage

## Why

TrailMate's offline basemap is safety-critical during weak-signal hiking. A PMTiles pack that only intersects a route is not enough: the user can still walk into route segments with no offline map context. Current Android and server-side PMTiles selection paths use intersection checks in several places, so a partially overlapping pack can be selected, imported, or reported ready.

## What Changes

- Server PMTiles catalog filtering requires a pack to fully contain the requested route bounds.
- Android catalog selection requires a pack to fully contain the route bounds before remote import.
- Android local PMTiles import and readiness checks require archive bounds to fully contain route bounds.

## Non-Goals

- Do not add geometric line-buffer coverage checks in this change; use the existing route bounding box.
- Do not add multi-pack tiling or stitching.
- Do not change the PMTiles catalog API schema.
