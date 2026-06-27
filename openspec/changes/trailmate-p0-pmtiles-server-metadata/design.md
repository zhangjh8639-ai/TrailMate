# Design: PMTiles Server Metadata

`OfflineBasemapService` remains the query boundary for route-covering PMTiles packs. It will optionally receive `OfflineBasemapFileService` and enrich only catalog items whose `downloadUrl` points at the server-hosted PMTiles route:

`/offline-basemaps/pmtiles/<file>.pmtiles`

For a matching local URL, the service extracts the file name, asks `OfflineBasemapFileService.findPmTilesFile(...)` to enforce path safety, and if the file exists it returns a new `OfflineBasemapCatalogItem` with:

- `sizeBytes` set to `Files.size(path)`;
- `sha256` set to a streaming SHA-256 hex digest.

If the URL is external, the file is missing, or metadata cannot be read, the original catalog item is returned unchanged. This keeps the catalog endpoint available even when an operator has not mounted PMTiles files yet, while allowing production deployments to provide Android with a real integrity proof as soon as files are present.

Because PMTiles packs can be large, the service caches local metadata by normalized file path plus file size and last modified time. Catalog requests still stat matching files, but avoid full-file SHA-256 reads unless the file metadata changes.

The existing one-argument service constructor stays available for tests and simple wiring. The Spring configuration will use the new two-argument constructor so production server responses are enriched.
