# Change: Off-route recovery primary action priority

## Why

When a hiker is off route, TrailMate's primary action must guide them back to recovery guidance before routine recording controls. In the field, "pause recording" is less important than stopping automatic progress and showing the recovery checklist.

## What Changes

- Prioritize `VIEW_RECOVERY` as the route cockpit primary action during an active or paused hike when route guidance reports `CHECK_ROUTE`.
- Keep the same priority during an active or paused hike when the user has recently rejoined after a deviation and still needs to acknowledge recovery guidance.
- Preserve existing departure readiness, GPS authorization, and finished-track review behavior outside the in-field off-route/recent-rejoin state.

## Non-Goals

- Adding a secondary action model for recording controls.
- Changing route-deviation thresholds.
- Changing the recovery panel content.
