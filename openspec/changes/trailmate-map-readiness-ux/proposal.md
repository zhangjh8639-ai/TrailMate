# Change: TrailMate Map Readiness UX

## Why

TrailMate is moving from prototype visuals toward a production Android outdoor app. The light-navigation map currently looks like a map, but it is a fixed illustration and does not reflect imported GPX geometry. That weakens trust when users import real routes and expect navigation context.

AMap is the production map provider path for China. SDK integration requires a valid platform Key, signing SHA1/package setup, privacy consent ordering, and lifecycle handling. The app should link the SDK and own the lifecycle, but it must not ship a fake provider or committed secret while project-owned Key inputs are absent.

## What Changes

- Draw the light-navigation route from `ImportedRoute.routePoints` when geometry exists.
- Keep a local Canvas fallback for routes without geometry.
- Add a testable map readiness state that distinguishes local GPX preview from AMap-backed production maps.
- Show map readiness in the route screen so users know whether they are viewing local preview, offline-ready navigation, or a production map provider.
- Link the AMap SDK with a pinned Maven version and render AMap `MapView` only after Key, SDK availability, and privacy consent gates pass.
- Document remaining AMap production prerequisites without adding keys in source control.

## Impact

- Improves trust and route fidelity immediately for imported GPX files.
- Adds the AMap SDK seam and lifecycle while preserving local preview when Key/privacy setup is incomplete.
- Does not change GPS foreground tracking behavior or claim background navigation support.
