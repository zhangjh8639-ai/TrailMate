## 1. Contract Tests

- [x] 1.1 Add JVM tests for route tab state, import preview metrics, route-only copy, and legacy surface exclusions.
- [x] 1.2 Run the targeted tests and confirm they fail for missing route tab state.

## 2. Route Feature State

- [x] 2.1 Add pure Kotlin route tab state models for filters, route asset cards, import preview metrics, and quality notes.
- [x] 2.2 Add deterministic sample state that builds the import preview through `RouteImportParser`.
- [x] 2.3 Run targeted JVM tests and confirm they pass.

## 3. Compose Route Screen

- [x] 3.1 Add `RoutesScreen` using Material 3 components, compact route filters, route cards, and import preview.
- [x] 3.2 Replace the `路线` placeholder branch in `TrailMateApp` with `RoutesScreen` while preserving other tab placeholders.
- [x] 3.3 Keep visible text Chinese, route-focused, and free of planning/equipment/community/marketplace surfaces.

## 4. Documentation and Verification

- [x] 4.1 Add route tab implementation notes and update the Superpowers plan.
- [x] 4.2 Run OpenSpec validation, unit tests, debug build, and whitespace checks.
- [x] 4.3 Perform connected-device smoke testing if a device is available.
- [x] 4.4 Request code/design review, address findings, commit, push, and open a stacked draft PR.
