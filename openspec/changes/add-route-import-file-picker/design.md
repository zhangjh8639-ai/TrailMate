## Context

The Android app already has a route import parser and a route tab UI that renders a deterministic sample import preview. The next production step is to let a user pick a real GPX/KML document from Android's system document picker and show the parsed result in the same route asset context.

This slice sits between UI and parser. It must not introduce route persistence, map rendering, GPS, background tracking, or a full navigation launch. It must also avoid bringing Web/Lovable code into the Android app; the Lovable prototype is reference material for flow hierarchy only.

## Goals / Non-Goals

**Goals:**

- Launch Android's system document picker from the `路线` tab.
- Accept common GPX/KML MIME types and extension-based fallbacks.
- Read the selected URI through a testable importer boundary.
- Parse the selected document with the existing `RouteImportParser`.
- Show importing, success, cancelled, and failure states in Chinese.
- Keep successful imports as a non-persistent preview until a later save/imported-route PR.
- Preserve the route-only safety copy that imported files do not contain commercial map basemaps.

**Non-Goals:**

- No Room/DataStore persistence.
- No route detail destination.
- No actual "start navigation" side effect.
- No MapLibre, GPS, foreground service, or offline route package work.
- No planner, equipment, community, marketplace, or complex pre-trip check scope.

## Decisions

### Decision: Use the Android system document picker instead of custom file browsing

Use `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument())` from Compose. This delegates storage access, file providers, and document permissions to Android rather than requiring broad storage permissions.

Alternatives considered:

- `ACTION_GET_CONTENT`: simpler but weaker for persisted document access semantics and less explicit about opening a document.
- Custom file browser: rejected because it adds storage complexity and unnecessary permissions.

### Decision: Keep file I/O outside composables

Add a small `RouteImportFileReader` boundary that takes a `ContentResolver` and `Uri`, derives a display filename, reads text content with a size guard, and returns a sealed result. `RoutesScreen` can call this boundary from an event handler, but parsing and file metadata stay outside composable rendering.

Alternatives considered:

- Parse directly inside `RoutesScreen`: rejected because it couples UI rendering to file-system details and is harder to unit test.
- Add a ViewModel now: deferred until persistence or navigation actions require lifecycle-aware state coordination. The current app shell does not have ViewModel/Hilt yet.

### Decision: Keep state reducer pure and unit tested

`RoutesTabState` should gain explicit import states and reducers such as `withImporting`, `withImportCancelled`, and `withImportResult`. Unit tests can prove copy, labels, and failure behavior without Android framework dependencies.

Alternatives considered:

- Store raw `Uri` or parser results in UI state: rejected because UI state should expose stable labels and visible user-facing content, not platform handles.

### Decision: Guard file size before parsing

Read selected files with a conservative text size cap for this slice. Oversized files should produce a friendly failure state instead of blocking UI or risking memory pressure. Streaming import can be added later when route persistence and background work exist.

Alternatives considered:

- Parse arbitrary files into memory: rejected for production-grade expectations.
- Implement full streaming now: deferred because the current parser API accepts text content and this slice should stay review-sized.

## Risks / Trade-offs

- **Large real-world GPX/KML files may exceed the first size cap** -> show a clear failure and leave streaming/background parsing to a later route import hardening PR.
- **Some Android document providers return generic MIME types** -> include extension-based fallback in the picker/import validation.
- **The route tab still has non-persistent success actions** -> label this PR as import-result only; failed imports only show retry, while save/detail/start stay visible only for parsed previews and still have no side effects.
- **No instrumented test for system picker in this slice** -> verify with JVM reducer/reader tests plus real-device smoke import when possible.
