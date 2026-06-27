# TrailMate P0 PMTiles Server Metadata

## Why

Android now validates PMTiles downloads against catalog `sha256` values when the server provides them. The default server catalog still publishes a placeholder `sizeBytes` and a blank checksum for the hosted Hangzhou PMTiles pack, so a production deployment cannot give the phone a trustworthy size or integrity proof from the built-in catalog.

## What Changes

- Server-side PMTiles catalog responses enrich locally hosted `/offline-basemaps/pmtiles/*.pmtiles` entries with the actual file size.
- Server-side PMTiles catalog responses enrich locally hosted entries with a streaming SHA-256 checksum when the configured file exists.
- Missing local files and external download URLs keep their configured catalog metadata so preview and CDN deployments stay compatible.

## Non-Goals

- Do not add PMTiles generation or upload tooling.
- Do not change the PMTiles catalog API schema.
- Do not require external CDN URLs to be reachable during catalog listing.
