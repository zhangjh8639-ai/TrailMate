# Change: Safe-exit fix reliability guard

## Why

Safe-exit guidance is safety-facing. TrailMate must not recommend "原路返回" or "前往下一检查点" from a stale, future, or malformed location fix, because that could point the hiker toward an unsafe direction.

## What Changes

- Treat invalid, stale, future, and low-accuracy `HikeLocationFix` values as low-confidence inside `RouteExitGuidanceEngine`.
- Keep existing exit direction recommendations unchanged once a fix is reliable.
- Pass the route screen's current presentation time into exit guidance so freshness is deterministic in tests and UI.

## Non-Goals

- Changing exit-distance heuristics.
- Adding nearest-road or rescue routing.
- Changing the route-deviation state machine.
