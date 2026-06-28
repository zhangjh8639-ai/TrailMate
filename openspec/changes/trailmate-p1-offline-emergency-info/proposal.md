# Change: Add offline emergency info sharing

## Why

In weak-signal or offline hiking situations, the hiker needs a concise way to communicate: where they are if GPS is reliable, what route they are on, how far they have progressed, and what the recipient should know about the information boundary.

## What Changes

- Add a local emergency info presentation that can be shared manually from the route safety area.
- Include route, progress, current/next checkpoint, recorded distance, and fresh coordinates when reliable.
- Degrade safely when GPS is stale, inaccurate, missing, or future-dated by omitting coordinate links and saying the coordinates are not reliable.
- Keep wording honest: static point-in-time information only, no live tracking, automatic contact, SMS/WeChat sending, server monitoring, rescue dispatch, or safety guarantee.

## Non-Goals

- Emergency contact database.
- Automatic SOS, background pings, SMS, WeChat API sending, or rescue dispatch.
- Nearest road, nearest exit, or evacuation route calculation.
