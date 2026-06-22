# Design: Foreground GPS And Track Recording

## Overview

The feature connects Android foreground location to the existing light-navigation model. Raw GPS fixes are recorded locally, and when the imported route includes geometry, fixes are projected to distance-along-route and cross-track error before they update checkpoint progress.

## Data Model

Imported target route gains sampled route points:

- latitude
- longitude
- elevation meters, optional
- distance along route km

Recorded track stores:

- status: idle, recording, paused, finished
- points with latitude, longitude, optional elevation, horizontal accuracy, timestamp
- started, paused, and finished timestamps
- total distance km

The first implementation persists the latest recording in the local snapshot. Later Room storage can promote this into a route library.

## Android Location

Use the Android framework `LocationManager` for the first production slice. The app requests foreground location permission and starts a `foregroundServiceType="location"` service while track recording is active. On Android 13+, the Route tab also explains and requests notification permission so the foreground service can surface lock-screen/background controls clearly. The service owns GPS sampling and local persistence so recording can continue when the screen is locked or the app is backgrounded after the user starts recording.

The route screen may also observe foreground location for live light-navigation progress, but it does not own persisted track points. The service broadcasts full `TrackRecordingState` payloads back to the UI after each persisted update.

## Route Projection

For each fix, the model layer projects the coordinate onto the nearest route segment using a local equirectangular approximation around the segment midpoint. The output is:

- distance along route km
- cross-track error meters
- horizontal accuracy meters
- timestamp

Low accuracy and far-from-route handling remains delegated to `LocationBackedHikeSessionEngine`.

## UI

The Route tab shows compact Chinese status:

- GPS authorization/status.
- Current accuracy when available.
- Track recording controls backed by the foreground service.
- Track notification permission state and a clear "allow notifications" action when required.
- Recorded distance and point count.
- Safety copy: "仅提供轻导航，不替代路标与离线地图".

## Privacy And Safety

Recorded tracks stay local by default. The app must not claim guaranteed deviation detection or emergency support.
