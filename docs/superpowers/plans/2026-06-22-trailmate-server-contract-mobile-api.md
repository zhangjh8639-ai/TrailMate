# TrailMate Server Contract And Mobile API Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Define the TrailMate backend responsibilities and adjust the Android app to depend on a stable server API contract instead of ad hoc local-only boundaries.

**Architecture:** Keep the Android app offline-first, but introduce explicit remote API ports and DTO contracts for auth, profile, GPX import, route assessment, plans, gear advice, feedback, export, and deletion. The server remains a modular Java/Spring Boot monolith in the plan; this task does not scaffold the full server yet.

**Tech Stack:** Android Kotlin data classes and interfaces, JUnit 4 unit tests, Markdown architecture/API docs, future Spring Boot 3 / Java 21 / PostgreSQL + PostGIS server.

---

### Task 1: Server Scope And API Documentation

**Files:**
- Create: `docs/architecture/trailmate-server-architecture.md`
- Create: `docs/api/trailmate-server-api.md`

- [ ] **Step 1: Write the architecture document**

Create `docs/architecture/trailmate-server-architecture.md` with these sections:

```markdown
# TrailMate Server Architecture

## Purpose

TrailMate uses a C/S architecture. Android owns field interaction, offline route packs, GPS recording, AMap rendering, and local cache. The server owns identity, durable user data, GPX processing, capability profile versions, deterministic route assessment, plan generation, gear-advice orchestration, feedback, export, deletion, and audit evidence.

## Server Modules

| Module | Owns | Does Not Own |
| --- | --- | --- |
| auth | Email/password sign-up, login, token refresh, logout, account deletion authorization | GPX parsing or route scoring |
| user-profile | Onboarding profile, body metrics, outdoor baseline, privacy consent timestamps | Raw GPX files |
| file-import | Multipart GPX upload, async import job, XML safety checks, quality report | Capability profile generation |
| activity | Historical activity records, normalized track summaries, activity deletion | Target route assessment |
| capability | Profile versions, evidence snapshots, deterministic profile recalculation | Rewriting historical assessments |
| route | Target route records, route geometry summary, target region metadata | Personal risk scoring |
| assessment | Match level, confidence, duration range, risk evidence | Medical, rescue, or realtime navigation claims |
| plan | Timing, rest, supply, risk, and rollback checkpoints | Live turn-by-turn navigation |
| gear | User gear inventory and route checklist artifact | Shopping, ads, or marketplace |
| ai-advice | Optional LLM call for explanation/equipment text only after deterministic inputs are fixed | Changing route score, distance, ascent, confidence, or risk facts |
| track | Uploaded completed tracks and recording summaries after a hike | Realtime rescue tracking |
| data-control | Export, deletion, tombstone/audit records | Hidden retention outside policy |
```

Document that the first server milestone should implement auth, file-import, activity, capability, route, assessment, plan, gear, feedback, and data-control before any social or marketplace features.

- [ ] **Step 2: Write the API contract document**

Create `docs/api/trailmate-server-api.md` with route groups and the Android-facing response names:

```markdown
# TrailMate Server API

Base path: `/api/v1`

All authenticated requests use `Authorization: Bearer <accessToken>`. Times are UTC ISO-8601 strings. Errors use `TrailMateApiError`.

| Method | Path | Android operation |
| --- | --- | --- |
| POST | `/auth/register` | `register` |
| POST | `/auth/login` | `login` |
| POST | `/auth/refresh` | `refreshSession` |
| POST | `/auth/logout` | `logout` |
| GET | `/users/me` | `getCurrentUser` |
| PUT | `/users/me/profile` | `saveOnboardingProfile` |
| POST | `/imports/gpx` | `startGpxImport` |
| GET | `/imports/{jobId}` | `getImportJob` |
| POST | `/imports/{jobId}/confirm` | `confirmImport` |
| GET | `/activities` | `listActivities` |
| DELETE | `/activities/{activityId}` | `deleteActivity` |
| POST | `/profiles/recalculate` | `recalculateProfile` |
| GET | `/profiles/current` | `getCurrentCapabilityProfile` |
| GET | `/routes` | `listRoutes` |
| GET | `/routes/{routeId}` | `getRoute` |
| POST | `/routes/{routeId}/assessments` | `createAssessment` |
| GET | `/assessments/{assessmentId}` | `getAssessment` |
| POST | `/assessments/{assessmentId}/plans` | `createPlan` |
| GET | `/plans/{planId}` | `getPlan` |
| POST | `/plans/{planId}/gear-advice` | `requestGearAdvice` |
| POST | `/plans/{planId}/feedback` | `submitCompletionFeedback` |
| POST | `/tracks` | `uploadRecordedTrack` |
| GET | `/exports/me` | `requestAccountExport` |
| DELETE | `/users/me` | `deleteAccount` |
```

Specify that Android can continue with local fallback for route assessment and gear checklist when remote calls time out, but any remote success must include algorithm and assessment fingerprints.

