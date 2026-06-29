# Design: Off-route recovery safe-exit option

## Product Rule

During an off-route state with reliable fix evidence, TrailMate should show a recovery action that points the user toward safe-exit guidance. This is a decision prompt, not turn-by-turn escape routing.

## Technical Approach

The change is isolated to the pure `RouteDeviationRecoveryEngine` and its glyph mapping. Add a new action kind for safe-exit review and insert it after "回到最近路线" in reliable off-route recovery actions.

The existing `RouteExitGuidancePanel` already renders directly below recovery guidance, so this change makes the relationship visible without introducing new navigation callbacks.

## Verification

- Add a failing unit test proving reliable off-route recovery exposes "查看安全退出".
- Verify low-accuracy and rejoined states do not gain a misleading exit option.
- Run targeted tests, OpenSpec validation, diff check, full Gradle tests, and read-only code review.
