# Backtrack Breadcrumb Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Backtrack breadcrumb panel actions visible only when they map to a real Route diagnostics callback.

**Assumptions:**
- READY and FINISHED breadcrumb states can route to track review.
- ALERT stale breadcrumb states should refresh location.
- PAUSED breadcrumb states should route through the existing track recording action only when it is enabled.
- Warming-up and unavailable states should remain guidance-only.

**Success Criteria:**
- Backtrack breadcrumb action mapping is covered by feature tests.
- The Route diagnostics panel uses the action mapping instead of ignoring `primaryActionLabel`.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature tests for breadcrumb action visibility and kind.

## Task 2: Implementation

- [x] Add `BacktrackBreadcrumbGuidancePanelButtonPresentationEngine`.
- [x] Wire `BacktrackBreadcrumbGuidancePanel` to existing callbacks.

## Task 3: Verification

- [x] Run targeted feature/model tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
