## Context

TrailMate already persists local tracking sessions and track points. The remaining product gap is visibility: after app recreation, service teardown, or process restart, an unfinished local session can exist in SQLite while the Navigation tab shows only the normal idle state. For a production hiking app, hiding an unfinished record makes the user unsure whether recording is still active and whether they need to end the session.

The AGENTS guidance keeps TrailMate focused on trustworthy track navigation. This change is intentionally narrow: it adds a local recovery entry to the Navigation tab for the latest unfinished tracking session, without adding full crash recovery, map rendering, route replay, record summaries, route sharing, or fake GPS samples.

## Goals / Non-Goals

**Goals:**

- Load the latest unfinished local tracking session from `TrackingRecordingStore` when the app starts.
- Present a clear Chinese recovery card in the Navigation tab.
- Explain that the unfinished recording is local/private and not equivalent to a running foreground GPS service.
- Let the user explicitly end the recovered local recording.
- Keep route switching and navigation copy consistent with the current active-tracking safeguards.
- Cover recovery state and copy with unit tests before implementation.

**Non-Goals:**

- Restarting the foreground tracking service after a crash or reboot.
- Synthesizing track points, GPS state, distance, elevation, or navigation progress.
- Uploading, sharing, or publishing the recovered session.
- Creating route records or post-trip summaries.
- Adding map, off-route, emergency, or original-return behavior.

## Decisions

1. Represent recovery as Navigation UI state, not as a new service mode.

   - Rationale: the app can know a local session is unfinished without proving the foreground GPS service is running. UI copy must not imply live recording unless the service is actually active.
   - Alternative considered: set `TrackingStartUiState.Active` for any unfinished DB session. Rejected because it would make stale local records look like current live GPS tracking.

2. Read only the latest unfinished local session on app start.

   - Rationale: users need one actionable recovery entry. Multiple historical unfinished rows should be resolved later by maintenance tooling, not by cluttering the main Navigation tab.
   - Alternative considered: list every unfinished session. Rejected for this slice because it complicates user choice and is not needed for the primary recovery path.

3. Ending a recovered session marks the local session ended and hides the recovery card.

   - Rationale: this is explicit, reversible only through future record history features, and avoids silently discarding data.
   - Alternative considered: delete the recovered session. Rejected because deletion would lose private recorded points without review.

4. Keep recovery local/private by design.

   - Rationale: imported routes, navigation sessions, and tracks are private by default. A recovery card is a local continuity aid, not a sharing flow.
   - Alternative considered: auto-sync unfinished sessions. Rejected because sync semantics and privacy confirmation are outside this slice.

## Risks / Trade-offs

- [Risk] The user may expect tapping recovery to resume live GPS tracking. -> Mitigation: copy uses "未结束的本地记录" and avoids "已恢复导航" unless service recovery is implemented later.
- [Risk] A stale DB row may remain after abnormal app termination. -> Mitigation: the card offers an explicit end action and does not claim the session is live.
- [Risk] Recovery state could duplicate the active tracking state when the current in-memory service is running. -> Mitigation: the UI suppresses recovery copy while the normal active tracking state is visible.
- [Risk] App startup now touches SQLite. -> Mitigation: load through a coroutine off the main thread and keep the query minimal.
