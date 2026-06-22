# TrailMate Physical Device Field QA

Date: 2026-06-20
Scope: Android phone, real GPS, AMap online base map, GPX route import, light navigation, foreground track recording.

Companion runbook: `docs/qa/trailmate-physical-device-map-gps-runbook.md`.

## Release Rule

TrailMate must not be described as production-ready for outdoor use until this checklist is executed on at least one physical Android device with the release package name, release signing SHA1, and production AMap key.

The release gate must use a structured field QA record, not a loose manual pass/fail flag. The record must include device identity, route distance, route point count, duration, screen-lock recording, background recording, notification controls, weak-signal behavior, battery start/end, safety-share behavior, crash/ANR state, and recording-loss state.

## Test Route

- Use a GPX route with at least 2 km distance and 100+ route points.
- Prefer a route with open sky, trees/buildings, one intentional off-route segment, and at least one weak-signal area.
- Start with battery at 80% or higher.
- Record device model, Android version, app version, package name, signing SHA1, AMap key environment, weather, and network carrier.

## 30-Minute Walk Protocol

1. Import the GPX route and confirm route distance, ascent, route point count, checkpoints, and gear suggestions.
2. Grant Android location with `精确位置` enabled; if only approximate location is granted, confirm TrailMate keeps requesting precise location and does not show GPS as ready.
3. Open the route tab and wait for AMap online tiles, route polyline, checkpoints, and current-location marker.
4. Turn Android system location off once and confirm TrailMate shows `打开系统定位`, opens the system location settings, and does not show `开始徒步`.
5. Turn Android system location back on.
6. Return to TrailMate and confirm it automatically enters location calibration without requiring a second tap.
7. Confirm the route cockpit does not show `开始徒步` while GPS is still searching or accuracy is weak; wait for a stable fix.
8. Confirm track recording does not show `开始记录` while location is searching, missing accuracy, low accuracy, or approximate-only permission.
9. Start navigation and track recording after a stable fix.
10. Walk 10 minutes with screen on.
11. Lock the screen for 5 minutes and confirm foreground notification remains visible.
12. Unlock, pause recording for 2 minutes, then resume after confirming location remains reliable.
13. Walk 8 minutes with the app in background.
14. Return to foreground and confirm the route progress, track point count, and notification state.
15. Intentionally move at least 50 m away from the planned route, then return to route.
16. Finish recording and verify the Data tab review.

## Pass Criteria

- GPS permission request does not block onboarding completion.
- Approximate-only Android location permission is not accepted as outdoor GPS readiness or foreground track-recording readiness.
- Android network location is not accepted as a substitute when the GPS provider is disabled.
- AMap online base map loads or shows a clear slow-loading/fallback message while route actions remain usable.
- Route polyline and checkpoints stay visible after tiles load.
- Current location is shown with a meaningful accuracy state.
- Low-accuracy or accuracy-missing map markers are labeled `大致位置`, not `当前位置`.
- Location fixes older than 60 seconds are shown as stale or needing calibration, and are not accepted for `开始徒步`, `开始记录`, or a precise `当前位置` map marker.
- Location fixes older than 60 seconds do not advance checkpoints, complete the route, or present route-progress/off-route guidance as reliable evidence.
- If Android system location is disabled after permission is granted, TrailMate shows `打开系统定位` and keeps departure readiness blocked.
- After returning from system location settings with providers enabled, TrailMate starts location calibration automatically.
- The route cockpit does not allow `开始徒步` while location is searching, missing accuracy, or low accuracy.
- The route cockpit does not allow `开始徒步` when the target AMap offline base-map region covers the route but airplane-mode tile proof has not been captured.
- Track recording does not allow `开始记录` or `继续记录` while location is searching, missing accuracy, or worse than 50 m.
- If the foreground recording service is triggered while precise location permission is missing or system location providers are disabled, it does not leave the local recording state as active recording.
- If Android rejects the foreground service location type or fails the GPS update subscription, TrailMate keeps idle recording idle or pauses an already-active recording instead of persisting `RECORDING`.
- If recording is already active and accuracy degrades, the field status says it is waiting for stable location instead of claiming every point is reliable.
- Foreground recording notification appears within 5 seconds of starting recording.
- Track recording continues during screen-off and background intervals.
- Pause, resume, and finish actions work from the app UI and notification.
- Track point count increases during movement and stops increasing while paused.
- After resuming from pause, the track does not backfill last-known locations from the paused interval.
- Finished track appears in Data with distance, point count, and saved status.
- Finished track review appears only when the recording has at least two points and non-zero movement distance.
- Off-route state warns the user without auto-advancing checkpoints incorrectly.
- Off-route recovery copy asks for location calibration instead of presenting a precise deviation distance when accuracy is worse than 50 m.
- Safety share text includes route name and current location when a reliable fix exists.
- Safety share copy describes a current static position and must not imply live tracking, rescue, or emergency monitoring.
- Safety share does not generate a coordinate link when accuracy is missing or worse than 100 m; it asks the user to wait for a more reliable fix.
- Battery drain over 30 minutes is recorded and judged acceptable for the test device.

