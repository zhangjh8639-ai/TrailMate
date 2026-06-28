# Shared Departure Primary Action Policy Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Route cockpit main action share the same departure readiness primary action policy as the diagnostics departure panel and secondary track action gate.

**Assumptions:**
- `DepartureReadinessPrimaryActionEngine` from the parent branch is the source of truth for readiness label mapping.
- Active recording controls must stay primary.
- Unsupported departure actions should be visible but disabled, not routed to reset-session behavior.

**Success Criteria:**
- Unsupported departure actions produce a disabled `BLOCKED` main action with the original label.
- Existing supported repair actions keep their Route cockpit primary action kinds.
- Targeted tests, full tests, OpenSpec validation, diff check, and read-only review pass.

---

## Task 1: RED Test

- [x] Add a failing Route cockpit test for unsupported departure action fallback.

## Task 2: Implementation

- [x] Add explicit `RouteCockpitPrimaryActionKind.BLOCKED`.
- [x] Convert `DepartureReadinessPrimaryAction` to Route cockpit primary actions.
- [x] Remove duplicate readiness label mapping from `RouteCockpitPresentationEngine`.
- [x] Handle blocked action as no-op in Route UI primary action dispatch.
- [x] Lock blocked fullscreen policy and Departure readiness panel button enabled state after review.

## Task 3: Verification

- [x] Run targeted model tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
