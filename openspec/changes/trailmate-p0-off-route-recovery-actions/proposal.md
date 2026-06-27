# TrailMate P0 Off-Route Recovery Actions

## Summary

Improve the route-deviation recovery experience so a reliable off-route alert gives the hiker a compact set of field actions: stop and confirm, return to the nearest planned route segment, and share current location when available.

## Motivation

TrailMate's first responsibility is not only to detect that the user is off route, but to help them avoid making the situation worse. The current recovery card explains the problem, but it reads more like guidance text than a decision surface. In low-signal outdoor use, the next action must be obvious and conservative.

## Scope

- Add deterministic recovery action data to the existing recovery presentation model.
- Show actions only from evidence TrailMate already has: GPS fix, route projection, route progress, location accuracy, and safety-share availability.
- Keep the copy Chinese-first and field-oriented.
- Preserve the existing safety-share and continue-navigation primary actions.

## Out of Scope

- Claiming nearest road, exit, shelter, rescue, or reroute recommendations.
- Downloading or querying POI/road network data.
- Emergency dispatch or live tracking.
- Changing the route map layout or fullscreen navigation layout.
