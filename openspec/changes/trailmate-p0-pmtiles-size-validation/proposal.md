# TrailMate P0 PMTiles Size Validation

## Why

The server can now publish actual PMTiles `sizeBytes` metadata for locally hosted packs. Android already verifies `sha256` when present, but checksum metadata can be absent for preview or transitional deployments. A PMTiles file with a valid header but truncated body should not be accepted when the catalog declares the expected size.

## What Changes

- Android verifies the downloaded remote PMTiles file length against catalog `sizeBytes` when it is a positive value.
- A size mismatch deletes the temporary `.pmtiles.download` file and falls back to the local PMTiles picker.
- Missing `sizeBytes` remains compatible with existing preview catalogs, while still requiring SHA-256 validation when provided and PMTiles archive validation.

## Non-Goals

- Do not add progress UI or available-storage checks in this change.
- Do not change the PMTiles catalog API schema.
- Do not require local file-picker imports to carry catalog size metadata.
