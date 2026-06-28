# Change: Return ETA action visibility

## Why

Return ETA guidance can show labels that are either real safety-share actions or passive status guidance. Labels such as "开始徒步后计算" and "继续观察" should not look tappable when the route page has no executable action behind them, but they should remain visible as context for the hiker.

## What Changes

- Add a return-ETA button presentation policy.
- Show return-ETA buttons only when the primary action requires safety sharing and has a non-blank label.
- Keep non-share return-ETA labels visible as non-clickable guidance text.

## Non-Goals

- Changing return ETA calculations.
- Adding save-recap or route-assessment navigation.
- Changing safety-share delivery.
