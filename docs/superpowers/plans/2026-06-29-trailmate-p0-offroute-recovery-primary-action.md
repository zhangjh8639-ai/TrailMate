# Off-route Recovery Primary Action Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the route cockpit primary action open recovery guidance before routine recording controls when the active or paused hiker is off route or just rejoined.

**Architecture:** Keep the change in the pure presentation engine so it remains deterministic, fast to test, and independent of Compose UI rendering. Existing screen handlers already understand `VIEW_RECOVERY`.

**Tech Stack:** Kotlin, Android Gradle unit tests, OpenSpec local change drafts.

---

### Task 1: Recovery CTA Priority

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/RouteCockpitPresentationEngineTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/RouteCockpitPresentationEngine.kt`
- Create: `openspec/changes/trailmate-p0-offroute-recovery-primary-action/*`

- [ ] **Step 1: Write the failing test**

Change the existing off-route recording test so `RouteCockpitPrimaryActionKind.VIEW_RECOVERY` is expected, add one recent-rejoin test with recording still active, and add one READY test proving departure gates remain primary.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteCockpitPresentationEngineTest"`

Expected: FAIL because current code returns `PAUSE_RECORDING`.

- [ ] **Step 3: Write minimal implementation**

Move the active/paused `locationGuidanceStatus == CHECK_ROUTE || wasRecentlyOffRoute` branch above the live recording branch in `RouteCockpitPresentationEngine.primaryAction`.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.RouteCockpitPresentationEngineTest"`

Expected: PASS.

- [ ] **Step 5: Verify and review**

Run: `openspec validate trailmate-p0-offroute-recovery-primary-action --strict`, `git diff --check`, and `.\gradlew.bat test`; then request read-only code review before PR.
