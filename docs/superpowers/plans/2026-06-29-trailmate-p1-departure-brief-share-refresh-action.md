# Departure Brief Share Refresh Action Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure the fullscreen route helper recomputes departure-brief share text when the hiker taps send, so long-open screens do not share stale planned departure or expected finish times.

**Assumptions:**
- Departure briefs should remain static route-plan messages without GPS coordinates.
- The existing panel visibility policy is sufficient for this slice.
- Click handling should use a model-level resolver so it can be tested outside Compose.

**Success Criteria:**
- Departure brief click-time recomputation is covered by tests.
- Fullscreen route helper uses the resolver at click time.
- Targeted tests, full Android/server unit tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing tests for click-time departure brief recomputation.
- [x] Add failing tests for unavailable share text at click time.

## Task 2: Implementation

- [x] Add `DepartureBriefShareActionEngine`.
- [x] Wire `RouteDetailScreen` departure brief click handling through the action engine.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
