## 1. Tests First

- [x] 1.1 Add failing reducer tests for permission request, granted start, denied fallback, and stop actions.
- [x] 1.2 Add failing unit test for runtime permission request contents across Android versions.
- [x] 1.3 Add failing unit test for the service launcher contract/actions without starting fake GPS.
- [x] 1.4 Update navigation state visible-text tests for the new Chinese start/stop/fallback copy and deprecated-surface exclusions.

## 2. Permission / Launcher Boundary

- [x] 2.1 Add a pure navigation tracking start reducer or equivalent testable state helper.
- [x] 2.2 Add runtime permission request helper that includes fine/coarse location and Android 13+ notifications only when applicable.
- [x] 2.3 Add `TrackingServiceLauncher` abstraction and Android implementation for start/stop commands.

## 3. Compose Navigation Flow

- [x] 3.1 Wire the Navigation tab selected-route state to the start action and permission launcher.
- [x] 3.2 Show a concise permission explanation and denied fallback without creating a pre-trip checklist.
- [x] 3.3 Show stop action after service start and send the service stop command.
- [x] 3.4 Keep idle route selection flow unchanged.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run focused navigation/tracking tests.
- [x] 4.3 Run full unit tests and debug build.
- [x] 4.4 Run real-device install/launch smoke and service start/stop smoke if the connected device is available.
- [x] 4.5 Request subagent spec and code-quality review before PR.
