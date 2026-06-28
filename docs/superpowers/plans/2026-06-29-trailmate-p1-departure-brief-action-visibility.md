# Departure Brief Action Visibility Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove misleading unavailable actions from the Route diagnostics departure brief panel while keeping useful safety context visible.

**Assumptions:**
- A departure brief action is only immediately executable when `DepartureBriefSharePresentation.shareText` is present.
- The panel should still explain why a brief cannot be sent.
- This change should stay scoped to presentation policy and Compose rendering.

**Success Criteria:**
- Sendable departure brief payloads show the existing send button.
- Missing-duration or completed states do not show disabled pseudo-navigation buttons.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature test for departure brief button visibility.

## Task 2: Implementation

- [x] Add a small `DepartureBriefSharePanelButtonPresentationEngine`.
- [x] Render the panel action only when the presentation policy marks it visible.

## Task 3: Verification

- [x] Run targeted feature test.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
