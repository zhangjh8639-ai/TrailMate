# Tasks: TrailMate D Light Navigation And Gear

## Design And Spec

- [x] Confirm prototype direction with user.
- [x] Add browser prototype for D flow.
- [x] Add baseline profile questionnaire to prototype.
- [x] Add personal gear and AI gear checklist to prototype.
- [x] Write Superpowers design document.
- [x] Write OpenSpec change proposal.
- [x] Review this OpenSpec change with the user.
- [x] After review, create a Superpowers implementation plan before coding.
- [x] Scaffold Android Compose prototype client.
- [x] Add prototype models and tests for baseline profile and AI gear advisor boundary.
- [x] Add onboarding, temporary profile, light navigation route tabs, and gear checklist UI.
- [x] Add sign-in/register prototype step before baseline profile questionnaire.
- [x] Add baseline profile questionnaire inputs for exercise rhythm, session duration, outdoor experience, ascent, height, weight, and pack weight.
- [x] Feed completed questionnaire profile into Home summary.
- [x] Ensure skipping profile intake does not fabricate body or pack metrics.
- [x] Add in-memory My Gear section with branded gear add form.
- [x] Add My Gear details tab with per-item route readiness.
- [x] Link route gear recommendations to available owned gear by category.
- [x] Add in-memory gear availability and delete actions.
- [x] Add smoke coverage for route-to-gear add flow and matched gear copy.
- [x] Add target-route GPX import prototype gate before route assessment tabs.
- [x] Add tested target-route GPX parser for track/route points, distance, ascent, and point count.
- [x] Harden prototype GPX XML parsing against DOCTYPE/XXE and mixed track/route ordering.
- [x] Add Android system file-picker route import path with recoverable parse errors.
- [x] Add prototype GPX import queue and retry-available UI state.
- [x] Add deterministic questionnaire-based route assessment engine.
- [x] Wire route detail tabs to dynamic assessment results after target route import.
- [x] Add deterministic hike plan checkpoints for the Route and Plan tabs after target route import.
- [x] Add Active Hike prototype controls for start, pause/resume, and checkpoint advancement.
- [x] Add prototype historical GPX capability profile summary with sample-history calibration.
- [x] Add Android system file-picker historical GPX import path with partial failure reporting.
- [x] Add local historical activity list with duplicate suppression and remove controls.
- [x] Parse historical GPX point timestamps for activity duration with estimate fallback and local state preservation.
- [x] Calibrate capability profile pace and route ETA ranges from historical GPX durations.
- [x] Use prototype historical GPX capability evidence in route assessment confidence, risk text, and match level.
- [x] Persist prototype historical GPX activities in the local snapshot and export preview.
- [x] Add local snapshot persistence for baseline profile, imported route, and personal gear state.
- [x] Add local Data tab with export preview and clear-local-data flow.
- [x] Add deterministic route-aware fallback gear checklist after target route import.
- [x] Add AI gear advisor request/response contract and assessment fingerprint validation.

## Implementation Planning Backlog

- [x] Define baseline profile persistence and privacy rules for the prototype local snapshot.
- [ ] Define real auth integration and post-login profile sync.
- [ ] Define production GPX import queue, persistence, and retry state machine.
- [ ] Replace questionnaire fallback capacity with production historical GPX capability profile.
- [x] Define prototype gear inventory persistence in the local snapshot.
- [x] Define prototype gear detail view and route readiness summary.
- [x] Define prototype local delete/export controls for profile, route, and gear data.
- [ ] Define production cloud delete/export rules for profile and gear data.
- [x] Define prototype route experience navigation tabs and Active Hike controls in Android.
- [ ] Define production GPS/location-backed navigation session.
- [x] Define AI gear advisor backend contract shape and Android validation.
- [ ] Connect production AI gear advisor backend service.
- [x] Define deterministic fallback gear checklist.
- [x] Define tests for AI boundary: route score cannot be changed by gear advisor.
- [x] Define tests for prototype delete/export of profile, route, and gear data.
- [ ] Define UI tests for questionnaire skip, save, and stale gear checklist states.
