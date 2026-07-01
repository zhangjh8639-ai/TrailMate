## 1. Import State Contract

- [x] 1.1 Add failing route tab state tests for idle, importing, cancelled, successful selected-file import, failed read, parser rejection, and deprecated-surface absence.
- [x] 1.2 Implement minimal route tab import state/reducer APIs to pass the tests.
- [x] 1.3 Keep the default route tab from showing a fake successful import before a user-selected file exists.

## 2. File Reader Boundary

- [x] 2.1 Add failing unit tests for selected document display-name fallback, supported extension checks, oversized text guard, and read failure result mapping.
- [x] 2.2 Implement the route import file reader boundary with `ContentResolver`/`Uri` handling and structured results.
- [x] 2.3 Ensure the file reader does not require broad external storage permissions.

## 3. Compose Integration

- [x] 3.1 Wire `RoutesScreen` import control to an event callback and render idle/importing/cancelled/success/failure states.
- [x] 3.2 Wire `TrailMateApp` to `ActivityResultContracts.OpenDocument`, read the selected document, parse it, and update route tab state.
- [x] 3.3 Preserve non-persistent save/detail/start-navigation affordances without adding side effects.

## 4. Documentation And Verification

- [x] 4.1 Add a Superpowers implementation plan under `docs/superpowers/plans/`.
- [x] 4.2 Document the route import file-picker slice and its limitations.
- [x] 4.3 Run OpenSpec validation, targeted tests, full unit tests, debug build, and real-device smoke import when a test file is available.
- [x] 4.4 Request/perform code and product review before pushing the PR.
