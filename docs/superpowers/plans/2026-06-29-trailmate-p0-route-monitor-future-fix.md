# Route Monitor Future Fix Guard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent future-timestamp recorded GPS points from triggering route-deviation alerts.

**Architecture:** Keep the guard in `TrackRecordingRouteMonitorEngine`, where recorded track points are converted into route-deviation decisions. The policy layer remains unchanged; it already knows how to present a wait-for-reliable-fix decision once the monitor refuses unreliable points.

**Tech Stack:** Kotlin, Android app unit tests, OpenSpec.

---

## Task 1: RED Test

- [x] Add `matchingRouteWithFutureTimestampPointWaitsForReliableFix` in `TrackRecordingRouteMonitorEngineTest`.
- [x] Run the targeted test and confirm it fails because a future point currently triggers the off-route path.

## Task 2: Implementation

- [x] Update `RecordedTrackPoint.isFresh` to require `timestampEpochMillis > 0L`.
- [x] Update `RecordedTrackPoint.isFresh` to require `timestampEpochMillis <= nowEpochMillis`.
- [x] Keep the existing maximum age check.

## Task 3: Verification

- [x] Run targeted route-monitor tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
