# Change: Share departure primary action policy

## Why

TrailMate now has a shared `DepartureReadinessPrimaryActionEngine` for diagnostics actions, but the Route cockpit main action still keeps its own label-to-action mapping. That duplicates safety-critical departure policy and can drift when readiness labels change.

The current fallback also maps unknown departure actions to `RESET_SESSION`, which is misleading for a blocked start state.

## What Changes

- Make the Route cockpit main action use `DepartureReadinessPrimaryActionEngine`.
- Add an explicit blocked Route cockpit primary action kind for unsupported departure actions.
- Preserve existing repair actions for offline route, offline base map, location, system location, and gear.
- Keep active recording controls prioritized over departure repair actions.

## Non-Goals

- Changing departure readiness scoring.
- Changing Route diagnostics panel behavior.
- Changing foreground recording service behavior.
