# Change: Add route direction watch

## Why
Hikers can still be near the route polyline while moving in the wrong direction after a fork, rest stop, or confusing trail junction. Existing off-route alerts catch lateral deviation, but they do not catch reliable on-route progress that is moving backward along the planned route.

## What Changes
- Add a direction-watch policy that compares the previous reliable route-aligned fix with the latest fix.
- Show a route-tab warning when reliable samples indicate the hiker is moving backward along the planned route.
- Suppress direction warnings when GPS is low-confidence, off-route, paused, finished, missing, stale, or only showing small GPS jitter.
- Keep the warning manual and advisory: it asks the hiker to stop and verify map, trail markers, and visible path direction.

## Non-Goals
- Turn-by-turn navigation.
- Compass sensor fusion.
- Automatic rerouting.
- Emergency dispatch or automatic contact.
- Replacing off-route alerts, offline maps, or field judgment.
