## Context

The route tab can now parse a real GPX/KML document and show a temporary preview. That preview intentionally says `未保存，仅本次查看`, because the previous slice did not create a route asset. The next production step is to let users explicitly save a parsed preview into the route asset list while keeping the implementation small enough to review.

The current app shell has no ViewModel, Room, DataStore, Hilt, or navigation graph yet. The route tab state is an in-memory Compose state holder. This design respects that stage: it adds a clear save state transition and route asset representation, but defers durable persistence to a later database slice.

## Goals / Non-Goals

**Goals:**

- Add a real save event for parsed import previews.
- Convert a parsed `RouteImportResult` into a route asset card with imported source, `仅轨迹可用`, unverified confidence, and private/track-only semantics through the existing core route model.
- Make saved copy unambiguous: saved imported route assets are not verified platform routes and do not include commercial map basemaps.
- Keep failed imports from exposing save/start actions.
- Keep reducer behavior pure and unit-testable.

**Non-Goals:**

- No Room/DataStore persistence in this slice.
- No route detail destination.
- No actual start-navigation side effect.
- No MapLibre rendering.
- No GPS, foreground service, background recording, or offline package download.
- No planner, equipment, community, marketplace, or complex pre-trip-check surfaces.

## Decisions

### Decision: Save through a pure reducer first

Add a reducer such as `RoutesTabState.withSavedImport(...)` that takes the current parsed import context and returns a new state with an imported route asset prepended. This keeps the first save step testable without Android framework dependencies.

Alternatives considered:

- Add Room now: rejected because the app has no database infrastructure yet and this would combine schema, DAO, repository, ViewModel, and UI work in one large PR.
- Keep save as a static button: rejected because the product flow needs visible movement from preview to route asset.

### Decision: Keep parsed import context in route tab state

The preview labels are enough for UI, but save needs the parsed geometry and source type. Add a small state field or value object that carries the latest parsed `RouteImportResult` only when it is valid and saveable. Failed imports must not carry saveable context.

Alternatives considered:

- Re-parse content on save: rejected because the selected document URI/text should not be retained or re-read silently.
- Store raw XML content in state: rejected because it bloats UI state and is unnecessary once parsed.

### Decision: Asset copy reflects unverified track-only route

Saved imported route cards should use:

- `sourceLabel`: `GPX 导入` or `KML 导入`
- `offlineStatusLabel`: `仅轨迹可用`
- `estimatedDurationLabel`: `待确认`
- `difficultyLabel`: `未验证`
- `confidenceLabel`: `可信度待确认`
- risk tags such as `导入轨迹`, `未验证`, `不含地图底图`

This prevents imported GPX/KML tracks from looking like curated platform routes.

### Decision: Deduplicate by saved import identity

If the same file/route is saved repeatedly in the same session, the route asset list should not grow duplicate cards. Use a deterministic in-memory id derived from source type, filename, route name, distance, and track point count. This is not a permanent database id; it is a stable session key.

Alternatives considered:

- Always append: rejected because it creates obvious duplicate clutter.
- Generate random UUID: deferred to persistent storage, where IDs should be owned by repository/database.

## Risks / Trade-offs

- **In-memory save disappears on process recreation** -> Document the limitation and keep the next slice focused on Room/Repository persistence.
- **Saved button still lacks route detail/navigation side effects** -> Keep labels clear and do not wire navigation until a dedicated route detail/navigation entry slice exists.
- **Imported route metadata is incomplete** -> Use `待确认`/`未验证` copy rather than fake region, difficulty, or duration.
- **Session dedupe is approximate** -> Good enough for temporary in-memory route assets; persistent IDs belong in the database slice.

## Migration Plan

No migration is needed. This is an in-memory state transition only.
