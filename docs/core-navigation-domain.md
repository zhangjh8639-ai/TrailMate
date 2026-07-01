# Core Navigation Domain

This slice adds TrailMate's first pure Kotlin domain layer under:

```text
app/src/main/java/com/trailmate/app/core/model
```

The models describe route assets, navigation sessions, navigation states, safety cards, records, feedback, and privacy defaults. They deliberately avoid Android framework location classes, Compose state, MapLibre, Room, DataStore, Retrofit DTOs, and GPX/KML parser types.

## Owns In This Slice

- Route identity, credibility, source, offline status, privacy, and geometry inputs.
- Typed units for distance, elevation, coordinates, GPS accuracy, battery, and timestamps.
- Navigation session identity, direction, state, high-level events, and reducer transitions.
- Navigation snapshots that future algorithms can populate.
- Emergency card data and safety copy helpers with constrained compass directions.
- Completed route records and structured trail-condition feedback.
- Private-by-default behavior for imported routes, navigation sessions, and route records.

## Future PR Owners

| Future area | Owner package or module |
| --- | --- |
| GPX/KML parsing and import validation | `feature.gpx_import`, future parser package |
| Route projection, progress, off-route detection | `core.geo` |
| Route package manifest and checksum | `core.offline` |
| Location provider abstraction and sampling | `core.location` |
| Foreground tracking and recovery | `services.tracking` |
| Room persistence | `core.database` |
| DataStore privacy/settings | `core.datastore` |
| Map rendering | `core.map`, `feature.navigation` |

## Non-Goals

- No route planning engine.
- No equipment, community, marketplace, or complex pre-trip-check domain.
- No GPS, foreground service, MapLibre, Room, API, or import parser implementation.
- No claim that off-route guidance is a safe crossing route.

## Verification

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
openspec validate add-core-navigation-domain
```
