# Tasks: TrailMate D Light Navigation And Gear

## Design And Spec

- [x] Confirm prototype direction with user.
- [x] Add browser prototype for D flow.
- [x] Add baseline profile questionnaire to prototype.
- [x] Add server catalog matching and AI gear checklist to prototype.
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
- [x] Replace legacy gear add flow with read-only server brand catalog candidates.
- [x] Add Gear details tab for selected catalog item readiness.
- [x] Link route gear recommendations to catalog candidates by category.
- [x] Remove in-memory gear availability and delete actions from the mobile flow.
- [x] Add smoke coverage that route-to-gear matching does not expose personal gear actions.
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
- [x] Add local snapshot persistence for baseline profile, imported route, and gear checklist state.
- [x] Add a local session repository boundary ahead of Room and cloud sync integration.
- [x] Add local Data tab with readable export preview and confirmed clear-local-data flow.
- [x] Add deterministic route-aware fallback gear checklist after target route import.
- [x] Add AI gear advisor request/response contract and assessment fingerprint validation.

## Implementation Planning Backlog

- [x] Define baseline profile persistence and privacy rules for the prototype local snapshot.
- [x] Define real auth integration and post-login profile sync.
- [x] Define production GPX import queue, persistence, and retry state machine.
- [x] Replace questionnaire fallback capacity with production historical GPX capability profile.
- [x] Retire prototype gear inventory persistence from the local snapshot.
- [x] Define catalog gear detail view and route readiness summary.
- [x] Define prototype local delete/export controls for profile, route, and checklist data.
- [x] Define production cloud delete/export rules for profile and checklist artifacts.
- [x] Define prototype route experience navigation tabs and Active Hike controls in Android.
- [x] Define location-backed hike session core.
- [x] Define AI gear advisor backend contract shape and Android validation.
- [x] Connect Android AI gear advisor backend service boundary.
- [ ] Connect concrete AI gear advisor HTTP transport, auth, and checklist artifact persistence.
- [x] Define deterministic fallback gear checklist.
- [x] Define tests for AI boundary: route score cannot be changed by gear advisor.
- [x] Define tests for prototype delete/export of profile, route, and checklist data.
- [x] Define UI tests for questionnaire skip, save, and stale gear checklist states.
