# Change: Departure brief share refresh action

## Why

The fullscreen route helper can show a departure-brief share button before a hike starts. The panel already generates a static route plan and expected return time, but the click handler used the share text captured when the panel was rendered. If the hiker leaves the screen open before sending, the brief can contain an outdated planned departure and expected finish time.

## What Changes

- Add an explicit departure-brief share action resolver.
- Recompute departure-brief share text at click time.
- Keep the existing panel layout and visibility policy.

## Non-Goals

- Adding GPS coordinates to departure briefs.
- Changing departure readiness gating.
- Changing the departure brief visual design.
