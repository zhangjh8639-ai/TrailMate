# Change: Add return ETA watch

## Why

TrailMate's safety loop should help a hiker and their safety contact reason about whether a trip is still within the expected return window. Safety sharing already includes an expected finish time, but the route screen does not actively surface whether the current hike is on time or overdue.

## What Changes

- Add a deterministic return-ETA watch based on recording start time and planned route duration.
- Surface a route safety card with normal, warning, overdue, missing-duration, and finished states.
- Escalate overdue trips to fresh safety sharing.
- Keep the feature local and explicit: no background alarm, no SMS, no emergency dispatch, and no server sync in this slice.

## Non-Goals

- No contact database.
- No automatic message sending.
- No server-side monitoring.
- No background notification scheduling.

## Verification

- Unit tests cover not-started, active, warning, overdue, missing-duration, and finished states.
- Android unit tests pass.
- `openspec validate --all --strict` passes.
