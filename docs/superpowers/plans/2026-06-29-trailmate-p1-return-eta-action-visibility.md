# Return ETA Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure return-ETA buttons are shown only for real executable safety-share actions while passive labels remain visible as text.

**Assumptions:**
- Return ETA currently only has one route-page executable action: safety sharing.
- Save recap, view assessment, start-after-hike, and continue-observing labels are passive guidance in the current route page.
- Blank labels should never render empty buttons.

**Success Criteria:**
- Return-ETA action visibility is covered by feature tests.
- ReturnEtaWatchPanel uses the button policy.
- Passive guidance labels remain visible as non-clickable card text.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature tests for return-ETA button visibility.

## Task 2: Implementation

- [x] Add `ReturnEtaWatchPanelButtonPresentationEngine`.
- [x] Wire `ReturnEtaWatchPanel` to the button policy.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
