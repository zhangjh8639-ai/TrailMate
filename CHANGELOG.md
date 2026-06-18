# Changelog

## Unreleased

- Added an Android Compose prototype scaffold for TrailMate direction D.
- Added first client iteration for onboarding baseline profile, light route navigation, and AI gear checklist UI backed by sample gear data.
- Added a sign-in/register prototype step plus a real baseline profile questionnaire feeding Home profile summaries.
- Defined the production auth/profile-sync boundary for account-bound profile drafts, returning-user restore, account switching, and conflict handling.
- Added a target-route sample GPX import gate before showing route assessment, light navigation, plan, and gear tabs.
- Added a tested target-route GPX parser for track/route points, route name, distance, ascent, and point count.
- Added an Android system file-picker route import path with recoverable GPX parse errors.
- Added a prototype GPX import queue state with parsed, importing, failed, and retry-available UI feedback.
- Added a deterministic questionnaire-based route assessment engine and wired route detail to dynamic assessment results.
- Added deterministic hike plan checkpoints and wired the Route/Plan tabs to the imported GPX route assessment.
- Added Active Hike prototype controls for starting, pausing, resuming, and advancing light-navigation checkpoints.
- Added a prototype historical GPX capability profile summary with sample-history calibration from questionnaire fallback to GPX evidence.
- Added an Android system file-picker historical GPX import path with partial failure reporting.
- Added a local historical activity list with duplicate suppression and remove controls.
- Parsed historical GPX point timestamps for activity duration when available, with estimate fallback and local state preservation.
- Added historical pace calibration so GPX evidence can tune capability copy and route ETA ranges.
- Wired prototype historical GPX capability evidence into route assessment confidence, risk text, and match level.
- Persisted prototype historical GPX activities in the local snapshot and Data export preview.
- Added a deterministic route-aware gear advisor fallback for the Gear tab while keeping route risk scoring separate.
- Added an AI gear advisor request/response contract with assessment fingerprint validation and a fallback-active Gear tab status.
- Added stale AI gear response handling so the Gear tab labels old-route checklist responses and keeps showing the current route fallback checklist.
- Refreshed validated AI gear recommendations against the current personal gear inventory before displaying covered or missing states.
- Added a saveable in-memory My Gear prototype with branded gear add, availability, delete, and route gear recommendation matching.
- Added a My Gear details tab with per-item route readiness and recommendation rationale.
- Added a local SharedPreferences snapshot store for baseline profile, imported target route, and personal gear state.
- Added a local session repository boundary between Compose state and the SharedPreferences snapshot adapter.
- Added a Data tab with local export preview and confirmed clear-local-data flow returning users to onboarding.
- Improved the Data tab export preview with readable local data summaries.
- Fixed local clear-data state so re-onboarding cannot resurrect a previously imported route or gear inventory.
- Added unit and Compose smoke-test coverage for baseline profile confidence, AI gear boundaries, onboarding, and route tabs.
