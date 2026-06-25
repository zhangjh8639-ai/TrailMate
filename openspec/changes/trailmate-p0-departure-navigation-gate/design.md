# Design: TrailMate P0 Departure Navigation Gate

## Product Contract

TrailMate's route cockpit is the boundary between planning and field use. It may show the route, checkpoint progress preview, and the next missing setup action, but it must not invite the user to start walking until the departure checklist is truly ready.

The first implementation slice focuses on deterministic presentation logic:

- `DepartureReadinessEngine` remains the source of truth for route readiness.
- `RouteCockpitPresentationEngine` must treat any non-start `DepartureReadinessSummary.primaryActionLabel` as a blocker, including offline base-map repair actions.
- `RouteWorkspaceScreen` copy should describe opening the route as a preview/preparation step, not as a generic "continue" action.

## Technical Approach

Use TDD against pure Kotlin model logic first. The existing tests already reveal the unsafe behavior: missing required offline base maps currently still allow `START_HIKE`. Update those tests to express the P0 safety contract, then minimally adjust `RouteCockpitPresentationEngine.primaryAction`.

After model behavior is green, make a narrow UI-copy change in `RouteWorkspaceScreen` so the route entry says `查看路线与出发检查`. This prevents a user from interpreting the route card as a field-start shortcut.

## Acceptance

- When `DepartureReadinessSummary` says `导入离线地图包`, `飞行模式验证底图`, `保存离线路线`, `授权定位`, `等待定位稳定`, or `补齐 N 件关键装备`, the cockpit primary action uses the matching repair kind.
- `RouteCockpitPrimaryActionKind.START_HIKE` appears only when departure readiness is complete and location is reliable.
- The normal route card does not use vague `继续准备` copy for an imported route.
- `openspec validate --all --strict` passes.
- Android unit tests pass for the affected route model and focused app suite.

