# Route Deviation Recovery Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure route-deviation recovery panel buttons are driven by explicit executable action kinds rather than Chinese label string comparisons.

**Assumptions:**
- Rejoined recovery can acknowledge the rejoin state from the route page.
- Low-accuracy and unavailable safety-share recovery can request a fresh location from the route page.
- Off-route recovery can share location only when the safety-share text is available.
- Labels should match the action that will actually run.

**Success Criteria:**
- Recovery button routing is covered by feature tests.
- RouteDeviationRecoveryPanel uses a button presentation policy.
- The route page dispatches recovery actions through explicit action kinds.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature tests for route-deviation recovery button routing.

## Task 2: Implementation

- [x] Add `RouteDeviationRecoveryPanelButtonPresentationEngine`.
- [x] Wire `RouteDeviationRecoveryPanel` to the button policy.
- [x] Replace label-string action branching with action-kind branching.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
