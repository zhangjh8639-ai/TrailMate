# Route Detail Tabs Redesign

## Overview

This change makes route detail a production user flow instead of a technical capability showcase. The route detail screen keeps four tabs, but each tab receives a single job:

- `评估`: decision.
- `路线`: field cockpit.
- `计划`: trip rhythm.
- `装备`: route checklist.

## First-Use Authorization Strategy

AMap SDK privacy consent should be handled before online map SDK components are initialized. TrailMate should offer a product-level map/location preparation step either after onboarding or before the user's first map-enabled route detail session.

The flow should explain:

- AMap online base maps are optional; local GPX preview remains available.
- Foreground location is used only when the user starts hiking or explicitly chooses to use current location.
- Notification permission is used for ongoing track recording controls.

Android runtime permission prompts should remain action-triggered. The app may explain permissions early, but it should request location when the user starts the hike or chooses to use current location, and request notification permission when the user starts track recording.

## Route Tab Cockpit

The route tab should stop exposing implementation readiness as ordinary content. It should open with:

- a large map surface;
- current/next checkpoint;
- route progress;
- route-match/location status;
- one state-driven primary button;
- compact secondary actions.

AMap-specific details should map to user language:

- `在线底图可用`
- `当前使用本地路线`
- `地图服务需同意后启用`
- `地图不可用，继续使用本地路线`

Detailed SDK diagnostics belong in a settings/developer detail entry, not in the default route detail journey.

## Assessment Tab

The assessment tab should remain decision-first, but it needs more deliberate visual hierarchy and action priority:

- recommended routes prioritize entering the route;
- caution routes prioritize gear/plan readiness;
- not-recommended routes prioritize route adjustment or planning review.

Raw AI inputs, body metrics, and historical evidence should not appear in the visible route detail.

## Plan Tab

The plan tab should present a trip timeline:

- total duration;
- recommended departure window or latest turn-back point when available;
- supply/rest/risk checkpoints;
- weather verification language.

If there is no real weather provider, the UI should not label weather as stable. It should say that weather needs review.

## Gear Tab

The gear tab should look like a route checklist, not a generic panel list:

- route checklist / my gear / detail sections;
- must-have, check, missing, and optional groups;
- matched brand/model displayed inline;
- add existing gear through a bottom sheet.

The AI recommendation label should be a small Beta support signal rather than the main page identity.

## Visual Direction

Preserve the user's preferred reference style:

- warm white and light terrain background;
- deep moss primary actions;
- blue only for current location/progress;
- amber/red only for caution and blockers;
- large map surface for the route tab;
- sparse, action-first Chinese labels.

## Implementation Notes

The redesign should reuse existing engines where possible:

- `RouteAssessmentEngine`
- `HikePlanEngine`
- `RouteCockpitPresentationEngine`
- `TrailMapReadinessEngine`
- `TrackRecordingEngine`
- `RouteGearAdvisorEngine`

The work is primarily presentation and flow architecture. Do not add new navigation guarantees or server assumptions.
