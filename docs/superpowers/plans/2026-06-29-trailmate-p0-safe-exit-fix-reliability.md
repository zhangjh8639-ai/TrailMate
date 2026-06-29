# Safe-exit Fix Reliability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent safe-exit guidance from recommending exit directions from stale, future, low-accuracy, or malformed location fixes.

**Architecture:** Keep the behavior in the pure route-exit presentation engine and pass deterministic presentation time from the Compose route screen. Reuse the existing low-confidence presentation.

**Tech Stack:** Kotlin, Android Gradle unit tests, OpenSpec local change drafts.

---

### Task 1: Safe-exit Fix Reliability

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteExitGuidanceEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteExitGuidanceEngine.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
- Create: `openspec/changes/trailmate-p0-safe-exit-fix-reliability/*`

- [ ] **Step 1: Write the failing tests**

Add tests proving stale, future, negative-accuracy, NaN-progress, and low-accuracy on-route fixes return `先稳定定位` and no emphasized route option.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteExitGuidanceEngineTest"`

Expected: FAIL because current safe-exit guidance trusts these fixes.

- [ ] **Step 3: Write minimal implementation**

Add `nowEpochMillis` to `RouteExitGuidanceEngine.present`, reject unreliable fixes before progress calculation, and pass `locationPresentationNowEpochMillis` from `RouteDetailScreen`.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteExitGuidanceEngineTest"`

Expected: PASS.

- [ ] **Step 5: Verify and review**

Run: `openspec validate trailmate-p0-safe-exit-fix-reliability --strict`, `git diff --check`, and `.\gradlew.bat test`; then request read-only code review before PR.
