## 1. Domain Model Foundation

- [x] 1.1 Add JVM tests for route metadata, geometry, and privacy defaults.
- [x] 1.2 Implement immutable route, geometry, waypoint, risk point, exit point, and typed value models.
- [x] 1.3 Verify route model tests pass.

## 2. Navigation State Semantics

- [x] 2.1 Add JVM tests for navigation session defaults and reducer transitions.
- [x] 2.2 Implement navigation session, navigation state, navigation event, reducer, and snapshot models.
- [x] 2.3 Verify navigation reducer tests pass.

## 3. Safety, Records, And Feedback

- [x] 3.1 Add JVM tests for emergency card copy, route record privacy, feedback categories, and deprecated scope absence.
- [x] 3.2 Implement emergency card, track point, route record, route feedback, feedback category, and safety copy helpers.
- [x] 3.3 Verify safety and record tests pass.

## 4. Documentation And Verification

- [x] 4.1 Document the core model boundary and future ownership for GPX/KML, core geo, tracking service, and persistence.
- [x] 4.2 Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.
- [x] 4.3 Run `.\gradlew.bat :app:assembleDebug --console=plain`.
- [x] 4.4 Run `openspec validate add-core-navigation-domain`.
- [x] 4.5 Run `git diff --check` and verify no legacy feature packages or generated build artifacts are staged.
