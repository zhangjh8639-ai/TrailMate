# light-navigation Specification Delta

## ADDED Requirements

### Requirement: PMTiles remote downloads shall be retry-safe for large offline route packs

TrailMate SHALL resume interrupted PMTiles route-pack downloads where the server supports byte ranges, while keeping local archive validation as the final readiness proof.

#### Scenario: Android resumes a partial PMTiles route-pack download

- GIVEN a partial PMTiles download file already exists in app storage
- WHEN Android retries the same PMTiles download URL
- THEN Android sends `Range: bytes=<partial-size>-`
- AND appends a `206 Partial Content` response to the partial file
- AND keeps the downloaded file subject to local PMTiles archive validation before readiness changes

#### Scenario: Android restarts when the server ignores Range

- GIVEN a partial PMTiles download file already exists in app storage
- AND Android sends a `Range` request
- WHEN the server responds with `200 OK`
- THEN Android overwrites the partial file from the full response instead of appending duplicate bytes

#### Scenario: Android rejects a mismatched partial response

- GIVEN a partial PMTiles download file already exists in app storage
- AND Android sends `Range: bytes=<partial-size>-`
- WHEN the server responds with `206 Partial Content`
- BUT the `Content-Range` start byte does not equal the local partial file size
- THEN Android rejects the response
- AND preserves the existing partial file for a safer retry or fallback
- AND does not mark the PMTiles basemap ready

#### Scenario: Android can pass a download auth token

- GIVEN Android has a non-blank PMTiles download access token
- WHEN Android requests the PMTiles download URL
- THEN Android sends `Authorization: Bearer <token>`
- AND the token is not written into PMTiles files, logs, or local route-pack metadata by the downloader

#### Scenario: Server serves a satisfiable PMTiles byte range

- GIVEN a PMTiles file exists in the configured offline basemap directory
- WHEN the client requests `Range: bytes=2-`
- THEN the server responds with `206 Partial Content`
- AND includes `Accept-Ranges: bytes`
- AND includes `Content-Range: bytes 2-<last-byte>/<file-size>`
- AND the response body contains only the requested bytes

#### Scenario: Server rejects an invalid PMTiles byte range

- GIVEN a PMTiles file exists in the configured offline basemap directory
- WHEN the client requests an invalid or unsatisfiable byte range
- THEN the server responds with `416 Requested Range Not Satisfiable`
- AND includes `Content-Range: bytes */<file-size>`