### Task 2: Android Server Contract Tests

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/network/TrailMateServerApiContractTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/network/TrailMateServerApiContract.kt`

- [ ] **Step 1: Write failing endpoint catalog test**

Add a test that asserts the endpoint catalog is stable:

```kotlin
@Test
fun endpointCatalogMatchesServerPlan() {
    assertEquals("/api/v1", TrailMateServerApiContract.BASE_PATH)
    assertEquals("/auth/login", TrailMateServerApiContract.Endpoints.login)
    assertEquals("/imports/gpx", TrailMateServerApiContract.Endpoints.gpxImport)
    assertEquals("/plans/{planId}/gear-advice", TrailMateServerApiContract.Endpoints.gearAdvice)
    assertEquals("/users/me", TrailMateServerApiContract.Endpoints.deleteAccount)
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.network.TrailMateServerApiContractTest
```

Expected: fails because `TrailMateServerApiContract` does not exist.

- [ ] **Step 3: Implement endpoint constants**

Create `TrailMateServerApiContract.kt` with `BASE_PATH`, `Endpoints`, and pure constants only.

- [ ] **Step 4: Run the endpoint test and verify it passes**

Run the same Gradle command. Expected: PASS.

### Task 3: Android Remote Port And DTO Tests

**Files:**
- Modify: `android-app/src/test/java/com/trailmate/app/core/network/TrailMateServerApiContractTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMateServerApiContract.kt`

- [ ] **Step 1: Write failing mobile port test**

Add a test that creates fake DTOs and verifies the Android remote port groups the real product operations:

```kotlin
@Test
fun mobileRemotePortGroupsCoreServerOperations() {
    val operations = TrailMateBackendOperation.entries.map { it.name }.toSet()

    assertTrue("RegisterLogin" in operations)
    assertTrue("ImportGpx" in operations)
    assertTrue("AssessRoute" in operations)
    assertTrue("GeneratePlan" in operations)
    assertTrue("RequestGearAdvice" in operations)
    assertTrue("SubmitFeedback" in operations)
    assertTrue("ExportAndDeleteAccount" in operations)
}
```

- [ ] **Step 2: Run the test and verify it fails**

Expected: fails because `TrailMateBackendOperation` does not exist.

- [ ] **Step 3: Implement backend operation enum and DTO skeletons**

Add `TrailMateBackendOperation`, `TrailMateApiResult`, `TrailMateApiError`, `TrailMateAuthSessionDto`, `TrailMateGpxImportJobDto`, `TrailMateAssessmentDto`, `TrailMatePlanDto`, and `TrailMateBackendApi` interface. Keep the DTOs intentionally small and Android-facing.

- [ ] **Step 4: Run the network contract test**

Expected: PASS.

### Task 4: Connect AI Gear Advice To The Unified API Boundary

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/model/AiGearAdvisorBackendService.kt`
- Modify: `android-app/src/test/java/com/trailmate/app/core/model/AiGearAdvisorBackendServiceTest.kt`

- [ ] **Step 1: Write failing test for unified backend mapping**

Add a test proving `requestGearAdvice` failure maps to retryable local fallback and never changes assessment values.

- [ ] **Step 2: Implement a `TrailMateBackendGearAdvisorClient` adapter**

The adapter should accept `TrailMateBackendApi`, call `requestGearAdvice`, and translate `TrailMateApiResult` into existing `AiGearAdvisorBackendResult`.

- [ ] **Step 3: Run targeted tests**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.network.TrailMateServerApiContractTest --tests com.trailmate.app.core.model.AiGearAdvisorBackendServiceTest
```

Expected: PASS.

### Task 5: Verification And Commit

**Files:**
- Review all files above.

- [ ] **Step 1: Run focused tests**

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests com.trailmate.app.core.network.TrailMateServerApiContractTest --tests com.trailmate.app.core.model.AiGearAdvisorBackendServiceTest
```

- [ ] **Step 2: Run OpenSpec validation for existing product readiness change**

```powershell
npx openspec validate trailmate-production-outdoor-readiness --strict
```

- [ ] **Step 3: Commit**

```powershell
git add docs/architecture docs/api docs/superpowers/plans/2026-06-22-trailmate-server-contract-mobile-api.md android-app/src/main/java/com/trailmate/app/core/network android-app/src/test/java/com/trailmate/app/core/network android-app/src/main/java/com/trailmate/app/core/model/AiGearAdvisorBackendService.kt android-app/src/test/java/com/trailmate/app/core/model/AiGearAdvisorBackendServiceTest.kt
git commit -m "feat(android): define server api contract"
```

## Self-Review

Spec coverage:
- Server modules from `TRAILMATE_CODEX_SPEC.md` are mapped to Android-facing operations.
- Android remains offline-first and does not require server availability for field navigation.
- AI is limited to advice/explanation and cannot rewrite route assessment facts.
- Data export and deletion are included as first-class server operations.

Placeholder scan:
- No `TBD`, `TODO`, or unspecified implementation steps remain.

Type consistency:
- `TrailMateBackendApi`, `TrailMateApiResult`, and operation names are introduced before use by the AI gear adapter task.
