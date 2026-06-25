# Production Outdoor Readiness Design

## Flow Contract

TrailMate's Android flow is:

1. Onboarding collects profile inputs and prepares map/location permissions.
2. Home asks what route the user is preparing for today.
3. Route workspace imports or replaces GPX.
4. Route detail opens assessment first.
5. Route cockpit shows map, checkpoints, readiness, and route actions.
6. Full-screen navigation hides app chrome and supports walking/recording.
7. Data reviews completed tracks and manages historical activities.

Historical activity/profile evidence is supporting material for assessment. It should be available for user control, but it should not dominate field-use screens.

## Map Readiness

AMap MapView is allowed only after:

- Android Key exists;
- SDK is linked;
- map/privacy consent is accepted;
- route geometry has at least two points.

Even then, a production surface needs an explicit loading or slow-network state. The app must not display a blank gray rectangle without explanation. If AMap is slow or unavailable, the user still gets the local GPX route preview, route metrics, checkpoints, and track recording controls.

## GPS And Recording

Foreground location and track recording remain Android-native:

- foreground location permission for map/route matching;
- foreground service with `foregroundServiceType="location"` for recording;
- persistent notification with pause/resume/finish controls;
- local persisted recording state.

The app may claim "recording works on emulator" only after service and notification proof. It may claim "production outdoor ready" only after physical-device field QA.

## Safety Boundaries

TrailMate is route assistance, not turn-by-turn navigation or rescue. Safety sharing can send route/location context through Android share sheet, but live tracking is out of scope until a server-backed link is designed.
