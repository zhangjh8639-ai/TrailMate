# Change: Route monitor future fix guard

## Why

The background route monitor treats recorded GPS points as input for off-route alerts. A point with a timestamp in the future can happen when device time, provider timestamps, or replayed samples are inconsistent. TrailMate must not treat such a point as reliable field evidence or trigger a wrong-turn alert from it.

## What Changes

- Treat future or non-positive recorded point timestamps as unreliable for route-deviation monitoring.
- Keep the existing stale-point behavior: wait for a reliable fix instead of sending off-route or rejoined notifications.

## Non-Goals

- Changing off-route distance thresholds.
- Changing alert cooldown or escalation timing.
- Changing foreground recording persistence.
