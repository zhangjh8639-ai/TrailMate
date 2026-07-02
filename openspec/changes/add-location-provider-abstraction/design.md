## Context

TrailMate needs reliable real GPS input for track navigation, off-route detection, and eventual background tracking. Current code can process `LocationSample` objects but has no platform boundary for obtaining them from Android.

This slice adds the boundary only. It intentionally stops before foreground services, runtime permission UI, persistence, and navigation screen wiring.

## Goals / Non-Goals

**Goals:**
- Define a platform-independent `TrailLocationProvider` contract.
- Provide request/status/subscription models suitable for foreground navigation.
- Map Android location readings into core `LocationSample` values with validation.
- Add an Android `LocationManager` provider implementation that can be wired later by a service or ViewModel.
- Declare foreground location permissions in the manifest.

**Non-Goals:**
- No runtime permission prompts.
- No foreground service, notification, WorkManager, or background location request.
- No Compose UI, map rendering, persistence, or route session orchestration.
- No Google Play Services dependency in this slice.

## Decisions

1. Use callback/subscription APIs instead of Flow.
   - Rationale: the app does not yet depend on coroutines/Flow; avoiding a new dependency keeps this slice small.
   - Alternative considered: `Flow<LocationSample>`; deferred until architecture dependencies are introduced deliberately.

2. Use Android `LocationManager` first.
   - Rationale: it works on devices without GMS and aligns with the requirement for a non-GMS fallback.
   - Alternative considered: Google Play Services Fused Location; deferred as an optional provider behind the same interface.

3. Keep Android `Location` mapping testable by using a primitive `SystemLocationReading` DTO.
   - Rationale: local JVM unit tests should not depend on Android framework method bodies.
   - Alternative considered: test `android.location.Location` directly; rejected because local unit tests use Android stubs.

4. Declare only foreground fine/coarse location permissions.
   - Rationale: this slice enables foreground GPS collection only. Background location and foreground service permissions belong with the tracking service slice.

## Risks / Trade-offs

- [Risk] Provider code compiles but is not yet exercised on a device. -> Mitigation: later foreground service/UI slice will add instrumentation or real-device smoke validation.
- [Risk] Callback API can be adapted differently by future services. -> Mitigation: keep the contract minimal and side-effect boundaries explicit.
- [Risk] Permission calls can throw `SecurityException` if caller starts without permission. -> Mitigation: provider reports `PermissionDenied` through observer and does not own runtime permission prompts.
