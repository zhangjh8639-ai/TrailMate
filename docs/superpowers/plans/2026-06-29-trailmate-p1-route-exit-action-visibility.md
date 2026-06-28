# Route Exit Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Route exit guidance panel's button visibility explicit and testable so recommendations do not look like executable navigation actions.

**Assumptions:**
- `RouteExitGuidanceTone.READY` means the panel should display guidance options, not a tappable action.
- `RouteExitGuidanceTone.CAUTION` currently represents repair states such as refreshing location.
- Blank action labels should never render as buttons.

**Success Criteria:**
- READY route-exit guidance hides the primary button.
- CAUTION route-exit guidance with a non-blank action label shows the button.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature test for Route exit button visibility.

## Task 2: Implementation

- [x] Add `RouteExitGuidancePanelButtonPresentationEngine`.
- [x] Use the policy in `RouteExitGuidancePanel`.

## Task 3: Verification

- [x] Run targeted feature/model tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
