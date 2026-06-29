# Safety Watch Share Availability Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure progress, daylight-return, and return-ETA safety-watch buttons do not show a share action when TrailMate first needs a fresh location.

**Assumptions:**
- Safety-watch panels should keep their original share label only when safety-share text is available.
- When share text is unavailable, these panels should reuse the repair label from `RouteSafetyShareShortcutPresentation`.
- Click handling should use explicit action kinds, not display labels.
- This slice should not change risk thresholds or panel layout.

**Success Criteria:**
- Field safety-watch button availability is covered by tests.
- Return-ETA button availability is covered by tests.
- Route page dispatches field and return-ETA safety-watch actions through explicit action kinds.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing tests for field safety-watch share availability.
- [x] Add failing tests for return-ETA share availability.

## Task 2: Implementation

- [x] Add `FieldSafetyWatchPanelActionKind`.
- [x] Add `ReturnEtaWatchPanelActionKind`.
- [x] Wire progress and daylight panels to `RouteSafetyShareShortcutPresentation`.
- [x] Wire return-ETA panel to `RouteSafetyShareShortcutPresentation`.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
