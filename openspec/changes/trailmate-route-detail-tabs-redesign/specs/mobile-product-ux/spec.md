## ADDED Requirements

### Requirement: Route Detail Tabs Have Distinct Jobs

TrailMate SHALL organize route detail tabs around distinct user jobs: route suitability, field navigation, trip planning, and route gear readiness.

#### Scenario: Assessment tab is a decision surface

- **GIVEN** a user opens route detail for an imported route
- **WHEN** the user views the `评估` tab
- **THEN** the screen presents a clear suitability decision, confidence, route metrics, key risks, and next actions
- **AND** it does not expose raw AI prompts, body metrics, historical evidence bundles, or debug diagnostics

#### Scenario: Each tab avoids duplicated primary content

- **GIVEN** the route has assessment, plan, map, and gear data
- **WHEN** the user switches between the four tabs
- **THEN** `评估` focuses on suitability, `路线` focuses on field navigation, `计划` focuses on supply/rest/risk timing, and `装备` focuses on route equipment readiness
- **AND** the same large map or diagnostic panel is not reused as the primary content across multiple tabs

### Requirement: First-Use Map And Location Preparation

TrailMate SHALL explain map, location, and recording permissions before they appear as route-tab blockers.

#### Scenario: AMap privacy consent is handled before SDK map initialization

- **GIVEN** AMap online map support is configured
- **WHEN** the user first enters a map-enabled route experience
- **THEN** TrailMate presents user-facing map service consent before initializing AMap SDK map components
- **AND** the user may continue with local GPX route preview if consent is not accepted

#### Scenario: Android runtime permissions are action-triggered

- **GIVEN** the user has not granted location or notification permission
- **WHEN** the user opens the app or views route assessment
- **THEN** TrailMate may explain why permissions are useful without launching all system permission prompts
- **AND** foreground location permission is requested when the user starts hiking or explicitly chooses to use current location
- **AND** notification permission is requested when the user starts track recording and the OS requires it

### Requirement: Route Tab Is A Map-First Light-Navigation Cockpit

TrailMate SHALL present the `路线` tab as a map-first field cockpit with user-facing status language.

#### Scenario: First viewport prioritizes navigation

- **GIVEN** the user selects the `路线` tab
- **WHEN** the route has usable GPX geometry
- **THEN** the first viewport shows a large route map, current or estimated position, current checkpoint, next checkpoint, route progress, and one primary action
- **AND** secondary actions include safety share, mark checkpoint, map/layer/offline access, and finish when relevant

#### Scenario: SDK diagnostics are not default user content

- **GIVEN** AMap key, SDK, package binding, privacy consent, or launch diagnostics are relevant
- **WHEN** the user views the default `路线` tab
- **THEN** TrailMate uses user-facing map status such as `在线底图可用`, `当前使用本地路线`, or `地图服务需同意后启用`
- **AND** it does not display package name, SHA1, API key status, SDK linkage, MapView, or diagnostics grids in the default route cockpit

#### Scenario: Primary action follows field state

- **GIVEN** location, hike session, track recording, and route-match state are available
- **WHEN** the state changes between not-authorized, located-ready, recording, paused, off-route, and completed
- **THEN** the primary action changes respectively to starting the hike, using current location, pausing, resuming, viewing recovery advice, or reviewing the recorded track

### Requirement: Plan Tab Presents Trip Rhythm

TrailMate SHALL present the `计划` tab as a supply, rest, and risk timeline.

#### Scenario: Weather is not overstated

- **GIVEN** TrailMate has no authoritative live weather provider for the route
- **WHEN** the user views route plan readiness
- **THEN** the plan uses review language such as `天气待复核`
- **AND** it does not claim weather is stable without source-backed evidence

#### Scenario: Checkpoints are action-oriented

- **GIVEN** TrailMate has generated route checkpoints
- **WHEN** the user views the `计划` tab
- **THEN** each checkpoint presents distance, timing, action recommendation, and risk type in a scannable timeline

### Requirement: Gear Tab Presents A Route Checklist

TrailMate SHALL present the `装备` tab as a route-specific checklist with personal gear matching.

#### Scenario: Gear recommendations are grouped by readiness

- **GIVEN** TrailMate has route gear recommendations and the user's inventory
- **WHEN** the user views the `装备` tab
- **THEN** required, check, missing, optional, and matched items are visually distinguishable
- **AND** each item can show the matched brand/model or an add-existing-gear action

#### Scenario: AI support is visible but not dominant

- **GIVEN** AI or fallback gear advice is available
- **WHEN** the user views the `装备` tab
- **THEN** AI advice is presented as a small support/Beta signal
- **AND** the primary page identity remains the route equipment checklist
