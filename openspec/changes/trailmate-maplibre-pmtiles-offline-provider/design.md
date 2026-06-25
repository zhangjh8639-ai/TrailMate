# Design: MapLibre PMTiles Offline Provider

## Product Principles

- The route screen must be honest about map capability. A saved GPX route is not the same thing as an offline basemap.
- MapLibre/PMTiles is the offline basemap direction, while the local Canvas route remains the safety fallback.
- Provider state must be scan-friendly in Chinese: users should know whether they are seeing `本地路线预览`, `离线底图`, or an optional online provider.

## Technical Direction

TrailMate will model map providers separately from map surfaces:

- `LOCAL_GPX_PREVIEW`: imported route geometry, checkpoints, user location, and recorded track rendered locally.
- `MAPLIBRE_PMTILES`: MapLibre Native rendering a local PMTiles package built from OSM-derived data.
- `AMAP_SDK`: optional online/domestic provider when configured.

The app chooses `MAPLIBRE_PMTILES` only when:

- the imported route has drawable geometry;
- MapLibre runtime is available in the app build;
- a local PMTiles basemap pack exists for the target route region.

The server may expose a PMTiles catalog endpoint that returns pack metadata for
route bounds. That catalog helps Android choose a suitable pack, but it is not
readiness proof by itself; Android still validates the downloaded/imported
PMTiles file locally before switching to `MAPLIBRE_PMTILES`.

The route screen import action should prefer the server catalog when a backend
base URL is configured. If the catalog lookup, selected download, or local
archive validation fails, the same action falls back to Android's local document
picker instead of leaving the user at a dead end.

If any of those gates are missing, TrailMate falls back to `LOCAL_GPX_PREVIEW` and keeps route assessment, checkpoint guidance, GPS, and track recording usable.

## Data And Licensing Notes

- OSM-derived data requires visible attribution and ODbL-aware data handling.
- PMTiles files are managed as app/user data, not committed source assets.
- The server catalog stores metadata and file URLs only; PMTiles binaries should
  live in object storage, CDN, or explicit static hosting.
- China mainland production use still requires separate review for map data compliance, coordinate handling, and regulatory obligations.

## Non-Goals

- No bundled nationwide PMTiles file.
- No authenticated or resumable PMTiles downloader in this policy step.
- No paid map provider removal until replacement QA is proven on real devices.
