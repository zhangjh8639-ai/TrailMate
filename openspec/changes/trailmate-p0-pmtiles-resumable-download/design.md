# Design: P0 PMTiles Resumable Download

## Android

`TrailMateHttpPmTilesBasemapFileDownloader` remains a small `HttpURLConnection` implementation so this change stays surgical. When the target file already exists with non-zero length, the downloader sends `Range: bytes=<existingLength>-`. A `206` response appends bytes to the existing file. A `200` response overwrites the file from scratch because the server ignored the range request. Non-retryable client errors remove unusable target files; interrupted non-empty files are preserved for a later retry.

The downloader accepts an optional Bearer token. This only proves the mobile boundary can carry auth to an authenticated download endpoint later; the server-side auth filter is explicitly outside this change.

`TrailMatePmTilesBasemapRemoteImportCoordinator` keeps an existing
`<routePackKey>.pmtiles.download` file in place before calling the downloader,
so the route import flow can actually resume a preserved partial file instead
of always restarting.

## Server

`OfflineBasemapDownloadController` returns `FileSystemResource` responses, and Spring MVC already handles single HTTP `Range` requests for those resources. This change captures that behavior with controller tests so future refactors do not accidentally remove resumable PMTiles support. It supports:

- full responses when no range is requested;
- `bytes=start-`;
- `bytes=start-end` clamped to the file length;
- `416 Requested Range Not Satisfiable` with `Content-Range: bytes */<length>` for invalid or unsatisfiable ranges.

The controller keeps path-safety in `OfflineBasemapFileService`.

## Readiness

Downloaded files still pass through `PmTilesOfflineBasemapImportPolicy` and `PmTilesArchiveHeaderParser` before the app marks a route-pack ready. A resumed file is not trusted merely because HTTP returned success.
