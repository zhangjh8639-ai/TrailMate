# Tasks

- [x] Document MapLibre + Protomaps/PMTiles + OSM as the offline basemap direction.
- [x] Add readiness policy for MapLibre runtime and local PMTiles route-pack gates.
- [x] Keep local GPX Canvas fallback when PMTiles basemap is missing.
- [x] Add Chinese readiness copy for route-only preview vs PMTiles offline basemap.
- [x] Validate the policy with Android unit tests.
- [x] Add MapLibre Native dependency and classpath availability test.
- [x] Add local PMTiles route-pack manifest/status model.
- [x] Add MapLibre route-map surface behind the PMTiles readiness gates.
- [x] Add local PMTiles file import flow behind the same gates.
- [x] Validate PMTiles v3 headers, vector tile type, and route-bounds coverage before enabling MapLibre PMTiles.
- [x] Keep default MapLibre offline style geometry-only until Protomaps glyph/sprite assets are bundled.
- [x] Add server PMTiles catalog API and Android catalog client/selection contract.
- [x] Add Android remote PMTiles download/import attempt with validation and local-picker fallback.
- [ ] Later: add authenticated/resumable PMTiles file download behind the same gates.
- [ ] Later: bundle full Protomaps glyph/sprite/style assets for labeled offline maps.
