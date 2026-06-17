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
- [x] Link route gear recommendations to available owned gear by category.
- [x] Add in-memory gear availability and delete actions.
- [x] Add smoke coverage for route-to-gear add flow and matched gear copy.
- [x] Add target-route GPX import prototype gate before route assessment tabs.
- [x] Add tested target-route GPX parser for track/route points, distance, ascent, and point count.
- [x] Harden prototype GPX XML parsing against DOCTYPE/XXE and mixed track/route ordering.
- [x] Add deterministic questionnaire-based route assessment engine.
- [x] Wire route detail tabs to dynamic assessment results after target route import.

## Implementation Planning Backlog

- [ ] Define baseline profile persistence and privacy rules.
- [ ] Define real auth integration and post-login profile sync.
- [ ] Define Android system file picker and production GPX import state machine.
- [ ] Replace questionnaire fallback capacity with historical GPX capability profile.
- [ ] Define gear inventory persistence and delete/export rules.
- [ ] Define route experience navigation tabs in Android.
- [ ] Define AI gear advisor backend contract and validation.
- [ ] Define deterministic fallback gear checklist.
- [ ] Define tests for AI boundary: route score cannot be changed by gear advisor.
- [ ] Define tests for delete/export of profile and gear data.
- [ ] Define UI tests for questionnaire skip, save, and stale gear checklist states.
