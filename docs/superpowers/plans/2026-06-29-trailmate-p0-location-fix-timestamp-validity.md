# Location Fix Timestamp Validity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent future or non-positive GPS fix timestamps from unlocking field-use states such as departure, recording, and precise map confidence.

**Architecture:** Keep timestamp validity in `TrailMateLocationFixReliability`, the shared reliability policy used by departure readiness, recording gates, map overlays, and GPS signal watches. Feature-level engines continue to consume the same freshness and age API.

**Tech Stack:** Kotlin, Android app unit tests, OpenSpec.

---

## Task 1: RED Test

- [x] Add `TrailMateLocationFixReliabilityTest.futureTimestampIsNotFreshOrReliableForFieldUse`.
- [x] Add `TrailMateLocationFixReliabilityTest.zeroTimestampIsNotFreshOrReliableForFieldUse`.
- [x] Add `TrackRecordingActionGateEngineTest.waitsForCurrentLocationBeforeStartingTrackRecording`.
- [x] Add `TrailMateLocationFixReliabilityTest.invalidTimestampAgeExceedsReliableWindow`.
- [x] Add provider timestamp preservation tests for Android location conversion.
- [x] Add invalid-timestamp presentation tests for reliability details, GPS watch, and map confidence.
- [x] Add invalid-timestamp tests for track append, route progress, and backtrack breadcrumb guidance.

## Task 2: Implementation

- [x] Require timestamps to be positive in `TrailMateLocationFixReliability.isFresh`.
- [x] Require timestamps to be no later than `nowEpochMillis` in `TrailMateLocationFixReliability.isFresh`.
- [x] Return an out-of-window age from `fixAgeMillis` for invalid timestamps.
- [x] Stop replacing invalid Android provider timestamps with `System.currentTimeMillis()`.
- [x] Show invalid timestamp as recalibration/caution, not as "刚刚" or precise map confidence.
- [x] Reject invalid recorded points in `TrackRecordingEngine.appendLocation`.
- [x] Stop invalid projected fixes from advancing route checkpoints.
- [x] Avoid marking invalid breadcrumb trails as backtrack-ready.

## Task 3: Verification

- [x] Run targeted reliability and recording gate tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
