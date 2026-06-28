# Change: Field watch action visibility

## Why

TrailMate's route page includes field safety watch cards for daylight and progress risk. Some card labels are manual guidance rather than executable app actions. Buttons must only appear when tapping them performs a real safety action, otherwise hikers can mistake guidance copy for an available workflow.

## What Changes

- Add a shared field-watch button policy for safety watch cards.
- Show a button only when the card's primary action requires safety sharing and has a non-blank label.
- Keep manual review/rest suggestions as guidance text instead of tappable controls.

## Non-Goals

- Changing the watch scoring engines.
- Adding new safety-share providers.
- Adding route rerouting, automatic rescue, or automatic contact flows.
