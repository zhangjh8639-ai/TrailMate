# Off-Route Alert Policy Design

## Product Behavior

When GPS route matching reports `CHECK_ROUTE` from a reliable fix, TrailMate should interrupt the field flow once with a clear warning: stop, check the route, and avoid blindly continuing. If the same off-route episode remains stable, the app should stay in recovery guidance without repeating alerts for every GPS fix. If the cross-track error grows meaningfully, TrailMate should re-alert because the user is likely moving farther away from the intended trail. Once a reliable `ON_ROUTE` fix arrives after an active off-route episode, TrailMate should emit a single rejoined confirmation and clear the episode.

## Technical Approach

Create `RouteDeviationAlertPolicy` as a pure Kotlin model in `android-app/src/main/java/com/trailmate/app/core/model/`. The policy takes:

- `LocationBackedHikeStatus`
- optional `HikeLocationFix`
- existing `RouteDeviationAlertState`
- `nowEpochMillis`

It returns `RouteDeviationAlertDecision` with the alert kind, notification/vibration intent, localized copy, primary action label, and next state. This keeps field-alert decisions testable without Android framework dependencies. Android notification and vibration wiring can later consume `shouldNotify` and `shouldVibrate`.

## State Model

`RouteDeviationAlertState` stores only what the policy needs for episode control:

- whether an off-route episode is active
- last alert timestamp
- last alerted cross-track error
- whether a rejoin confirmation has already been emitted for the cleared episode

## Policy Constants

- Suppress repeated alerts for the same episode for 120 seconds.
- Re-alert within the cooldown when cross-track error increases by at least 50 m from the last alerted distance.
- Treat fixes with horizontal accuracy above 50 m as unreliable for alerting.

These values align with the existing GPS reliability and route-check thresholds while keeping user interruption conservative.

## Safety Boundaries

The policy never claims automatic rescue, guaranteed safety, or rerouting. It prompts the user to stop and verify the route using the map, trail signs, and visible terrain. Low-accuracy and missing-fix states must not fire off-route alerts.
