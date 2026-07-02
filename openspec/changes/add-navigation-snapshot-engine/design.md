## Context

The app already has independent pieces for route projection, route progress, off-route evidence, and navigation session state. The next production step is to compose those pieces into a pure engine that future GPS providers, foreground services, and Navigation UI can call consistently. This layer must remain Android-service-free so it can be tested thoroughly before real location and background recording are connected.

## Goals / Non-Goals

**Goals:**
- Produce a `NavigationSnapshot` for each accepted location sample.
- Reuse existing geometry algorithms for projection, progress, remaining distance/elevation, next waypoint, and nearest exit.
- Reuse `OffRouteDetector` and map its evidence into the conservative `NavigationState` contract.
- Provide nearest route point guidance as direction text plus distance, without implying a safe straight-line return.
- Keep all behavior deterministic and unit-testable.

**Non-Goals:**
- No Android permission prompts, GPS subscriptions, foreground services, notifications, track persistence, MapLibre rendering, or UI work.
- No automatic rerouting or safe path planning after a deviation.
- No network calls, weather data, server sync, or route package downloads.

## Decisions

1. **Pure engine object in `core.geo`**
   - Decision: add a stateless engine that accepts all inputs explicitly and returns a snapshot plus off-route evidence.
   - Rationale: existing route math lives in `core.geo`, and tests can run without Android runtime dependencies.
   - Alternative considered: put this in `feature.navigation`; rejected because the logic must be shared by UI and future tracking service.

2. **Return both snapshot and evidence**
   - Decision: engine output includes the generated `NavigationSnapshot` and the latest `OffRouteEvidence`.
   - Rationale: off-route confirmation needs prior evidence across samples; callers should not re-run or infer it from UI state.
   - Alternative considered: mutate a session object; rejected because this slice should stay side-effect-free.

3. **Map GPS-unreliable evidence without escalating to off-route**
   - Decision: poor accuracy yields a snapshot with the session's current state preserved and no deviation alert state escalation.
   - Rationale: AGENTS guidance says GPS accuracy must be considered before warning users; bad GPS alone must not become off-route proof.

4. **Nearest route point guidance is informational**
   - Decision: guidance contains compass direction and distance to the projected route point only.
   - Rationale: the app must not say a straight line to the route is safe; UI can pair this with conservative copy.

## Risks / Trade-offs

- **Compass direction approximation** -> Use bearing-based eight-way labels; future map UI can improve with visual overlays.
- **Loop/crossing route ambiguity** -> Current projection still uses nearest segment; future route progress stabilization can add heading/history windows.
- **No real GPS in this slice** -> Unit tests prove deterministic core behavior, while device verification is limited to build/install smoke if needed.
