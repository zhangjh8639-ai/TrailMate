## 1. Implementation

- [x] Add failing unit coverage for negative, NaN, infinite, missing, and over-threshold field accuracy.
- [x] Add failing unit coverage for invalid max-accuracy thresholds.
- [x] Harden `TrailMateLocationFixReliability.isReliableForFieldUse` with finite and non-negative accuracy checks.
- [x] Keep timestamp freshness and valid-accuracy behavior unchanged.

## 2. Validation

- [x] Run targeted `TrailMateLocationFixReliabilityTest`.
- [x] Run OpenSpec validation in strict mode.
- [x] Run the Android/server test suite.
- [x] Request subagent code review before opening the PR.
