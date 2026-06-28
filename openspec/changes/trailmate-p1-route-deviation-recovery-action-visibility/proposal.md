# Change: Route deviation recovery action visibility

## Why

Route-deviation recovery is a safety-critical field state. Its primary button should run a clear executable action: acknowledge rejoin, refresh location, or share current location. The route page previously selected the action by comparing the Chinese label text, which is fragile and can drift from the real action.

## What Changes

- Add a route-deviation recovery button presentation policy.
- Route recovery primary buttons through explicit action kinds.
- Fall back to a fresh-location action when a share label is present but share text is unavailable.

## Non-Goals

- Changing deviation detection thresholds.
- Changing safety-share text generation or delivery.
- Adding map navigation or route recalculation.
