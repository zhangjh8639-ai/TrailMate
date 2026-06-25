# Add P0 off-route alert policy

## Summary

Add an episode-based off-route alert policy for light navigation. The policy turns existing GPS route-check states into user-facing alert decisions so TrailMate can warn when a hiker is likely off route, avoid repeating the same warning every fix, escalate when deviation worsens, and clear the episode after the user rejoins the route.

## Motivation

TrailMate's hiking core is offline trusted navigation: in weak-signal, fork-heavy, low-attention field conditions, users need to know when they may be on the wrong path. The app already detects route mismatch as `CHECK_ROUTE`, but it does not yet define when to alert, when to suppress repeated alerts, and when to mark the episode recovered. Without that policy, future notification and vibration integration risks either missing important warnings or annoying users with repeated alerts.

## Scope

- Add a deterministic mobile model for off-route alert decisions.
- Surface the current alert decision in the route GPS panel before the detailed recovery panel.
- Reuse existing `LocationBackedHikeStatus` and `HikeLocationFix` route-check outputs.
- Keep all alert text Chinese and action-oriented.
- Keep the first implementation local and deterministic; no cloud service, AI model, or map provider change.

## Out Of Scope

- Changing the 75 m off-route threshold in `LocationBackedHikeSessionEngine`.
- Android system notification channel or vibration API wiring.
- Route rerouting, rescue dispatch, or guaranteed safety claims.
- Server persistence.
