# Design: PMTiles Route Coverage

Both server and Android already represent PMTiles and route extents with rectangular longitude/latitude bounds. This change adds a `contains(...)` check to those bounds models and uses it wherever route-pack coverage determines safety readiness.

Server:

- `OfflineBasemapBounds.contains(other)` returns true only when the pack min/max coordinates fully enclose the requested route bounds.
- `OfflineBasemapService.listPmTilesCatalog(...)` filters packs with `contains(routeBounds)` instead of `intersects(routeBounds)`.

Android:

- `PmTilesLatLngBounds.contains(other)` mirrors the server containment logic.
- `TrailMateOfflineBasemapCatalogSelectionPolicy` selects only MVT catalog items whose bounds contain the route bounds.
- `PmTilesOfflineBasemapImportPolicy` rejects PMTiles archive bounds that only partially overlap the target route bounds.
- `PmTilesOfflineBasemapManifestReader` marks `coversTargetBounds` only when archive bounds contain the target route bounds.

This keeps the implementation small and deterministic while removing the unsafe "partial overlap is ready" behavior.
