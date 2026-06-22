# Proposal: TrailMate Production Mobile UX System

## Summary

Redesign the Android client information architecture and screen responsibilities so TrailMate feels like a production hiking app instead of a feature prototype. The work keeps the current product direction: personalized route assessment, AI-assisted gear preparation, light navigation, GPS recording, and offline-first field use.

## Problem

The current Android app has valuable capabilities, but several screens carry too many roles:

- Home mixes route start, route assessment, data summaries, and broad shortcuts.
- Route detail mixes field cockpit, diagnostics, map setup, assessment, plan, GPS, safety, and gear details in one large file.
- Evidence inputs such as profile, historical GPX, and body metrics risk appearing as product content instead of staying behind the assessment.
- Settings, data control, historical evidence, and profile management are not yet shaped as a production account/privacy area.

## Goals

- Define one primary task for each bottom tab and route-detail tab.
- Keep internal evidence and AI inputs out of primary UI surfaces.
- Preserve field-critical route cockpit capabilities while reducing diagnostic clutter.
- Establish a visual system aligned with the user's reference screenshots.
- Make the redesign implementable incrementally without breaking GPS, GPX import, gear, or persistence tests.

## Non-Goals

- No server implementation in this change.
- No marketplace, social routes, paid plans, or brand shopping.
- No full turn-by-turn navigation or rescue guarantee.
- No broad rewrite of all screens in one implementation step.

## Evidence

Design source:

- User-provided TrailMate reference screenshots.
- Existing Android code in `android-app/src/main/java/com/trailmate/app/feature`.
- Existing TrailMate specs and route cockpit design docs.
- Outdoor app product patterns from AllTrails, komoot, Gaia GPS, and Strava Beacon.

## Proposed Direction

Use the production UX design spec at `docs/superpowers/specs/2026-06-19-trailmate-production-mobile-ux-design.md` as the guiding design artifact. The implementation should follow an incremental "information architecture first, visual polish second" path.
