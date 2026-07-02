## Context

The previous slice added a Navigation-tab recovery card for the latest unfinished local tracking session. That closes the "invisible local record" gap, but it leaves one important ambiguity: an unfinished local session may either be a stale local record or a foreground tracking service that is still running in the current app process.

TrailMate must not tell users a live GPS service is merely a local stale record, and it must not allow a local "end recovered session" action to leave the service appending points after the session was marked ended. This change adds a small local runtime-state bridge between the foreground service and Navigation UI.

## Goals / Non-Goals

**Goals:**

- Track the currently running foreground tracking session id and route id in local in-process runtime state.
- Let Navigation UI prefer "foreground service running" copy when runtime state says tracking is active.
- Keep the recovered local-record card for unfinished sessions when no running service is known.
- Stop foreground tracking when the user ends a recovered/running local record.
- Clear runtime state when tracking stops or service is destroyed.
- Cover precedence and lifecycle behavior with unit tests before implementation.

**Non-Goals:**

- Full crash, reboot, or process-death service resurrection.
- Cross-process persistence of runtime service status.
- Route record creation, post-trip summaries, off-route guidance, map rendering, or emergency card changes.
- Syncing, uploading, sharing, or publishing tracking sessions.
- Introducing a DI framework, broadcast receiver, WorkManager job, or new external dependency.

## Decisions

1. Use a tiny in-process runtime registry instead of persisted "service running" storage.

   - Rationale: Android can kill a process independently of service lifecycle details, and persisted "service running" flags are easy to become stale. In-process state is honest: it only claims live tracking while this process knows the service is active.
   - Alternative considered: store `service_running=true` in SQLite/DataStore. Rejected because a crash could leave the flag true and mislead users.

2. Prefer live service copy over recovered-local-record copy.

   - Rationale: if the runtime registry says a service is active, the user needs to see that GPS tracking is actually running, not a generic recovery warning.
   - Alternative considered: show both cards. Rejected because it creates duplicate actions and makes the state harder to understand on a small Navigation screen.

3. Treat "end recovered session" as a stop-service action when a runtime service may exist.

   - Rationale: ending the local session while service callbacks keep appending points would corrupt the product truth. Stopping the service first is the safer default.
   - Alternative considered: only mark the DB ended. Rejected because location callbacks may continue in the active service case.

4. Keep UI wording precise.

   - Rationale: AGENTS requires truthful, conservative safety wording. The UI can say "foreground tracking service is running" only when runtime state indicates it, and must not claim crash recovery or rescue.
   - Alternative considered: "已恢复导航". Rejected because it overstates what this slice implements.

## Risks / Trade-offs

- [Risk] In-process runtime state is lost after process death. -> Mitigation: the existing recovered-local-record path remains the fallback; copy does not claim service recovery after process death.
- [Risk] UI might briefly show recovered copy before runtime state loads. -> Mitigation: initialize runtime state synchronously from a lightweight singleton when composing the app.
- [Risk] Runtime state can be wrong if service stop crashes mid-command. -> Mitigation: clear runtime state in service stop and destroy paths, and keep DB recovery as fallback.
- [Risk] This still does not prove GPS is currently producing fixes. -> Mitigation: copy says foreground service is running, not that a fresh GPS fix exists.
