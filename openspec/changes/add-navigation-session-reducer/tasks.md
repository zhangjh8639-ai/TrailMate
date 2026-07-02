## 1. Tests First

- [x] 1.1 Add failing unit test for starting navigation runtime state.
- [x] 1.2 Add failing unit test that location samples update snapshot, evidence, and progress.
- [x] 1.3 Add failing unit test that repeated off-route samples become confirmed off-route.
- [x] 1.4 Add failing unit test that original-return state survives on-route samples.
- [x] 1.5 Add failing unit test that poor GPS suppresses nearest-route guidance.
- [x] 1.6 Add failing unit test that ended sessions ignore later samples and resume actions.
- [x] 1.7 Add runtime regression test that pause and resume preserve prior navigation mode.
- [x] 1.8 Add regression tests for poor GPS progress retention and ending stale snapshots.

## 2. Core Implementation

- [x] 2.1 Add immutable runtime state and action models.
- [x] 2.2 Implement start, pause, resume, return, and end actions.
- [x] 2.3 Route location samples through `NavigationSnapshotEngine`.
- [x] 2.4 Preserve previous off-route evidence and route progress across samples.
- [x] 2.5 Keep reducer side-effect free and independent from Android services, storage, network, and UI.

## 3. Verification

- [x] 3.1 Run OpenSpec validation.
- [x] 3.2 Run focused navigation runtime reducer tests.
- [x] 3.3 Run full unit tests and debug build.
- [x] 3.4 Request code review before PR.
