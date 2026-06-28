# Change: Add backtrack breadcrumb guidance

## Why

When a hiker is tired, off route, or losing GPS confidence, the safest first question is often not "which feature should I open?", but "can I trust the recorded path to retrace my steps?" TrailMate already records and draws the walked track, but it does not clearly state whether that breadcrumb is long enough and fresh enough to use as a return reference.

## What Changes

- Add a pure backtrack breadcrumb guidance policy based on `TrackRecordingState`.
- Report whether the recorded track is usable, still warming up, stale, paused, or unavailable.
- Surface the guidance in the Route tab safety/diagnostics stack near existing exit guidance.
- Keep copy conservative: the breadcrumb is a reference for retracing the walked path, not a nearest-road, rescue, or turn-by-turn claim.

## Non-Goals

- Turn-by-turn backtracking.
- Nearest road, exit, rescue point, or evacuation-route calculation.
- Automatic emergency contact, rescue dispatch, or background monitoring.
- Changing the foreground track-recording service state machine.
- Replacing offline map, trail markers, visible path, or field judgment.
