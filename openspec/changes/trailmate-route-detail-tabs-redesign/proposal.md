# Proposal: Route Detail Tabs Redesign

## Why

The route detail page now has four useful tabs, but the product hierarchy is still not production-grade. The route tab in particular mixes AMap SDK consent, map diagnostics, GPS status, notification permission, offline route packs, recording, safety share, and field navigation into one surface.

Users need a clean hiking product flow: assess the route, prepare the plan and gear, then use a map-first light-navigation cockpit in the field.

## What Changes

- Reframe the four route-detail tabs around distinct user questions: suitability, field navigation, trip plan, and gear readiness.
- Move AMap privacy consent into a first-use map/location preparation flow instead of hiding it inside route-tab diagnostics.
- Redesign the route tab as a map-first cockpit with one primary action and user-facing map status language.
- Move SDK diagnostics, package/SHA1/key checks, and internal launch readiness out of the normal user route tab.
- Upgrade plan and gear tabs into production-style trip timeline and route gear checklist surfaces.

## Out Of Scope

- Full turn-by-turn navigation.
- Automatic GPX rerouting.
- Cloud track upload or server-side correction.
- Emergency rescue or guaranteed safety flows.
- Gear marketplace, affiliate purchase, or brand recommendation monetization.
