## Context

The repository now has a runnable Android shell with the five product tabs, but route and navigation concepts still exist only as UI placeholder copy and the Lovable React prototype. TrailMate needs production-grade route navigation later: GPX/KML import, route projection, off-route detection, foreground tracking, emergency card, records, and feedback. Those features should share one domain vocabulary instead of each feature inventing its own model.

The first domain slice must stay small enough to review. It should define models, defaults, and a light navigation state reducer, while leaving algorithms, persistence, services, and UI binding to later PRs.

## Goals / Non-Goals

**Goals:**

- Create `com.trailmate.app.core.model` as the initial Android domain layer.
- Define explicit units for coordinates, distance, elevation, duration, accuracy, battery, and timestamps through small value objects or typed fields.
- Model route assets: route metadata, geometry summary, waypoints, risk points, exit points, source type, offline status, confidence, and difficulty.
- Model navigation: session, state, transition events, snapshot, nearest-route-point guidance, and safe off-route copy.
- Preserve paused safety state so resuming from suspected or confirmed off-route does not silently downgrade to normal navigation.
- Model safety and history: emergency card, track point, route record, route feedback, and feedback category.
- Make privacy defaults executable and tested.
- Keep all tests JVM-only for fast review.

**Non-Goals:**

- No GPS provider, foreground service, Room schema, DataStore, networking, MapLibre, GPX/KML parser, route projection, off-route algorithm, or UI screen implementation.
- No standalone planning, equipment, community, marketplace, or complex pre-trip-check domain.
- No persistence annotations or serialization format commitment in this PR.

## Decisions

### Decision 1: Put the first domain layer inside the app module

Use `app/src/main/java/com/trailmate/app/core/model/**` for this PR instead of creating a separate Gradle module.

Rationale: The repo currently has a single app module. Adding a separate `:core:model` module before there is enough shared code would increase Gradle complexity and review scope. The package name still creates a clean future extraction path.

Alternative considered: create `core/model` as a separate module now. Rejected for this slice because it would require additional Gradle wiring and is not necessary to test pure Kotlin domain behavior.

### Decision 2: Keep route models immutable Kotlin data classes

Use `data class` and `enum class` values with constructor validation where needed. Avoid Android framework types in the model layer.

Rationale: Navigation algorithms and import parsers need models that work in JVM unit tests and can later be persisted or serialized without UI/runtime dependencies.

Alternative considered: use UI-oriented models directly in Compose. Rejected because UI text and layout needs will change more often than route/session semantics.

### Decision 3: Make privacy defaults explicit factory values

Use `PrivacyVisibility.Private` defaults for imported routes, personal tracks, saved routes, and navigation sessions. Do not rely on UI callers to remember privacy defaults.

Rationale: TrailMate handles sensitive location data. Defaults must be enforced in the model layer before persistence and sharing features exist.

Alternative considered: document privacy defaults only. Rejected because documentation alone is too easy to bypass when new features are added.

### Decision 4: Add a reducer but not navigation algorithms

Create a small reducer for allowed navigation state transitions (`StartNavigation`, `SuspectOffRoute`, `ConfirmOffRoute`, `ReturnOnTrack`, `Pause`, `Resume`, `End`) without computing route progress or off-route status.

Rationale: Product state semantics can be tested now, while geometry belongs in the next `core.geo` slice.

Alternative considered: implement off-route detection in this PR. Rejected to keep this PR focused and TDD-friendly.

### Decision 5: Constrain safety directions and route sources

Use a `CompassDirection` enum for off-route copy and keep `RouteSourceType` to true route origins (`Platform`, `ImportedGpx`, `ImportedKml`).

Rationale: Free-form direction text can inject unsafe instructions into otherwise safe copy, and favorite/recent are asset filters rather than route origins.

Alternative considered: accept free-form strings and UI filters in the same enum. Rejected because it weakens safety guarantees and muddles domain semantics.

## Risks / Trade-offs

- Domain names may shift after GPX and geometry work → Mitigation: keep classes minimal, immutable, and easy to rename before persistence is introduced.
- Single-module package can grow too large → Mitigation: keep `core.model` pure and document future extraction to `:core:model`.
- Reducer without algorithms can feel abstract → Mitigation: tests tie it to product states and safety copy, and geometry work is explicitly the next slice.
- Models are not yet serialized or stored → Mitigation: do not add Room/serialization annotations until persistence/API requirements are concrete.
