# Proposal: Route Fullscreen Navigation Focus

## Why

The route cockpit is directionally useful, but the current first viewport is still dense. It mixes map status, current checkpoint, field readiness, progress, safety actions, and diagnostics disclosure into one visible stack. The route tab should feel like a hiking cockpit, not a settings page.

AMap online map consent is also still reachable from route diagnostics. That makes authorization feel route-specific even though it is a first-use app decision.

Users also need a focused in-field mode that hides normal app chrome and prioritizes navigation, location status, and track recording.

## What Changes

- Reduce the default route cockpit density by keeping only map, checkpoint, one primary action, compact progress, and a full-screen entry in the first surface.
- Remove route-page AMap consent actions; map service consent and foreground location permission are handled during first-use onboarding.
- Add a full-screen route navigation mode focused on current checkpoint, route progress, location status, safety share, checkpoint marking, and track recording.
- Hide bottom navigation and page padding while the full-screen navigation mode is active.

## Out Of Scope

- Turn-by-turn navigation.
- Background map rerouting.
- New server-side authorization.
- A new settings page for re-running map consent.
- Replacing Android's system permission UI.
