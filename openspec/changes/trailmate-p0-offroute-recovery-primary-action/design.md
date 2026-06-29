# Design: Off-route recovery primary action priority

## Product Rule

Route cockpit primary CTA represents the safest next field action. During an active or paused hike with `CHECK_ROUTE` or recent off-route recovery acknowledgement, the CTA should open recovery guidance even if track recording is currently running.

## Technical Approach

The change is isolated to `RouteCockpitPresentationEngine.primaryAction`. Move the active/paused `CHECK_ROUTE`/`wasRecentlyOffRoute` branch above live recording controls while keeping READY departure gates outside this recovery priority.

This keeps the route detail screen event handling unchanged because `VIEW_RECOVERY` is already handled by expanding diagnostics and exiting fullscreen navigation.

## Verification

- Update `RouteCockpitPresentationEngineTest` to prove recovery CTA wins over recording pause when off route.
- Add coverage for the recent-rejoin state while recording.
- Add coverage proving READY departure gates are not replaced by recovery guidance.
- Run the targeted unit test class and full Gradle test suite.
