# Route Fullscreen Navigation Focus

## Overview

This change treats the route tab as two modes:

- Default cockpit: compact, map-first, good for reviewing the route before moving.
- Full-screen navigation: focused, field-use mode for walking and recording.

## Default Cockpit

The default route tab should not show every field status at once, and it should not stack translucent control cards on top of the map. It should keep:

- standalone map preview with only small map controls;
- a separate action panel for current checkpoint and next checkpoint;
- one primary action that opens full-screen navigation when field controls are needed;
- compact progress;
- full-screen entry;
- lightweight status chips for location, recording, and offline route state;
- one short safety disclaimer.

The visible default page must not include `开始徒步`, pause/resume, mark-checkpoint, or a mixed map-diagnostics block. Readiness details, track diagnostics, map layers, and route setup details can remain behind secondary detail surfaces, but those surfaces must be framed around user tasks such as checkpoints, supply, rest, and route details rather than authorization or map diagnostics. They must not contain authorization actions.

## Authorization Placement

Map service consent and foreground location permission belong in onboarding / first-use preparation. After the user completes baseline setup and accepts map service preparation, TrailMate requests Android foreground location permission so route navigation, current-position support, and track recording all share the same authorization moment.

The route tab may explain that online base maps are not enabled or that location is unavailable, but it must not present map-status or light-navigation authorization as a normal route-page setup step. Route-level location actions are only fallback recovery when the user skipped or denied the first-use permission prompt.

## Full-Screen Navigation

Full-screen navigation should:

- hide the app bottom navigation;
- use the full available viewport;
- preserve the route map as the main surface;
- keep current checkpoint, next checkpoint, live checkpoint action guidance, route progress, route-match state, location status, and track recording state visible;
- expose only field-critical actions: start/pause/resume, safety share, mark checkpoint, finish recording, and exit full screen.

The mode is still light navigation. It should show the safety disclaimer and avoid implying turn-by-turn or rescue-grade guidance.
