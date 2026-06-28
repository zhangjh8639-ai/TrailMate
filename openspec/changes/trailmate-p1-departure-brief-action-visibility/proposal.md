# Change: Hide unavailable departure brief actions

## Why

The Route diagnostics safety stack shows the departure brief panel even when the route cannot produce a sendable departure brief, such as missing estimated duration or a completed trip. Showing a disabled button labeled "查看路线评估" or "查看复盘" suggests a navigation action that is not implemented in the panel.

TrailMate's field UI should avoid redundant or misleading controls, especially in safety-related route views.

## What Changes

- Add a feature-level button presentation policy for the departure brief panel.
- Show the departure brief action only when a sendable share payload exists.
- Keep the explanatory status, caption, and route details visible when the brief cannot be sent.

## Non-Goals

- Adding route assessment navigation from this diagnostics card.
- Changing the departure brief text payload.
- Changing safety share or offline emergency share behavior.
