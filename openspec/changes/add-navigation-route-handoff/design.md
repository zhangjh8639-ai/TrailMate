## Context

TrailMate is being rebuilt as an Android-native hiking track navigation app. The route asset center supports GPX/KML import, local persistence, and route detail inspection. The Navigation tab still shows a placeholder card, so a user cannot yet carry a selected route into the navigation surface.

The current app uses a small Compose shell with local state rather than Navigation Compose, ViewModels, or DI. This slice should add the handoff state and a production-honest pre-navigation screen without pretending that GPS navigation exists.

## Goals / Non-Goals

**Goals:**

- Let route detail select a route asset for the Navigation tab.
- Show the Navigation tab in a route-ready state with route facts and safety/availability boundaries.
- Keep imported route copy explicit: local private, track-only, unverified, and no commercial/full offline basemap.
- Avoid fake active-navigation behavior: no GPS permission request, no session start, no recording, no off-route claims.
- Preserve existing route import preview and route detail behavior.
- Cover pure state behavior with tests before implementation.

**Non-Goals:**

- Start a `NavigationSession` or dispatch `NavigationEvent.StartNavigation`.
- Add MapLibre, live location, background location, foreground service, route recording, off-route alerting, emergency card, or power management.
- Add arbitrary route planning, complex pretrip checks, equipment, community, marketplace, or social surfaces.
- Introduce Navigation Compose, ViewModels, Hilt, or a full routing architecture in this PR.

## Decisions

1. Introduce a lightweight `NavigationTabState` derived from route asset/detail data.
   - Rationale: route assets already contain the visible facts needed for a pre-navigation ready screen.
   - Alternative considered: load full route domain objects from a repository. That is the right long-term shape, but the app has no route repository yet and this PR should stay focused on handoff UX.

2. Keep route handoff as app shell state.
   - Rationale: the current app shell already owns selected tab and route state; adding one selected navigation route keeps the change contained.
   - Alternative considered: add Navigation Compose and route args. Useful later, but too much framework movement for a small handoff slice.

3. Use careful action language: "选择为导航路线" and "等待开始" rather than "开始导航".
   - Rationale: real navigation requires GPS, permissions, foreground service, and session persistence. This slice should not imply those are present.

4. Keep Navigation tab bottom navigation visible.
   - Rationale: this is a pre-navigation ready state, not active map navigation. Full-screen navigation can be decided when real active navigation is implemented.

## Risks / Trade-offs

- Route-ready state is based on asset snapshots, not full route geometry -> It is sufficient for pre-navigation confirmation; real navigation must later require route geometry and session setup.
- Users may expect a start button after selecting a route -> Use honest copy and no active start action until GPS/session support exists.
- Imported route readiness could be mistaken for offline map readiness -> Show track-only and no-basemap notes prominently.
- App shell state is temporary -> Keep state small and pure so a later ViewModel/navigation migration can absorb it.

## Migration Plan

- No data migration is required.
- Existing route assets and details remain unchanged except for adding the route handoff action.
- Rollback is safe: removing this feature only removes UI state and route handoff, not persisted route data.

## Open Questions

- Whether the active navigation PR should create sessions from route asset IDs, persisted route records, or full route domain objects.
- Whether platform and imported routes should have different readiness gates once GPS and offline package checks exist.
