# Change: Backtrack breadcrumb action visibility

## Why

Backtrack breadcrumb guidance tells the hiker whether the recorded walked track can be trusted as a return reference, but its primary action label was not rendered or routed in the Route diagnostics panel. In field use, the hiker needs clear actions when the breadcrumb is usable, stale, paused, or unavailable.

## What Changes

- Add a feature-level action policy for the Backtrack breadcrumb panel.
- Route usable or saved breadcrumb actions to track review.
- Route stale breadcrumb actions to location refresh.
- Route paused breadcrumb actions to the existing track recording action when enabled.
- Hide pseudo-actions for warming-up or unavailable breadcrumb states.

## Non-Goals

- Adding turn-by-turn backtracking.
- Changing the track-recording state machine.
- Adding nearest-road or evacuation routing.
- Changing the breadcrumb safety copy.
