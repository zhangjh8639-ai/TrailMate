## Context

TrailMate is being rebuilt as an Android-native hiking track navigation app. The route tab already parses GPX/KML files and lets the user add a parsed import into the current route list, but the saved asset exists only in Compose memory. If Android kills the process, the imported route disappears.

The current repo is an early single-module Android app with Kotlin, Compose, core route models, GPX/KML parsing, and unit tests. It does not yet have Hilt, Room, ViewModels, or a multi-module persistence layer. The AGENTS guide allows Room / SQLite, and this slice should stay focused on imported route persistence rather than introducing the full future architecture.

## Goals / Non-Goals

**Goals:**

- Persist successfully parsed imported GPX/KML route assets to local SQLite.
- Store route metadata and geometry data, not only UI copy.
- Preserve production boundaries: imported routes are private, track-only, unverified, and do not include commercial basemap data.
- Restore persisted imported routes when the app starts and show them before bundled/sample routes.
- Keep duplicate saves idempotent by import identity.
- Cover the behavior with unit tests before implementation.

**Non-Goals:**

- Add route detail, active navigation, MapLibre rendering, GPS recording, foreground service, or server sync.
- Add a full Room/Hilt/ViewModel architecture in this slice.
- Add broad storage permissions or background file watchers.
- Persist failed or unsupported imports.
- Add route editing, arbitrary planning, equipment, community, or marketplace behavior.

## Decisions

1. Use Android SQLite via `SQLiteOpenHelper` for this slice.
   - Rationale: the app has no Room/Hilt infrastructure yet, and adding a full persistence stack would expand the PR beyond the route import persistence risk.
   - Alternative considered: Room. Room is the likely long-term direction, but it requires new dependencies and schema/migration setup. This can be introduced when the broader local data layer is cut.

2. Store route rows and geometry points in separate tables.
   - `imported_routes` stores stable metadata: id, file name, source type, route name, distance, elevation gain, waypoint count, track point count, has elevation, imported timestamp, privacy visibility, offline status, and confidence.
   - `imported_route_points` stores ordered coordinates with elevation and cumulative distance.
   - Rationale: geometry can be large and should not be packed into preferences or UI text.
   - Alternative considered: single JSON blob. That is simpler initially but makes validation, partial loading, and future migration harder.

3. Keep persistence mappers independent from Compose.
   - Route import results become persistent records through pure Kotlin mapping.
   - Persistent records become `RouteAssetCardState` through a separate mapper.
   - Rationale: tests can validate privacy/trust defaults and UI ordering without an Android device.

4. Save only parsed imports with valid geometry and source format.
   - Rationale: failed imports cannot produce reliable navigation data and must remain unsaveable.

5. Load persisted imports on app startup using an IO coroutine and merge them before sample/platform route cards.
   - Rationale: route assets should be visible immediately after restart while keeping the current single-screen Compose app simple.

## Risks / Trade-offs

- SQLiteOpenHelper is a temporary lower-level persistence choice -> Keep the surface small behind an `ImportedRouteStore` interface so a later Room migration is contained.
- Large GPX files can create many point rows -> Use a transaction and ordered point inserts; later slices can add simplification or chunked import if performance requires it.
- Startup load can race with a user-triggered import in the simple Compose state holder -> Merge persisted assets by identity and avoid replacing the active import preview.
- Geometry persistence omits waypoints/risk/exit point details in this slice -> Store the main route polyline and counts now; extend schema when route detail/navigation consumes structured annotations.
- Reinstalling the app will clear local SQLite data -> This is acceptable for local persistence; server sync is out of scope.

## Migration Plan

- Create SQLite schema version 1 for imported route persistence.
- Existing users of the current rebuild have no stored route DB, so no data migration is required.
- Rollback is safe: removing the feature only hides persisted imports; no server data or shared files are affected.

## Open Questions

- Whether the later full local data layer should migrate this store to Room in place or keep a small SQLite gateway.
- How route-level offline packages should reference imported route geometry once MapLibre and navigation are introduced.
