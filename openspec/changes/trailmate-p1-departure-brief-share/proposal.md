# Change: Add manual departure brief sharing

## Why

TrailMate already supports current-position safety sharing once GPS is fresh. Before entering weak-signal terrain, a hiker also needs a simple way to tell a trusted person: which route they are taking, how long it should take, and when to start checking in if they have not returned.

## What Changes

- Add a local route-plan departure brief that can be shared manually before or during a hike.
- Include route name, distance, ascent, planned/actual start time, expected finish, and a +60 minute confirmation rule.
- Make the safety boundary explicit: this is static itinerary text, not live tracking, automatic rescue, SMS, WeChat, or server monitoring.
- Add a compact entry in the route safety area.

## Non-Goals

- Emergency contact storage.
- Automatic alerts, background checks, SMS, WeChat API sending, or rescue dispatch.
- Server-backed live tracking links.
