## Context

TrailMate's Android shell already exposes the correct five-tab IA and the route import core can parse GPX/KML text into route geometry and import summaries. The `路线` tab still renders generic placeholder copy, so users cannot yet understand route assets or the result of importing a track.

The Lovable prototype at `D:\workSpace\trailguide-pro` is a visual and interaction reference. `AGENTS.md` remains the product source of truth: the route tab is a route asset center, not a planning, equipment, community, or commerce surface.

## Goals / Non-Goals

**Goals:**

- Provide a focused Compose `RoutesScreen` for the route tab.
- Represent route tab content with pure Kotlin state models that can be tested without Android UI instrumentation.
- Reuse `core.routeimport.RouteImportParser` for deterministic sample import data so the UI contract mirrors real parser output.
- Present the import result fields required by `AGENTS.md` and keep the commercial-map-bottom copy visible.
- Provide route asset cards and filters for offline, imported, favorite, and recent route views.

**Non-Goals:**

- No real Android file picker.
- No Room/DataStore persistence.
- No backend sync.
- No MapLibre rendering.
- No GPS, tracking service, or real navigation start.
- No route planner, equipment, community, marketplace, or complex pre-trip check.

## Decisions

1. **Use a feature package instead of expanding the shell file.**
   - Decision: add `com.trailmate.app.feature.routes`.
   - Rationale: the shell should only dispatch tabs; route asset presentation belongs to the route feature boundary.
   - Alternative considered: keep the route UI inside `TrailMateApp.kt`. This would be faster now but would make later file picking and route persistence harder to isolate.

2. **Introduce pure UI state and text contracts.**
   - Decision: add route tab models for filters, asset cards, import preview, metric rows, and quality notes.
   - Rationale: JVM tests can verify product wording, parser-derived metrics, and legacy-surface exclusions before Compose code is written.
   - Alternative considered: assert only Compose screenshots. That is useful later, but slower and too visual for product contract tests.

3. **Render a deterministic imported sample rather than fake metrics.**
   - Decision: build sample import preview by parsing an embedded GPX string through `RouteImportParser`.
   - Rationale: the UI should not drift from import-core semantics. Distance, elevation, waypoint count, track point count, and warning labels come from the same path real imports will use.
   - Alternative considered: hard-code all import numbers. That would look good but would not prove the import result contract is wired correctly.

4. **Keep actions as affordances only.**
   - Decision: show `导入 GPX / KML`, `开始导航`, `保存到路线`, and `查看详情` as screen controls without implementing side effects in this slice.
   - Rationale: the next slices need file picker, local route storage, detail routing, and navigation session state. Mixing those into this UI slice would make the PR too broad.
   - Alternative considered: wire one fake callback. It would not add production behavior and could hide missing architecture.

## Risks / Trade-offs

- [Risk] Users may assume the visible import CTA already opens a real picker. → Mitigation: keep this branch as a draft PR and document that side effects are deliberately out of scope.
- [Risk] Static sample state can become stale as route models evolve. → Mitigation: derive import preview from `RouteImportParser` and cover it with JVM tests.
- [Risk] UI polish may still be limited without screenshot review on device. → Mitigation: run unit tests and assemble now; install/smoke on the connected device if available after implementation.

## Migration Plan

1. Add route tab state tests and watch them fail.
2. Add route feature state models and deterministic sample builder.
3. Replace the `路线` placeholder branch in the shell with `RoutesScreen`.
4. Run unit tests, OpenSpec validation, debug build, and optional connected-device smoke test.
5. Rollback is the removal of `feature.routes` and restoring the old route placeholder branch.

## Open Questions

- The real file picker and import persistence belong in a later PR.
- Route detail navigation and starting an actual navigation session belong in later PRs after navigation/session boundaries are in place.
