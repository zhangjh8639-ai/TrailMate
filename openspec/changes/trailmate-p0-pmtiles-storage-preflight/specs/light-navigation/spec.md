# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Android remote PMTiles downloads shall preflight local storage

TrailMate SHALL avoid starting a remote PMTiles basemap download when known catalog metadata proves the remaining download bytes exceed usable local storage.

#### Scenario: Known PMTiles pack size exceeds usable local storage

- GIVEN Android selects a remote PMTiles catalog item whose bounds fully contain the target route bounds
- AND the catalog item has a known positive `sizeBytes`
- AND the target PMTiles directory has less usable storage than the remaining bytes required for the pack
- WHEN Android prepares to import the remote PMTiles pack
- THEN Android does not call the remote downloader
- AND Android offers the local PMTiles picker fallback with an insufficient-storage message

#### Scenario: Partial PMTiles download reduces remaining storage requirement

- GIVEN Android selects a remote PMTiles catalog item with a known positive `sizeBytes`
- AND a partial `.download` file already exists for the route pack
- AND usable storage covers the known remaining bytes after subtracting the partial file size
- WHEN Android prepares to import the remote PMTiles pack
- THEN Android can call the remote downloader so the resumable download path can continue

#### Scenario: Unknown PMTiles pack size remains compatible

- GIVEN Android selects a remote PMTiles catalog item whose `sizeBytes` is missing or non-positive
- WHEN Android prepares to import the remote PMTiles pack
- THEN Android does not block the download solely because a known size is unavailable
