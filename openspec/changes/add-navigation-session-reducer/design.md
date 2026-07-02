## Context

The codebase already has route projection, route progress, off-route evidence, a domain-level `NavigationStateReducer`, and `NavigationSnapshotEngine`. Those pieces can calculate one location snapshot, but they do not keep runtime evidence between samples or apply user actions such as pause, resume, original return, and end navigation.

This slice adds a pure reducer that will later be called by a foreground tracking service or ViewModel. It must remain independent from Android lifecycle, permissions, storage, and UI so it can be tested with deterministic inputs.

## Goals / Non-Goals

**Goals:**
- Represent active navigation runtime state in one immutable object.
- Apply user actions and location samples through a single reducer entry point.
- Preserve previous off-route evidence and progress across samples.
- Keep manual and terminal safety states stable until an explicit action changes them.

**Non-Goals:**
- No GPS provider, permission request, foreground service, database persistence, map rendering, or Compose UI.
- No route re-planning or safe straight-line return.
- No Live Share or emergency contact transport.

## Decisions

1. Add a new reducer instead of extending `NavigationSession.reduce` with location data.
   - Rationale: `NavigationSession.reduce` is a small domain event reducer; the runtime reducer needs route geometry, location samples, battery level, previous evidence, previous progress, and snapshot output.
   - Alternative considered: add all fields to `NavigationSession`; rejected because session identity and live computation evidence have different lifecycles.

2. Store latest off-route evidence and route progress in runtime state.
   - Rationale: sustained deviation and stable projection require history between samples.
   - Alternative considered: recompute from scratch every sample; rejected because confirmation duration/sample thresholds would not work.

3. Treat original return, pause, and ended as explicit user states.
   - Rationale: TrailMate must not silently override safety-critical user intent during return or pause flows.
   - Alternative considered: let snapshots always choose the state; rejected because it can switch a returning user back to ordinary navigation.

4. Keep the reducer side-effect free.
   - Rationale: it enables unit tests, deterministic debugging, and future reuse from service, ViewModel, and recovery flows.

## Risks / Trade-offs

- [Risk] The reducer can only return in-memory state, not persisted recovery state. -> Mitigation: persistence and crash recovery will be a later service/storage slice.
- [Risk] GPS unreliable after confirmed off-route needs careful UI language. -> Mitigation: preserve the session state while suppressing nearest-route guidance until a reliable sample returns.
- [Risk] Route geometry is passed on each location update. -> Mitigation: acceptable for the current small core; later route package/session repositories can own geometry lookup.
