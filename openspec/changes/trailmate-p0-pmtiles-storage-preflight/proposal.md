# TrailMate P0 PMTiles Storage Preflight

## Why

PMTiles offline basemap packs can be large. If Android starts a remote basemap download when local storage is already too low, the user can lose time, battery, and network while still ending up without an offline map. The catalog already publishes `sizeBytes`, so TrailMate can avoid a known-unsatisfiable download before touching the network.

## What Changes

- Android checks usable local storage before remote PMTiles download when the selected catalog item has known positive `sizeBytes`.
- Existing `.download` partial files reduce the remaining bytes needed for resumable downloads.
- Unknown or non-positive catalog sizes remain compatible and do not block download attempts.
- On known insufficient storage, Android opens the local picker fallback with a storage-specific message and does not call the downloader.

## Non-Goals

- Do not change the server catalog API schema.
- Do not add Android storage cleanup tools.
- Do not change PMTiles checksum, size, or archive validation after download.
