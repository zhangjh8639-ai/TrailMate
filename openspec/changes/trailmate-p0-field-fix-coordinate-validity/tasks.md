## 1. Implementation

- [x] Add failing unit coverage for missing and non-finite field coordinates.
- [x] Harden `TrailMateLocationFixReliability.isReliableForFieldUse` with latitude/longitude presence and finite checks.
- [x] Keep valid coordinate, timestamp, and accuracy behavior unchanged.

## 2. Validation

- [x] Run targeted `TrailMateLocationFixReliabilityTest`.
- [x] Run OpenSpec validation in strict mode.
- [x] Run the Android/server test suite.
- [x] Request subagent code review before opening the PR.
