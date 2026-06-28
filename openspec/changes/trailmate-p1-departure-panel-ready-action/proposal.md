# Change: Make the departure readiness panel start ready hikes

## Why

The diagnostics Departure readiness panel shows `开始徒步并记录轨迹` when all required readiness checks pass. Its click handler only recognizes the older `开始徒步` label, so the panel can appear actionable while doing nothing.

## What Changes

- Add a small model for mapping Departure readiness primary labels to UI action kinds.
- Use that model in the Route diagnostics Departure readiness panel.
- Start the hike and request track recording when the readiness panel is fully ready.
- Keep existing repair actions for offline route, offline base map, location, and gear.

## Non-Goals

- Changing departure readiness scoring.
- Changing the main route cockpit primary action.
- Changing foreground recording service behavior.
