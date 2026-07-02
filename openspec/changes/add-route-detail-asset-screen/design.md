## Context

TrailMate is being rebuilt as an Android-native hiking track navigation app. The Routes tab already supports GPX/KML import, save-to-route, and local SQLite restoration of imported route assets. However, the route card is too compressed to explain whether a route is a platform route, imported track-only route, trusted offline route, or unverified local import.

The current Android app is still a small single-module Compose app with local state in `TrailMateApp` and pure Kotlin route tab state mappers. This slice should improve the route asset flow without introducing the full future navigation stack.

## Goals / Non-Goals

**Goals:**

- Add a focused route detail state that can be built from an existing route asset card.
- Show safety-relevant route facts and bounded copy in Chinese.
- Make saved/restored imported routes inspectable while preserving private, track-only, unverified, no-basemap language.
- Keep the route detail screen a read-only inspection layer that does not pretend to start navigation.
- Preserve the current import preview state when opening and closing route detail.
- Cover state behavior with tests before implementation.

**Non-Goals:**

- Add real active navigation, MapLibre map rendering, GPS location, foreground service, or track recording.
- Add arbitrary route planning, route editing, pretrip check flows, equipment, community, or marketplace behavior.
- Add server APIs, offline package downloads, or commercial map basemap handling.
- Introduce Navigation Compose or a full ViewModel/DI architecture in this small UI slice.

## Decisions

1. Build detail state from `RouteAssetCardState`.
   - Rationale: existing route assets already centralize the fields needed for a first production-facing detail layer.
   - Alternative considered: introduce a full `Route` repository for all route details now. That would be more future-proof but would expand this PR into data architecture work not required for the detail UI bridge.

2. Keep detail routing in the current app shell state.
   - Rationale: the app does not yet use Navigation Compose; adding it only for one detail screen would be disproportionate.
   - Alternative considered: add Navigation Compose now. It is likely useful later, but this PR should stay focused on route detail and keep migration risk low.

3. Do not expose a fake start-navigation side effect from detail.
   - Rationale: active navigation is the product core and must be implemented with GPS, navigation state, and foreground service support, not a placeholder.
   - Alternative considered: keep a static "开始导航" button for visual continuity. This risks misleading users and tests, so it remains out of scope.

4. Render imported route caveats as first-class route detail sections.
   - Rationale: GPX/KML imports only provide route geometry and waypoints. The detail screen must state that they do not include commercial or full offline map basemap data.

## Risks / Trade-offs

- Detail state derived from card fields can become insufficient for future route versions and annotations -> Keep the new model small and pure so it can later be backed by a richer route repository.
- Keeping routing in Compose state is temporary -> Limit it to route detail selection/back handling so future Navigation Compose migration is contained.
- Removing or hiding placeholder navigation actions may make the screen feel less "complete" visually -> Prefer product honesty over fake affordances until navigation exists.
- Imported route details may lack duration/difficulty/confidence -> Display "待确认", "未验证", and "可信度待确认" instead of inventing analysis.

## Migration Plan

- No data migration is required.
- Existing route cards remain visible.
- Saved imported routes become inspectable because their detail action is populated.
- Rollback is safe: removing this feature only removes a UI/detail state layer and does not affect persisted route records.

## Open Questions

- Whether future route details should be loaded from the route database/server repository or composed from local asset snapshots.
- Whether the future navigation PR should hide the bottom bar during active navigation and route detail consistently.
