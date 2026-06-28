# Route Safety-Share Shortcut Action Label Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ensure route-page safety-share shortcut buttons say and do the same thing in normal cockpit and fullscreen navigation.

**Assumptions:**
- The compact shortcut can keep the shorter "安全分享" label when share text is already available.
- If safety-share text is unavailable, the shortcut should show the existing repair label from `SafetySharePresentation`.
- Missing, stale, or unreliable location should request location before any share attempt.
- Located and low-accuracy fixes must continue aging while the route page stays open, otherwise the shortcut can show stale share availability.
- This slice should not redesign the route cockpit layout.

**Success Criteria:**
- Safety-share shortcut labels and action kinds are covered by feature tests.
- Located fix presentation clock refresh is covered by feature tests.
- Route cockpit and fullscreen navigation use the shared shortcut policy.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add failing feature tests for route safety-share shortcut label and action kind.

## Task 2: Implementation

- [x] Add `RouteSafetyShareShortcutPresentationEngine`.
- [x] Add `RouteLocationPresentationClockPolicy`.
- [x] Wire fullscreen navigation shortcut to the policy.
- [x] Wire route cockpit shortcuts to the policy.
- [x] Wire route location presentation clock to refresh located and low-accuracy fixes.

## Task 3: Verification

- [x] Run targeted feature tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
