# TrailMate P0 Background Off-Route Alerts Design

## Product Behavior

When a user starts a hike recording from an imported route, TrailMate already runs a foreground location service and shows a persistent recording notification. This change lets that service also evaluate route deviation using the route geometry captured for the active recording.

If a reliable service fix is far from the planned route, TrailMate posts the same Chinese off-route alert and vibration plan used by the route screen. If the user returns to the route, TrailMate may post a rejoined-route confirmation without vibration. If GPS is weak, stale, missing, or the route geometry is not available, the service must not claim a precise off-route distance.

The visible route screen remains responsible for in-app banners and recovery copy. While the foreground recording service is active, the route screen should avoid sending duplicate notification/vibration delivery for the same route-fix stream.

## Technical Design

### Pure route monitor

Add a pure `TrackRecordingRouteMonitorEngine` in `core.model`. It consumes:

- the locally saved `ImportedRoute?`;
- the recording route name;
- the recording route key, when available;
- a `RecordedTrackPoint`;
- the current `RouteDeviationAlertState`;
- the current clock time.

The engine validates that the imported route matches the recording route key, falling back to route name only for legacy recordings, and has at least two route points. The route key includes a geometry fingerprint so a same-name replacement with matching summary metadata but different coordinates does not pass identity checks. The engine also rejects stale fixes using the shared 60-second reliability window before claiming a precise off-route or rejoined status. For fresh reliable points, it projects the recorded point onto the route using `RouteGeometryEngine.projectToRoute`, derives `LocationBackedHikeStatus.CHECK_ROUTE` when the cross-track distance exceeds the same 75 m field threshold used by light navigation, otherwise `ON_ROUTE`, then delegates notification decision behavior to `RouteDeviationAlertPolicy`.

If there is no matching route geometry, it returns a `NONE` decision with a reset alert state. This prevents an old off-route episode from leaking across route changes.

### Foreground service integration

`TrackRecordingForegroundService` already loads the local snapshot, appends usable fixes, and publishes recording updates. This change extends the append path:

1. Load the snapshot once.
2. Convert the Android `Location` into `RecordedTrackPoint`.
3. Append the point through `TrackRecordingEngine`.
4. If the point was accepted and recording remains active, evaluate the route monitor with the route captured for the recording, falling back to `snapshot.importedRoute` only when it still matches the recording route key.
5. Deliver the decision through `RouteDeviationAlertAndroidDelivery`.
6. Save and publish the updated recording as before.

The service keeps an in-memory `RouteDeviationAlertState` and route geometry reference scoped to the active service instance. It resets when recording pauses, finishes, or the monitored route key changes. If the user imports another route while recording, the service keeps monitoring the originally captured route. If the process restarts and the original geometry is no longer available locally, it records the track but avoids precise route-deviation alerts rather than evaluating against the wrong same-name geometry.

### Duplicate-delivery guard

The route screen can still compute and display route deviation state. However, when `TrackRecordingStatus.RECORDING` is active, the foreground service owns interruptive notification/vibration delivery. Add a small pure policy so this ownership rule is testable and the Compose route code stays readable.

## Risks And Limits

- Android may still throttle or stop the service if the OS kills the app; this change does not claim guaranteed background survival.
- Because the alert state is in-memory, app process death resets the service alert cooldown after restart.
- The service only monitors the locally saved imported route. If a route has not been saved or has insufficient geometry, it records the track but does not issue precise route-deviation alerts.
