# Track Action Departure Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent the Route diagnostics "轨迹记录" button from bypassing required departure readiness repairs before departure.

**Assumptions:**
- The main Route cockpit action is already gated by `RouteCockpitPresentationEngine`.
- This change only affects secondary track recording start/restart entry points before the hike starts.
- Pause/resume controls for active recordings must stay available.
- If the hike is already active and recording is idle, starting recording should remain available because it improves field safety.

**Success Criteria:**
- Idle/finished secondary recording actions show departure repair actions before `onTrackAction`.
- Active/paused recordings keep the existing track action.
- Unit tests, OpenSpec validation, and diff checks pass.

---

## File Structure

- Add `android-app/src/test/java/com/trailmate/app/core/model/TrackRecordingDepartureGateEngineTest.kt`
- Add `android-app/src/main/java/com/trailmate/app/core/model/TrackRecordingDepartureGateEngine.kt`
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Add OpenSpec files under `openspec/changes/trailmate-p1-track-action-departure-gate/`

## Task 1: Model Gate

- [x] Write RED tests for idle, finished, recording, paused, ready, and unknown repair cases.
- [x] Implement the minimal presentation engine.
- [x] Verify targeted tests turn GREEN.

## Task 2: UI Wiring

- [x] Build the gated action inside `RouteCockpitTabContent`.
- [x] Pass the gated label, enabled state, and handler into `GpsTrackPanel`.
- [x] Keep lower-level recording permission checks inside the existing `onTrackAction` path.

## Task 3: Final Gates

- [x] Mark OpenSpec tasks complete.
- [x] Run `:android-app:testDebugUnitTest :trailmate-server:test`.
- [x] Run `openspec validate --all --strict`.
- [x] Run `git diff --check`.
- [x] Request read-only code review before commit.
