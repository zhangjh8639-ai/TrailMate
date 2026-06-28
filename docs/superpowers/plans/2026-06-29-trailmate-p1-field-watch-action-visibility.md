# Field Watch Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure route-page field safety watch buttons are shown only for real executable actions.

**Assumptions:**
- Daylight and progress watch cards use safety sharing as their only executable route-page action.
- Manual review/rest labels are guidance copy, not tappable actions.
- Blank labels should never render an empty button.

**Success Criteria:**
- Button visibility is covered by feature tests.
- Daylight and progress watch cards use the shared button policy.
- Manual guidance labels remain visible as non-clickable card text.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature tests for field-watch button visibility.

## Task 2: Implementation

- [x] Add `FieldSafetyWatchPanelButtonPresentationEngine`.
- [x] Wire daylight and progress watch panels to the shared policy.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
