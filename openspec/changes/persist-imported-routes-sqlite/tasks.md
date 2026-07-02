## 1. Tests First

- [x] 1.1 Add failing unit tests for parsed import to persistent record mapping, including private, track-only, and unverified defaults.
- [x] 1.2 Add failing unit tests for persisted record to route asset card mapping, including bounded copy and no start/detail actions.
- [x] 1.3 Add failing unit tests for startup merge ordering and duplicate imported route handling.
- [x] 1.4 Add failing unit tests for geometry point persistence round-trip data.

## 2. Persistence Model

- [x] 2.1 Add imported route persistence record and geometry point model.
- [x] 2.2 Add pure mappers from saveable parsed import data to persistent records.
- [x] 2.3 Add pure mappers from persistent records to route asset cards and route tab state.

## 3. SQLite Store

- [x] 3.1 Add an `ImportedRouteStore` interface and SQLite implementation using `SQLiteOpenHelper`.
- [x] 3.2 Create `imported_routes` and `imported_route_points` tables with schema version 1.
- [x] 3.3 Implement transactional upsert and ordered geometry point loading.

## 4. App Wiring

- [x] 4.1 Extend route import save state to retain metadata and geometry needed for persistence.
- [x] 4.2 Load persisted imported routes on app startup using an IO coroutine and merge them into the route tab.
- [x] 4.3 Save parsed imports through the store before updating the route tab card.

## 5. Verification

- [x] 5.1 Update route import documentation to describe local persistence and boundaries.
- [x] 5.2 Run OpenSpec validation, unit tests, and debug build.
- [x] 5.3 Install on the connected real device and manually verify import, save, force-stop, restart, and restored route card behavior.
- [x] 5.4 Request code review and product/UX review before PR.