## Weak-Signal And Offline Checks

- In a weak-signal area, the app shows low-accuracy/searching state instead of pretending location is precise.
- With airplane mode enabled after the route is open, GPX route geometry remains visible.
- With airplane mode enabled, AMap online tiles may be unavailable; the app must not claim true offline base maps unless AMap offline map manager proof exists.
- After downloading the target AMap offline region, enable airplane mode, reopen the route, and save screenshots proving base-map tiles still render around the active route.
- AMap diagnostics must stay pending with `断网瓦片` marked for verification until the airplane-mode tile proof has been captured.
- Departure readiness must stay pending with a `飞行模式验证底图` action until the airplane-mode tile proof has been captured.
- Use `我已断网并看到底图` only while the phone is offline and the active route map still shows base-map tiles; this proof is valid only for the current route key and target adcode, or the target city/province name when adcode is unavailable.
- Re-enable network and confirm the map recovers without losing the active recording.

## Failure Conditions

- App crashes, ANRs, or loses active recording.
- Foreground service stops while the hike is still active.
- Notification controls do not match recording state.
- Paused-window movement is appended after resume through Android last-known location.
- Route actions are blocked by map tile loading.
- The app labels a saved GPX route pack as an offline base map.
- AMap diagnostics shows `可真机验证` before airplane-mode offline tile proof exists.
- The route cockpit shows `开始徒步` before airplane-mode offline tile proof exists.
- The app records offline tile proof while Wi-Fi or cellular network is still active.
- The app records offline tile proof before the current route session has loaded visible AMap base-map tiles.
- Offline tile proof recorded for one route or city unlocks another route or city.
- The release gate accepts offline tile evidence that is not backed by saved proof for the current route and target region.
- The release gate accepts physical-device QA evidence without a structured field protocol record.
- Emulator-only evidence, a walk shorter than 30 minutes, missing lock/background recording proof, excessive battery drain, crash/ANR, or recording loss satisfies physical-device release evidence.
- Location accuracy, route deviation, or safety sharing presents uncertain data as reliable.
- A low-accuracy or accuracy-missing map marker is labeled as precise `当前位置`.
- A stale location fix older than 60 seconds is accepted as reliable for route start, track recording, or precise map position.
- A stale location fix older than 60 seconds advances checkpoints, completes the route, or drives off-route recovery copy.
- System location disabled is treated as `开始徒步` or only repeats permission prompts instead of opening system location settings.
- Returning from system location settings requires a second tap before TrailMate tries location again.
- Approximate-only Android location permission starts GPS/navigation/recording, or uses the network provider as proof of outdoor GPS.
- The foreground recording service enters `RECORDING` without precise location permission, an enabled GPS provider, or a successful GPS update subscription.
- A service rejection from idle recording is dropped by the UI instead of clearing the active-recording state.
- Active recording with weak or missing accuracy is described as fully reliable track capture.
- A finished recording with one point or zero movement distance is presented as a saved reviewable track.
- Off-route recovery presents a precise deviation distance while current accuracy is worse than 50 m.
- Safety share is labeled as realtime tracking, rescue, or emergency monitoring while it only sends static Android share text.

## Evidence To Save

- Screenshots: route tab before start, full-screen navigation, notification, pause/resume, off-route warning, finished Data review.
- Screenshots: airplane-mode route map with visible offline base-map tiles and the `断网瓦片` proof state after recording.
- Evidence folder captured with `tools/qa/collect-trailmate-device-evidence.ps1` before and after the walk.
- Copied TrailMate diagnostics report with device identity, package/SHA1, location recovery action/steps, offline-download recovery action/steps, and `offlineBaseMapReason`.
- `adb bugreport` or logcat around any crash, ANR, foreground-service warning, or permission failure.
- Exported/serialized recorded track data if available.
- Battery percentage before and after the 30-minute protocol.
