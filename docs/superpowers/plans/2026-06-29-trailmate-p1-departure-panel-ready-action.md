# Departure Readiness Panel Ready Action Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Route diagnostics Departure readiness panel start a ready hike and request recording when its label is `开始徒步并记录轨迹`.

**Assumptions:**
- The main cockpit action is already correct.
- The diagnostics panel should use the same repair-label semantics as the main action.
- Recording must still pass through the existing permission/location gate.

**Success Criteria:**
- A ready departure panel action resolves to start-hike-and-record.
- Repair labels still resolve to repair actions.
- Route UI calls `onSessionChange(HikeSessionEngine.start(...))` and then `onTrackAction()` only for the ready action.

---

## Task 1: Model Action Mapping

- [x] Add RED tests for start, route pack, base map, location, system location, gear, and unknown labels.
- [x] Implement the minimal model.
- [x] Reuse the model in track departure gate repair mapping where practical.

## Task 2: Route UI Wiring

- [x] Resolve the departure panel action once in `RouteCockpitTabContent`.
- [x] Replace ad hoc label checks in `DepartureReadinessPanel` click handling.
- [x] Keep recording routed through existing `onTrackAction`.

## Task 3: Verification

- [x] Run targeted unit tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation and diff check.
- [x] Request read-only code review before commit.
