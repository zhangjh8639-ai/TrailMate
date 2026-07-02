## 1. Save State Contract

- [x] 1.1 Add failing route tab state tests for saveable parsed imports, private track-only imported assets, idempotent repeated saves, failed imports not saveable, and deprecated-surface absence after save.
- [x] 1.2 Implement the minimal route import save state and reducer APIs to pass those tests.
- [x] 1.3 Ensure saved imported assets use `GPX 导入`/`KML 导入`, `仅轨迹可用`, `待确认`, `未验证`, and `可信度待确认` copy.

## 2. Compose Save Interaction

- [x] 2.1 Add a callback from `RoutesScreen` for the parsed-preview save action without making failed previews saveable.
- [x] 2.2 Wire `TrailMateApp` to call the save reducer from the `保存到路线` action.
- [x] 2.3 Keep save/detail/start-navigation as affordances only; do not add route detail or navigation side effects.

## 3. Documentation And Review

- [x] 3.1 Update route import docs with the in-memory save behavior and persistence limitation.
- [x] 3.2 Run OpenSpec validation, route-focused tests, full unit tests, debug build, and real-device smoke save when a test GPX is available.
- [x] 3.3 Request/perform code and product review before pushing the PR.
