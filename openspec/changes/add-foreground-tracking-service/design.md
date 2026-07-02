## Context

The app has core navigation algorithms, a runtime reducer, and a location provider abstraction, but no Android component that can legally keep navigation/tracking alive once the UI goes to the background. A foreground service shell is the next safe step before wiring GPS subscription and route session persistence.

## Goals / Non-Goals

**Goals:**
- Add an Android service class dedicated to active tracking/navigation.
- Start foreground mode with a persistent notification when explicitly started.
- Stop cleanly through an explicit stop action.
- Register the service and required foreground service permissions in the manifest.
- Keep notification content professional and clear that tracking/navigation is active.

**Non-Goals:**
- No runtime permission request UI.
- No location subscription, route session persistence, crash recovery, or track database writes in this slice.
- No background location permission declaration.
- No Compose UI control for starting the service.

## Decisions

1. Build a foreground service shell before GPS subscription.
   - Rationale: Android foreground service lifecycle, notification, and manifest permissions are safety-critical and deserve their own review.
   - Alternative considered: wire location provider immediately; deferred to avoid mixing lifecycle, permissions, and navigation state in one PR.

2. Keep start/stop intent construction in a small helper object.
   - Rationale: tests can verify actions and extras without instantiating Android services in local JVM tests.
   - Alternative considered: embed action strings only inside service; rejected because it is harder to test and reuse.

3. Declare `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, and `POST_NOTIFICATIONS`, but not `ACCESS_BACKGROUND_LOCATION`.
   - Rationale: active navigation needs a location foreground service and notification permission on modern Android, while background location is not introduced by this slice.

4. Notification text avoids rescue or safety guarantees.
   - Rationale: TrailMate must be clear that it is tracking/navigation support, not automatic rescue or guaranteed safety.

## Risks / Trade-offs

- [Risk] Service starts without actual GPS subscription yet. -> Mitigation: notification copy says tracking is being kept active, and GPS wiring is explicitly a later slice.
- [Risk] Android 13 notification permission may be denied. -> Mitigation: this slice only declares permission; runtime request and user-facing fallback will be handled later.
- [Risk] No real-device service start validation yet. -> Mitigation: build/install smoke can verify manifest/package validity; full service start needs explicit runtime permission UX in a later slice.
