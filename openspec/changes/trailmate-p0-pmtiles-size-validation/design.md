# Design: PMTiles Size Validation

`TrailMatePmTilesBasemapRemoteImportCoordinator` remains the integrity gate for remote PMTiles imports. After the downloader returns the temporary `.pmtiles.download` file, Android checks the selected catalog item:

- if `sizeBytes` is `null`, keep existing preview-compatible behavior;
- if `sizeBytes` is positive, require `downloadedFile.length()` to equal `sizeBytes`;
- if the size does not match, delete the temporary download and fall back to the local `.pmtiles` picker;
- only after size validation passes should SHA-256 validation and PMTiles archive validation continue.

Size validation is intentionally separate from SHA-256 validation. Size catches obvious truncation cheaply and is useful when catalog checksums are not yet available; SHA-256 remains the stronger integrity proof when present.
