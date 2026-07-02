## 1. Specification

- [x] 1.1 Validate OpenSpec artifacts for `add-tracking-service-session-consistency`.

## 2. Tests

- [x] 2.1 Add unit tests for the tracking service runtime registry start/clear behavior.
- [x] 2.2 Add unit tests that Navigation state prefers running service copy over recovered-local-record copy.
- [x] 2.3 Add unit tests that running-service copy avoids crash recovery, rescue, sharing, and fresh-fix claims.
- [x] 2.4 Add a device runtime test for registry mark/clear behavior.

## 3. Implementation

- [x] 3.1 Add a local tracking service runtime state model/registry.
- [x] 3.2 Update foreground service start/stop/destroy paths to set and clear runtime state.
- [x] 3.3 Add Navigation UI state for known running tracking service sessions.
- [x] 3.4 Wire `TrailMateApp` to read runtime state and prefer running-service UI over recovered local-record UI.
- [x] 3.5 Ensure ending local tracking from Navigation requests service stop and does not hide state before local end succeeds.

## 4. Verification

- [x] 4.1 Run `openspec validate add-tracking-service-session-consistency --strict`.
- [x] 4.2 Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.
- [x] 4.3 Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [x] 4.4 Run `.\gradlew.bat :app:connectedDebugAndroidTest --console=plain` on the attached phone.
- [x] 4.5 Install and smoke-launch on the attached phone.
- [x] 4.6 Request code/product review before PR.
