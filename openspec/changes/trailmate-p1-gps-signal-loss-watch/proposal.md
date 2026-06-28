# Change: Add GPS signal loss watch

## Why
During a hike, the user may believe TrailMate is still navigating and recording while the latest usable device location has stopped updating. This is risky in low-signal valleys, dense tree cover, battery-saving states, or after Android provider interruption because distance, deviation, and safety sharing may all be based on stale evidence.

## What Changes
- Add a GPS signal-loss watch policy for active track recording.
- Warn when the latest location snapshot is stale while recording is active.
- Warn more strongly when recording is active but location permission, provider, or availability is blocking fresh fixes.
- Show the warning in the Route tab and fullscreen navigation dock without adding another navigation mode or GPS toggle.
- Keep copy advisory: it asks the hiker to stop, refresh location, and verify offline map, trail markers, and visible path.

## Non-Goals
- Turn-by-turn navigation.
- Background rescue monitoring.
- Automatic emergency contact or dispatch.
- Replacing off-route alerts, offline maps, or field judgment.
- Changing the foreground service recording state machine.
