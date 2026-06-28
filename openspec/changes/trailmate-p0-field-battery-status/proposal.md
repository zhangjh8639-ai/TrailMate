# TrailMate P0 Field Battery Status

## Why

Low battery is a field safety risk for outdoor hiking navigation. TrailMate already summarizes GPS, track recording, offline basemap, and lock-screen notification readiness in the route cockpit, but it does not expose live battery state in the same field status surface. Users need a concise warning before relying on navigation or recording during a low-power hike.

## What Changes

- Add a lightweight battery status model for route field readiness.
- Show battery state in the route cockpit field status summary.
- Prefer conservative Chinese copy when battery is low or critically low.
- Read current Android battery percent from the system battery broadcast on the route detail screen.

## Out of Scope

- Blocking hike start solely on battery level.
- Battery optimization exemption flows.
- Long-term battery drain analytics.
