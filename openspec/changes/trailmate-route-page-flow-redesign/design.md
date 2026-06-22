# Design: Route Page Flow Redesign

## User Flow

1. Route workspace: import or continue a target GPX.
2. Route detail assessment: decide if the target route is suitable.
3. Gear or route cockpit: either fill missing gear or begin light navigation.
4. Route cockpit: use GPS, track recording, checkpoints, safety share, and recovery guidance.
5. Data tab: review recorded track after finishing.

## Screen Contracts

### Route Workspace

The workspace is a route-preparation hub. It should show a current route card, route facts, and a primary "继续准备" action. It should not show full assessment, GPS controls, field diagnostics, or raw import/debug state when there is no active import error.

### Assessment

Assessment uses route/profile/equipment evidence only through summarized outputs:

- match level;
- estimated duration;
- distance and ascent;
- confidence;
- at most three risk chips;
- next action.

The full map is not shown here.

### Route Cockpit

The cockpit remains map-first and state-driven. It uses the existing presentation engine and field readiness strip. Diagnostics remain in a collapsed "地图与定位设置" section.

### Plan

Plan is a checkpoint timeline with advisory copy. It should make supply, rest, and risk checks visible without sounding like a rescue or guarantee.

## Technical Approach

- Reuse existing domain engines.
- Replace `RouteAssessmentTab` with a purpose-built assessment layout.
- Add callbacks from assessment to route and gear tabs.
- Keep `RouteCockpitTabContent` and `ReferenceRouteSurface` as the field map path.
- Update smoke tests to reflect the corrected default route detail flow.
