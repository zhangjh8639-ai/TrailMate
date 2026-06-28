# TrailMate P1 Safety Share Fix Age

## Why

Safety sharing is only useful when the shared position is current enough for a contact to act on it. TrailMate already refuses to share missing or inaccurate coordinates, but it does not distinguish a fresh GPS fix from an old cached fix. In the field, sending a stale location can mislead safety contacts during low-signal or paused-location situations.

## What Changes

- Add location timestamp freshness to the safety-share input model.
- Refuse to generate share text when the location timestamp is missing, invalid, or older than the safety-share freshness window.
- Pass the Android location fix timestamp from the route screen into the safety-share engine.

## Out of Scope

- Real-time tracking links.
- Server-side emergency contacts.
- Background safety pings.
- Changing the existing accuracy threshold.
