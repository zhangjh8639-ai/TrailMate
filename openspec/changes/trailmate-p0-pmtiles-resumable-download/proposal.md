# TrailMate P0 PMTiles Resumable Download

## Why

TrailMate is moving away from provider-locked offline basemaps toward a MapLibre + PMTiles offline route-pack direction. The current Android downloader performs a single full-file GET and deletes partial files on failure, which is fragile for large PMTiles packages on mobile networks. The server download endpoint also returns only full-file responses, so Android cannot resume an interrupted route-pack download.

## What Changes

- Add retry-safe Android PMTiles downloading that resumes from a partial local file using HTTP `Range`.
- Allow Android to attach an optional Bearer token to PMTiles file downloads without logging or persisting it in the downloader.
- Add regression coverage for server single byte-range PMTiles responses with `206 Partial Content`, `Accept-Ranges`, and `Content-Range`.
- Keep local picker fallback and local PMTiles validation as the readiness gate; catalog metadata and a completed HTTP response still do not prove offline basemap readiness.

## Non-Goals

- Do not add a full Spring Security authorization filter in this change.
- Do not bundle Protomaps glyph/sprite assets.
- Do not change the route map UI flow.
- Do not commit real PMTiles binaries.
