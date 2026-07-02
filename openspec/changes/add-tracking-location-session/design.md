## Context

The previous slices added a foreground tracking service, runtime permission start flow, and a `TrailLocationProvider` abstraction. The service can now legally run as a location foreground service, but it still stops at lifecycle control; it does not yet subscribe to Android location updates.

This change must remain small enough for review because GPS sampling is a safety-critical boundary. It should prove that active navigation is backed by real system location callbacks, while leaving route matching, persistence, off-route logic, and crash recovery for later PRs.

## Goals / Non-Goals

**Goals:**
- Add a pure Kotlin tracking location session object that can be unit-tested without an Android service.
- Start a real `TrailLocationProvider` subscription when the foreground service accepts a permitted start action.
- Stop and release the active subscription when the service receives stop, null, or unknown actions.
- Keep latest sample, sample count, and provider status in memory for future route matching and diagnostics.
- Ensure no samples are synthesized before the provider emits a real location.

**Non-Goals:**
- No Room/SQLite persistence or route record creation.
- No route matching, off-route detection, progress calculation, or emergency card updates.
- No background location permission.
- No fake location generation, simulated progress, upload, or live sharing.
- No notification text changes based on live GPS state in this slice.

## Decisions

1. Introduce `TrackingLocationSession` as a pure Kotlin owner of provider subscription state.
   - Rationale: the service lifecycle should be thin, while session behavior can be tested with fake providers and real `LocationSample` objects.
   - Alternative considered: call `TrailLocationProvider` directly from `TrackingForegroundService`; rejected because service-local behavior is harder to unit-test and easier to regress.

2. Extend the service controller with location start/stop commands.
   - Rationale: command tests can prove the intended ordering: enter foreground first, then subscribe; stop subscription before foreground shutdown.
   - Alternative considered: keep only foreground commands and imperatively subscribe in service; rejected because the lifecycle contract would be implicit.

3. Store session state in memory only.
   - Rationale: persistence needs route/session identity, crash recovery, and privacy decisions that belong in later slices.
   - Alternative considered: write every sample to local storage now; rejected to avoid mixing real GPS subscription with track database design.

4. Construct `AndroidLocationProvider` inside the Android service.
   - Rationale: the current app does not have DI/Hilt wired for services yet, and adding DI would make this slice larger than the capability requires.
   - Alternative considered: introduce Hilt injection now; deferred until broader service/repository wiring exists.

## Risks / Trade-offs

- [Risk] In-memory latest sample is lost if the process is killed. -> Mitigation: this slice does not claim recovery; persistence and crash recovery are planned later.
- [Risk] Service may receive provider errors without user-visible diagnostics yet. -> Mitigation: session state records provider status for future UI/diagnostic surfaces.
- [Risk] Android service construction remains lightly integrated without DI. -> Mitigation: keep the Android-specific provider factory small and service-local for now.
- [Risk] Real-device smoke can confirm service start/stop but not easily inspect private session state. -> Mitigation: unit tests cover subscription lifecycle; device smoke covers no-crash foreground behavior.

## Migration Plan

No data migration. Deploying this change only adds service-owned location subscription lifecycle. Rollback is reverting the service wiring and session class.

## Open Questions

- Track persistence format, crash recovery, and route-matching handoff remain for later OpenSpec changes.
