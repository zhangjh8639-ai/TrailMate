## 1. Tests First

- [x] 1.1 Add failing unit tests for platform route asset detail state generation.
- [x] 1.2 Add failing unit tests for saved imported route detail state boundaries: local private, track-only, unverified, and no basemap.
- [x] 1.3 Add failing unit tests for opening/closing route detail while preserving the active import preview.
- [x] 1.4 Add failing unit tests that route detail visible text does not restore deprecated surfaces.

## 2. Route Detail State

- [x] 2.1 Add route detail state models and visible text helpers.
- [x] 2.2 Add pure state transitions for selecting and clearing route detail.
- [x] 2.3 Populate detail actions for saved/restored imported route assets without adding fake navigation actions.

## 3. Compose UI

- [x] 3.1 Add click handling from route asset cards to route detail selection.
- [x] 3.2 Add a read-only route detail screen with back navigation and compact route facts.
- [x] 3.3 Show imported route boundary notes prominently and avoid placeholder start-navigation behavior.

## 4. Verification

- [x] 4.1 Run OpenSpec validation and update tasks as completed.
- [x] 4.2 Run unit tests and debug build.
- [x] 4.3 Install on the connected real device and manually verify opening/closing route detail for platform and imported routes.
- [x] 4.4 Request code review and product/UX review before PR.
