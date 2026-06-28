# Change: Add conservative route exit guidance

## Why

TrailMate's hiking value is strongest when it helps a hiker avoid getting lost or making risky decisions under weak signal, fatigue, or changing weather. The route screen already supports navigation, off-route recovery, and safety sharing, but it does not give an always-available answer to: "If I need to stop, should I go back or continue to the next planned reference?"

## What Changes

- Add deterministic route exit guidance based on current progress, planned checkpoints, and GPS reliability.
- Show a compact Chinese safety-exit card in route diagnostics.
- Require reliable GPS before recommending a direction.
- Explicitly avoid claiming nearest road/exit knowledge until offline road or POI data is available.

## Non-Goals

- No rescue dispatch.
- No automatic nearest road calculation.
- No new server dependency.
- No new navigation mode.

## Verification

- Unit tests cover backtrack, next checkpoint, finish, and low-accuracy cases.
- Android unit tests pass.
- `openspec validate --all --strict` passes.
