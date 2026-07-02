## Context

The previous slices introduced a real foreground location session, but samples remain in memory. TrailMate's product promise depends on continuing to record while the screen is locked or the UI is recreated, and later features such as route matching, off-route review, emergency context, and record summaries need an ordered local track source.

The current app is still a single-module Android app. It already uses `SQLiteOpenHelper` for imported route persistence in `trailmate.db`, and it does not yet have Room/Hilt service injection. This slice should add durable tracking storage without introducing a broad persistence framework migration.

## Goals / Non-Goals

**Goals:**
- Persist active navigation recording session metadata locally.
- Persist real provider location samples as ordered track point rows.
- Default persisted sessions to private visibility.
- Provide queries for unfinished active sessions and ordered session points.
- Connect `TrackingLocationSession` to a recording store boundary so future service recovery can resume from durable data.
- Keep the implementation testable without Android service instrumentation.

**Non-Goals:**
- No route matching, progress calculation, off-route detection, record summary generation, or UI record list.
- No server sync, upload, live sharing, or public visibility.
- No fake GPS point generation or route simulation.
- No full Room/Hilt migration in this slice.
- No long-duration background performance optimization yet.

## Decisions

1. Use a small `TrackingRecordingStore` interface with SQLite implementation.
   - Rationale: the foreground service and session should depend on an abstract local recorder, not directly on SQLite details.
   - Alternative considered: write directly from `TrackingLocationSession` into `SQLiteDatabase`; rejected because it couples provider callbacks to schema details and is harder to test.

2. Store session rows and track point rows separately.
   - `tracking_sessions` stores id, route id, started/ended timestamps, state, direction, visibility, and sample count.
   - `tracking_track_points` stores ordered point index, coordinates, elevation, accuracy, timestamp, speed, and bearing.
   - The SQLite store assigns point indexes from the current database max index inside the append transaction, so a resumed session appends after existing points instead of replacing them.
   - Rationale: track points can grow large and need ordered retrieval for recovery and later replay.
   - Alternative considered: JSON blob per session; rejected because appending and partial recovery would be fragile.

3. Keep session creation explicit.
   - `TrackingLocationSession` receives a `NavigationSession` and a `TrackingRecordingStore`.
   - The selected route and generated navigation session identity are passed into `TrackingForegroundService` in the start intent.
   - Rationale: route/session identity should be explicit instead of invented by the provider callback layer or foreground service.
   - Alternative considered: auto-generate session ids inside the store; deferred until the navigation start flow owns a real session factory.

4. Append only from provider samples.
   - Rationale: every persisted point must correspond to a real `LocationSample`; no points are written at `start()` before first GPS fix.
   - Alternative considered: write an initial placeholder point; rejected because placeholders corrupt downstream distance and off-route calculations.

5. Share the app database with an explicit v1 to v2 migration.
   - Rationale: both imported route and tracking persistence are early local storage in the same app database.
   - Existing version 1 databases created by imported route persistence are migrated by creating the new tracking tables and indexes with `IF NOT EXISTS`.
   - Alternative considered: separate tracking DB; rejected because future migration would have to coordinate two local stores.

## Risks / Trade-offs

- [Risk] Schema version 1 already exists for imported routes. -> Mitigation: bump to version 2 and migrate v1 databases by creating the tracking tables without dropping imported route data.
- [Risk] Writing every GPS fix synchronously can become expensive on long routes. -> Mitigation: this slice proves correctness; later slices can batch writes or move recording onto a dedicated dispatcher.
- [Risk] Service currently uses direct construction rather than DI. -> Mitigation: keep the recording store behind a small interface and construct SQLite only in the Android service boundary after validating route/session context.
- [Risk] No crash recovery UI yet. -> Mitigation: provide `findActiveSession()` and ordered point queries now so a later slice can restore state.

## Migration Plan

Database version 2 creates tracking recording tables alongside imported route tables. Version 1 databases are migrated in place with `CREATE TABLE IF NOT EXISTS` and index creation, preserving existing imported routes.

## Open Questions

- Whether the eventual Room migration should preserve the same table names or create a new schema with explicit migrations.
- Whether track point persistence should batch by time/distance once low-power navigation mode is introduced.
