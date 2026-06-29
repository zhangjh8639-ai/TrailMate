## 1. Implementation

- [x] Add failing unit coverage for stale, future, low-accuracy, negative, and non-finite progress fixes.
- [x] Update progress safety watch to ignore unreliable fixes before comparing progress to the plan.
- [x] Keep existing caution and alert copy, thresholds, and action labels unchanged for reliable fixes.

## 2. Validation

- [x] Run targeted progress safety watch unit tests.
- [x] Run OpenSpec validation in strict mode.
- [x] Run the Android test suite.
- [x] Request subagent code review before opening the PR.
