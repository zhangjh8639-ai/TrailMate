# Changelog

## Unreleased

- Added an Android Compose prototype scaffold for TrailMate direction D.
- Added first client iteration for onboarding baseline profile, light route navigation, and AI gear checklist UI backed by sample gear data.
- Added a sign-in/register prototype step plus a real baseline profile questionnaire feeding Home profile summaries.
- Added a target-route sample GPX import gate before showing route assessment, light navigation, plan, and gear tabs.
- Added a tested target-route GPX parser for track/route points, route name, distance, ascent, and point count.
- Added an Android system file-picker route import path with recoverable GPX parse errors.
- Added a deterministic questionnaire-based route assessment engine and wired route detail to dynamic assessment results.
- Added deterministic hike plan checkpoints and wired the Route/Plan tabs to the imported GPX route assessment.
- Added a saveable in-memory My Gear prototype with branded gear add, availability, delete, and route gear recommendation matching.
- Added a local SharedPreferences snapshot store for baseline profile, imported target route, and personal gear state.
- Added unit and Compose smoke-test coverage for baseline profile confidence, AI gear boundaries, onboarding, and route tabs.
