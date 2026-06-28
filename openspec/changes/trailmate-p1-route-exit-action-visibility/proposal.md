# Change: Route exit action visibility policy

## Why

The route-exit guidance panel contains two kinds of content: safety recommendations such as returning along the route, and immediate repair actions such as refreshing location. These must not blur together in the field UI. A recommendation should be shown as guidance, while only executable repair actions should render as buttons.

## What Changes

- Add a feature-level button visibility policy for the Route exit guidance panel.
- Keep reliable `READY` exit guidance as recommendation content without a primary button.
- Show the primary button only for `CAUTION` guidance with a non-blank action label.

## Non-Goals

- Adding a new route-exit navigation mode.
- Changing the Route exit guidance recommendation engine.
- Changing off-route recovery or safety-share behavior.
