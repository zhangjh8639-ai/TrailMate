## 1. Specification

- [x] 1.1 Validate OpenSpec artifacts for `add-tracking-active-session-recovery`.

## 2. Tests

- [x] 2.1 Add unit tests for recovery UI state and Chinese copy.
- [x] 2.2 Add unit tests that active tracking suppresses stale recovery copy.
- [x] 2.3 Add unit tests for ending a recovered session reducer/handler where the logic is extractable.

## 3. Implementation

- [x] 3.1 Add a small Navigation recovery UI state model.
- [x] 3.2 Load the latest unfinished local tracking session from `TrackingRecordingStore` on app startup.
- [x] 3.3 Render a Navigation-tab recovery card with explicit local/private wording and an end action.
- [x] 3.4 Mark the recovered session ended when the user explicitly ends it.
- [x] 3.5 Avoid duplicate recovery copy while live tracking state is active.

## 4. Verification

- [x] 4.1 Run `openspec validate add-tracking-active-session-recovery --strict`.
- [x] 4.2 Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.
- [x] 4.3 Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [x] 4.4 Run device smoke or connected Android tests on the attached phone when the build is green.
- [x] 4.5 Request code/product review before PR.
