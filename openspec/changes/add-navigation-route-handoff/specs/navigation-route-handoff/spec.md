## ADDED Requirements

### Requirement: Route detail can select a navigation route
The system SHALL allow a route detail view to hand its route asset to the Navigation tab as the selected route for pre-navigation readiness.

#### Scenario: Select platform route for navigation readiness
- **WHEN** the user chooses a platform route from route detail for navigation readiness
- **THEN** the app switches to the Navigation tab and shows that route as the selected route without starting active navigation

### Requirement: Navigation tab shows selected route readiness
The system SHALL show route facts and safety-relevant boundaries when the Navigation tab has a selected route.

#### Scenario: Navigation ready state shows route facts
- **WHEN** a selected route is available in the Navigation tab
- **THEN** the screen shows route name, source, offline/track status, distance, elevation gain, estimated duration, difficulty, confidence, and risk tags

#### Scenario: Selected route keeps a stable route reference
- **WHEN** the Navigation tab receives a selected route
- **THEN** the readiness state keeps the route key needed to recover the route asset for a later real navigation session

### Requirement: Imported route readiness preserves import boundaries
The system SHALL keep imported GPX/KML route readiness labeled as local private, track-only, unverified, and no-basemap.

#### Scenario: Imported route ready state is bounded
- **WHEN** an imported route is selected for navigation readiness
- **THEN** the Navigation tab indicates the route is本机私密, only the track is available, confidence is待确认, and the import does not include a commercial or full offline map basemap

### Requirement: Route handoff does not start active navigation
The system MUST NOT start GPS, location permission flows, foreground tracking, route recording, or a `NavigationSession` from this handoff.

#### Scenario: Handoff remains pre-navigation
- **WHEN** the user selects a route for navigation readiness
- **THEN** the visible copy indicates待开始 or route preparation and does not show "开始导航" or "开始轨迹导航" actions in this change

### Requirement: Navigation idle fallback remains available
The system SHALL show a clear idle Navigation tab state when no route has been selected.

#### Scenario: No route selected
- **WHEN** the user opens the Navigation tab before selecting a route
- **THEN** the screen explains that a route can be selected from the Routes tab or imported GPX/KML, without adding planning, equipment, community, marketplace, or pretrip-check flows

### Requirement: Navigation ready state can return to route selection
The system SHALL provide a safe non-navigation action from the pre-navigation ready state back to route selection.

#### Scenario: User changes selected route
- **WHEN** a selected route is shown in the Navigation tab readiness state
- **THEN** the screen offers a change-route action and does not start GPS, tracking, or route recording

### Requirement: Deprecated product scope stays absent
The system MUST NOT add planning, equipment, community, marketplace, or complex pretrip-check surfaces to the Navigation handoff.

#### Scenario: Navigation handoff visible copy stays within track navigation scope
- **WHEN** Navigation handoff visible text is inspected
- **THEN** it does not contain deprecated product surfaces such as 规划, 装备, 社区, 商城, or 出发前检查
