# TrailMate P0 Background Off-Route Alerts

## Summary

Extend TrailMate's off-route alert path from the visible route screen into the foreground track-recording service so a user who locks the screen or switches apps can still receive off-route notification/vibration while a hike recording is active.

## Motivation

TrailMate's core hiking promise is not just recording movement. During a hike, especially with weak signal, low attention, and many junctions, the app must help the user notice when they are no longer following the planned route. The current off-route alert delivery is tied to the route screen's foreground GPS callback. That is useful while the screen is open, but it does not cover the field case where the user starts recording, locks the phone, and keeps walking.

## Scope

- Use the locally saved imported route geometry as the active target route for the foreground recording service.
- Evaluate reliable service location fixes against that route while recording is active.
- Deliver off-route/rejoined alert decisions through the existing Android alert delivery adapter.
- Keep route-screen visual guidance intact while avoiding duplicate interruptive alerts during active foreground-service recording.
- Keep the feature local-first and deterministic.

## Out of Scope

- Background tracking without a user-started foreground recording service.
- Emergency dispatch, rescue workflows, rerouting, or guaranteed safety claims.
- Cloud sync or server-side live tracking.
- Map tile downloads or offline basemap verification.
- Persisting every alert episode after app restarts.
