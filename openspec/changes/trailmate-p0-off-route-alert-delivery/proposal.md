# Add P0 off-route alert delivery

## Summary

Deliver urgent off-route alert decisions through Android notification and vibration hooks when the Route tab receives a new GPS route-check decision that requests user attention.

## Motivation

TrailMate already converts reliable off-route GPS states into deterministic alert decisions and shows them in the route GPS panel. Field users also need a physical interruption when they may be walking away from the planned route, especially at forks or while looking away from the screen. This change bridges the pure alert policy to Android delivery without changing route thresholds or claiming full background navigation.

## Scope

- Add a pure Kotlin delivery model that maps `RouteDeviationAlertDecision` to notification, vibration, or in-app-only outcomes.
- Add Android notification/vibration delivery for new route alert decisions emitted from the Route tab GPS snapshot path.
- Add the required non-runtime vibration permission.
- Keep delivery copy Chinese, action-oriented, and safety-bounded.

## Out Of Scope

- Background off-route detection while the Route tab is not receiving route geometry and GPS snapshots.
- Passing imported route geometry into `TrackRecordingForegroundService`.
- Voice prompts, rerouting, rescue dispatch, or guaranteed safety claims.
- Server persistence or cloud push notifications.
