## 1. Tests First

- [x] 1.1 Add failing unit tests for platform route detail handoff into navigation ready state.
- [x] 1.2 Add failing unit tests for imported route readiness boundary copy.
- [x] 1.3 Add failing unit tests that route handoff does not expose fake active navigation actions.
- [x] 1.4 Add failing unit tests that Navigation idle/ready visible text avoids deprecated surfaces.

## 2. Navigation Handoff State

- [x] 2.1 Add `NavigationTabState` / selected-route ready state models.
- [x] 2.2 Add pure mapping from `RouteAssetCardState` or route detail data into navigation readiness data.
- [x] 2.3 Add app shell state transition from route detail action to Navigation tab selection.

## 3. Compose UI

- [x] 3.1 Add a Navigation tab screen with idle and selected-route ready states.
- [x] 3.2 Add a route detail action to select the route for navigation readiness.
- [x] 3.3 Show imported-route track-only/no-basemap notes in the Navigation ready state.
- [x] 3.4 Avoid GPS, tracking, active session, and fake start-navigation affordances.

## 4. Verification

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run unit tests and debug build.
- [x] 4.3 Install on the connected real device and verify route detail handoff to Navigation tab for platform and imported routes.
- [x] 4.4 Request code review and product/UX review before PR.
