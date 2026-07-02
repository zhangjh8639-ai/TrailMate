## Context

The navigation tab can display a selected route and the previous slice added `TrackingForegroundService`, but there is still no user-facing way to start the service. Android requires foreground location permission before a location foreground service can run, and Android 13+ requires notification permission for visible notifications. TrailMate must request these permissions at the navigation moment, not at app launch, and must explain the outcome in Chinese without pretending GPS or track recording exists before the next slices wire it.

## Goals / Non-Goals

**Goals:**
- Add a compact navigation-page control for starting active track navigation for the selected route.
- Request foreground location and Android 13+ notification permission only when the user starts navigation.
- Start and stop the real `TrackingForegroundService` through a small launcher boundary.
- Show permission denied/fallback copy that keeps route viewing available and clearly explains why continuous location cannot start.
- Keep the UI aligned with the existing Compose style: white/soft green surfaces, forest green primary action, restrained copy.

**Non-Goals:**
- No `ACCESS_BACKGROUND_LOCATION` request or declaration.
- No GPS sampling, route matching subscription, track persistence, crash/session recovery, Live Share, or emergency card implementation.
- No fake location, simulated progress, or fabricated track points.
- No new pre-trip checklist, planning tab, equipment, community, or marketplace surface.

## Decisions

1. Keep permission decision logic testable outside Compose.
   - Rationale: a small reducer can cover request/start/denied states in JVM tests without introducing instrumentation tests or mocks.
   - Alternative considered: put all branching directly in `TrailMateApp`; rejected because it would be harder to test and review.

2. Add a `TrackingServiceLauncher` Android boundary.
   - Rationale: Compose can depend on a simple `start()` / `stop()` interface while Android intent and foreground-service details remain in `services/tracking`.
   - Alternative considered: call `ContextCompat.startForegroundService` directly from the composable; rejected because it couples UI to service mechanics and is harder to test.

3. Request permissions lazily from the Navigation tab.
   - Rationale: users should see why location is needed at the moment they choose to navigate, and denied users can still inspect route details.
   - Alternative considered: request on app launch; rejected because it violates Android permission timing and the TrailMate IA.

4. Treat Android 13+ notification permission as required for starting this foreground-navigation shell.
   - Rationale: a production hiking app must keep the active-navigation notification visible and stoppable; starting a foreground tracking service after notification denial makes the UI safety contract unclear.
   - Alternative considered: start with location permission only and degrade notification visibility; rejected for this slice because there is not yet a richer service-state source or degraded-mode UI.

## Risks / Trade-offs

- [Risk] Users may interpret "start navigation" as full GPS route matching. -> Mitigation: copy says this starts foreground tracking support; GPS sampling and route matching are next slices.
- [Risk] Permission denial could feel like a dead end. -> Mitigation: denied copy preserves route viewing and distinguishes location versus notification permission before continuous navigation starts.
- [Risk] UI may become another preparation checklist. -> Mitigation: one primary action, one secondary stop action, and one concise permission note only.
- [Risk] Starting the service before GPS subscription creates a notification without recorded points. -> Mitigation: this PR keeps copy honest and does not create progress, route match, or track records.
