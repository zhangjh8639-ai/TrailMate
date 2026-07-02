## Why

TrailMate can now surface unfinished local tracking sessions, but the app still cannot distinguish a stale local record from a foreground tracking service that is actually running. A production hiking app must keep UI, local session state, and foreground-service lifecycle consistent so users are not misled about whether GPS recording is active.

## What Changes

- Add a local in-process tracking service runtime state that records the currently running session id and route id while the foreground service is active.
- Let the Navigation tab prefer a real running tracking session over the generic recovered-local-record card.
- Prevent stale recovery copy from hiding an active foreground service.
- Ensure explicit ending of a recovered/running local session stops foreground tracking and clears runtime state.
- Keep all state local/private; do not upload, share, synthesize GPS points, or create route records in this slice.
- Do not implement full crash/reboot foreground-service resurrection, map rendering, off-route navigation, record summaries, or route feedback in this slice.

## Capabilities

### New Capabilities

- `tracking-service-session-consistency`: Keeps Navigation UI, local unfinished tracking sessions, and foreground tracking service runtime state consistent without claiming unsupported crash recovery.

### Modified Capabilities

- None.

## Impact

- Android foreground tracking service lifecycle.
- Navigation tab state and copy for active vs recovered sessions.
- Tracking service start/stop launcher/runtime metadata.
- Unit tests for state precedence and service runtime registry.
- Device tests or smoke tests for start/stop behavior on the attached Android phone.
