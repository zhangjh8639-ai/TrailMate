## ADDED Requirements

### Requirement: Bottom Tab Responsibilities

TrailMate SHALL keep each bottom tab focused on a single production user job.

#### Scenario: Home is the route preparation start point

- **GIVEN** the user opens the app after onboarding
- **WHEN** the home tab is displayed
- **THEN** the first viewport focuses on importing or continuing a target route
- **AND** raw historical evidence, body metrics, AI prompt content, and diagnostics are not displayed in the primary home surface

#### Scenario: Route tab manages routes

- **GIVEN** the user opens the route tab
- **WHEN** target routes exist or are being imported
- **THEN** the page shows current route state, import actions, route list, and import queue when relevant
- **AND** it does not duplicate the full route assessment or personal profile editor

#### Scenario: Data tab reviews activity outcomes

- **GIVEN** the user opens the data tab
- **WHEN** recorded tracks or historical GPX activities exist
- **THEN** the page shows activity review, track replay summary, and ability trend entry points
- **AND** it does not expose raw AI input bundles or account settings as primary content

#### Scenario: Me tab owns settings and privacy

- **GIVEN** the user opens the me tab
- **WHEN** account, profile, permission, map, or data controls are needed
- **THEN** the page provides those settings and data rights controls
- **AND** it does not become a route evaluation, route navigation, or gear checklist surface

### Requirement: Route Detail Task Separation

TrailMate SHALL separate route detail work into assessment, route cockpit, plan, and gear tasks.

#### Scenario: Assessment answers suitability

- **GIVEN** the user opens route detail
- **WHEN** the assessment tab is selected
- **THEN** the first viewport shows match level, estimated duration, key risks, confidence, and one next action
- **AND** raw evidence and prompt material remain collapsed or absent

#### Scenario: Route tab prioritizes field use

- **GIVEN** the user opens route detail
- **WHEN** the route tab is selected
- **THEN** the first viewport shows map, current or estimated position, current checkpoint, next checkpoint, primary action, and readiness strip
- **AND** implementation diagnostics remain lower priority than field controls

#### Scenario: Plan tab presents checkpoint timeline

- **GIVEN** a route plan exists
- **WHEN** the plan tab is selected
- **THEN** the page presents rest, energy, hydration, risk, and turnaround checks as a timeline
- **AND** wording remains advisory and does not guarantee safety

#### Scenario: Gear tab presents route-specific preparation

- **GIVEN** route gear recommendations exist
- **WHEN** the gear tab is selected
- **THEN** the page shows route-critical gear needs, matched server catalog candidates, and missing-category guidance
- **AND** it does not show marketplace, affiliate, or unsupported brand recommendations

### Requirement: Evidence And AI Inputs Stay Backgrounded

TrailMate SHALL use user evidence and AI inputs to improve outputs without turning them into primary UI content.

#### Scenario: Evidence informs results without cluttering screens

- **GIVEN** TrailMate has baseline profile, body metrics, historical GPX, route data, and gear checklist/catalog context
- **WHEN** the user views Home, Route, Route Detail, Gear, Data, or Me
- **THEN** those inputs may affect assessments, readiness, plan, and gear outputs
- **AND** the app does not display raw evidence bundles, full body metrics, or AI prompt payloads as main content

#### Scenario: Explainability remains available

- **GIVEN** the user wants to understand a recommendation
- **WHEN** they open a rationale or details affordance
- **THEN** TrailMate may show concise summarized reasons tied to route facts, ability confidence, gear status, or data availability
- **AND** it must avoid medical advice, rescue promises, or unsupported certainty

### Requirement: Production Visual System

TrailMate SHALL use a coherent mobile visual system based on field-ready hiking patterns.

#### Scenario: Visual hierarchy reflects task priority

- **GIVEN** a screen has a primary task
- **WHEN** it is rendered on a phone viewport
- **THEN** the largest visual element and only primary CTA support that task
- **AND** secondary diagnostics, settings, and explanations do not compete with the first-viewport action

#### Scenario: Colors carry consistent meaning

- **GIVEN** a status, warning, map, or navigation element is shown
- **WHEN** color is used
- **THEN** moss green means primary/ready, amber or clay means caution/missing, blue means location/map/track progress, and neutral surfaces carry background content
- **AND** the app avoids decorative one-note palettes and excessive gradients
