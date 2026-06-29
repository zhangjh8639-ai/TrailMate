# Off-route Recovery Safe-exit Option Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a visible safe-exit review action to reliable off-route recovery guidance.

**Architecture:** Keep the behavior in the pure recovery presentation engine and reuse the existing route exit guidance panel. The UI change is limited to assigning an icon for the new action kind.

**Tech Stack:** Kotlin, Android Gradle unit tests, OpenSpec local change drafts.

---

### Task 1: Reliable Off-route Safe-exit Action

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteDeviationRecoveryEngine.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p0-offroute-recovery-exit-option/*`

- [ ] **Step 1: Write the failing test**

Add an assertion to reliable off-route recovery that expects an action labelled `查看安全退出` with copy mentioning 原路返回, 下一检查点, and 实走轨迹.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationRecoveryEngineTest.presentsOffRouteRecoveryStepsWhenRouteCheckFails"`

Expected: FAIL because the current recovery actions do not include safe-exit review.

- [ ] **Step 3: Write minimal implementation**

Add `REVIEW_SAFE_EXIT` to `RouteDeviationRecoveryActionKind`, insert the action after `RETURN_TO_ROUTE`, and map it to a route/map glyph.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteDeviationRecoveryEngineTest"`

Expected: PASS.

- [ ] **Step 5: Verify and review**

Run: `openspec validate trailmate-p0-offroute-recovery-exit-option --strict`, `git diff --check`, and `.\gradlew.bat test`; then request read-only code review before PR.
