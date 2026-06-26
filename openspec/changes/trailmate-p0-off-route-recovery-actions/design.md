# TrailMate P0 Off-Route Recovery Actions Design

## Product Behavior

When the user is reliably off route, TrailMate should make the conservative path clear:

1. Stop moving and confirm the situation.
2. Return to the nearest planned route segment along visible safe ground.
3. Share current location if the GPS fix is accurate enough.

When location accuracy is poor, TrailMate should not claim a precise deviation distance. It should instead ask the user to wait for stable positioning before advancing checkpoints.

When the user has rejoined the route after a recent deviation, TrailMate should confirm the rejoin and shift the action back to continuing navigation.

## Technical Design

Extend `RouteDeviationRecoveryPresentation` with an `actions` list. Each action has:

- `kind`: stable semantic type for UI and future analytics.
- `label`: short Chinese action title.
- `value`: field instruction or evidence.
- `emphasized`: whether the row should receive stronger visual treatment.
- `enabled`: whether the action is currently possible.

The engine remains pure and deterministic. The UI renders the action list in the existing recovery panel, beneath details and above the primary button. The primary button behavior is unchanged so no fake reroute, nearest exit, or emergency workflow is introduced.

## Safety Notes

The action text must avoid promising automatic rescue or correct rerouting. The current app can project the user's position onto a GPX route, but it does not know roads, trail closures, exits, or terrain hazards. The wording should say "沿安全可见路径返回" rather than "直线返回" or "最近出口".
