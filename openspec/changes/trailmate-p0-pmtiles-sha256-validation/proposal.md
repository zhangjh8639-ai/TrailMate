# TrailMate P0 PMTiles SHA-256 Validation

## Why

Remote PMTiles catalog items already expose an optional `sha256` field, but Android currently downloads and validates only the PMTiles archive shape. A corrupt or mismatched remote file can still reach the archive validator if its header looks valid. Offline basemaps are safety-critical context, so TrailMate should verify the catalog checksum whenever the server provides one.

## What Changes

- Android computes SHA-256 for downloaded remote PMTiles packs when the selected catalog item provides a non-blank checksum.
- A checksum mismatch deletes the temporary `.pmtiles.download` file and falls back to the local PMTiles picker.
- Blank or absent checksums remain allowed for preview/local deployments, but the app does not treat the checksum as verified.

## Non-Goals

- Do not add signing, certificate pinning, or transparency logs in this change.
- Do not require checksums for local file-picker imports.
- Do not change the PMTiles catalog API schema because `sha256` already exists.
