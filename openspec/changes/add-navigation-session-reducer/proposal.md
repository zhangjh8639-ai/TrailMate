## Why

TrailMate now has route projection, progress, off-route evidence, and a snapshot engine, but there is no pure runtime reducer that can safely carry navigation session state across GPS samples and user actions. Adding this layer now creates a testable bridge before wiring real GPS, foreground service, persistence, or UI.

## What Changes

- Add a side-effect-free navigation session reducer that accepts a current runtime state and an action.
- Support starting/ending navigation, processing location samples, pausing/resuming, entering original-return mode, and clearing off-route evidence after returning to route.
- Preserve previous off-route evidence and route progress so sustained deviations and route matching remain stable across samples.
- Keep privacy, storage, GPS provider, foreground service, map, and UI out of scope.

## Capabilities

### New Capabilities
- `navigation-session-reducer`: Pure reducer for active navigation runtime state and user/location actions.

### Modified Capabilities
- None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/geo/` and unit tests under `app/src/test/java/com/trailmate/app/core/geo/`.
- No new runtime dependencies.
- No Android permissions, services, storage, network, or UI changes in this slice.
