# TrailMate Route Cockpit Design

## Overview

The route cockpit is the production shape of TrailMate's light navigation surface. It is not a new navigation engine. It is a focused presentation layer over the app's current route, GPS, tracking, readiness, assessment, and gear state.

## User-Facing Shape

The "路线" tab should open with:

- Route identity and segment tabs.
- A large AMap route surface.
- Current checkpoint and next checkpoint overlays.
- Route progress by distance and ascent.
- GPS/route-match state.
- One state-driven primary action.
- A compact readiness strip for GPS, track recording, offline map, and gear.

Secondary details are still available below the cockpit:

- AMap setup and diagnostics.
- GPS reliability details.
- Departure readiness detail.
- Safety share details.
- Track review and export.

## State Model

The implementation may introduce a small presentation model that maps existing domain state to UI state:

- `primaryAction`: enable location, start hike, pause, resume, finish, review track, or view recovery advice.
- `gpsStatus`: available, weak, disabled, denied, or unknown.
- `recordingStatus`: not started, recording, paused, finished.
- `routeMatchStatus`: on route, uncertain, off route.
- `readinessItems`: GPS, recording, offline map, gear.
- `checkpointSummary`: current checkpoint, next checkpoint, distance, ascent, and progress.

This model should not duplicate route assessment logic. It should only compose existing results into screen-ready labels, statuses, and actions.

## Interaction Rules

- Before location permission, the primary action requests GPS permission.
- Before recording, the primary action starts the hike session and track recorder.
- During recording, the primary action pauses recording, with secondary actions for marking checkpoint and ending.
- During paused state, the primary action resumes recording.
- When off-route, the route cockpit surfaces recovery advice and keeps safety share visible.
- After completion, the primary action opens track review.

## Visual Rules

- Use the existing TrailMate moss/field/mist palette.
- Use amber only for caution states, red only for hard blockers.
- Keep the map prominent and avoid nested cards.
- Keep diagnostic copy out of the first viewport unless it blocks map, GPS, or recording.
- Match the Chinese UI direction from the provided references: direct, short labels and action-first surfaces.

## Non-Goals

- Do not add full turn-by-turn navigation.
- Do not silently reroute imported GPX routes.
- Do not upload recorded tracks for cloud correction.
- Do not show raw AI prompts, user evidence bundles, or historical import details in the route cockpit.
- Do not claim safety guarantees or rescue behavior.

## Rollout

1. Add route cockpit presentation tests.
2. Extract route tab sections into smaller composables where needed.
3. Replace the current first-viewport route tab stack with cockpit-first layout.
4. Move diagnostics/readiness detail below expandable status sections.
5. Add UI smoke verification.
