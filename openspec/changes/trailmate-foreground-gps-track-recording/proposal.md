# Proposal: Foreground GPS And Track Recording

## Summary

Add foreground GPS positioning and local track recording to the Android prototype so an active hike can show current location status, advance light-navigation checkpoints from accurate fixes, and preserve the user's recorded track on device.

## Why

The current app imports GPX and simulates light navigation, but it cannot use real device location or record a user's actual path. This limits the usefulness of the Route tab during a hike and prevents post-hike evidence from improving future capability estimates.

## Scope

In scope:

- Runtime foreground location permission.
- Foreground-only Android location updates while the Route screen/session is active.
- Imported route geometry retained from GPX for route-relative projection.
- Local track recording with start, pause, resume, finish, point count, elapsed time, and distance.
- Local persistence of the latest recorded track.
- Clear UI language that this remains light route following.

Out of scope:

- Background GPS tracking.
- Turn-by-turn navigation.
- Voice prompts.
- Emergency dispatch or guaranteed deviation detection.
- Cloud sync for recorded tracks.
