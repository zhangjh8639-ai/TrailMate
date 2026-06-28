# Change: Gate secondary track actions on departure readiness

## Why

TrailMate's Route cockpit primary action now blocks departure when required offline route, base map, location, or gear repairs remain. The diagnostics "轨迹记录" button still uses only the recording permission gate, so it can present "开始记录" from a secondary entry point while the main action is correctly asking the hiker to fix departure readiness first.

## What Changes

- Add a small presentation policy for secondary track recording actions.
- Before departure, idle or finished recording start actions are replaced by required departure readiness repair actions.
- Once the hike is already active, starting recording remains available because recording improves field safety.
- Keep existing recording permission/location gate labels once departure readiness is complete.
- Preserve active recording controls: pause and resume remain available even if readiness evidence changes later.

## Non-Goals

- Changing the departure readiness scoring policy.
- Changing foreground service recording behavior.
- Adding new screens or dialogs.
- Blocking pause/resume for already-active recordings.
