# Design: PMTiles SHA-256 Validation

`TrailMatePmTilesBasemapRemoteImportCoordinator` remains the integrity gate for remote PMTiles downloads. After the downloader returns a temporary file and before `PmTilesOfflineBasemapImportPolicy.resolve(...)`, the coordinator checks `selected.sha256`.

If the checksum is non-blank:

- normalize it by trimming and lowercasing;
- require exactly 64 hex characters;
- compute SHA-256 over the downloaded file using streaming reads;
- compare case-insensitively;
- delete the temporary download on mismatch or malformed expected hash;
- return the existing local-picker fallback action.

If the checksum is blank or `null`, the coordinator continues to archive-header validation. This preserves current preview/local-server deployments while allowing production catalogs to opt into integrity proof without a schema migration.

The checksum helper should be private to the coordinator for now. It has one caller and does not need a shared abstraction until another remote asset pipeline uses it.
