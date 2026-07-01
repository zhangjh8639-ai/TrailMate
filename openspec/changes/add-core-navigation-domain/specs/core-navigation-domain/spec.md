## ADDED Requirements

### Requirement: Route domain models
The system SHALL define pure Kotlin route domain models for navigation-ready trail routes without depending on Compose, Android framework location classes, MapLibre, network DTOs, or persistence annotations.

#### Scenario: Route metadata uses explicit navigation fields
- **WHEN** a route is represented in the domain layer
- **THEN** it includes identity, name, region, route type, distance, elevation gain, estimated duration, difficulty, confidence, route version, last updated time, source type, offline status, risk tags, recent feedback, and privacy visibility

#### Scenario: Route geometry keeps navigation inputs separate from UI copy
- **WHEN** route geometry is represented
- **THEN** it stores coordinates, optional elevation, cumulative distance, waypoints, risk points, and exit points as data rather than localized UI strings

### Requirement: Privacy defaults
The system SHALL default sensitive trail data to private visibility.

#### Scenario: Imported route is private by default
- **WHEN** an imported route domain object is created through its default factory
- **THEN** its visibility is private

#### Scenario: Navigation session is private by default
- **WHEN** a navigation session is created through its default factory
- **THEN** its visibility is private

#### Scenario: Route record is private by default
- **WHEN** a completed route record is created through its default factory
- **THEN** its visibility is private

### Requirement: Navigation state semantics
The system SHALL define navigation state values and a reducer for allowed high-level state transitions.

#### Scenario: Navigation can start from idle
- **WHEN** the reducer receives a start-navigation event while idle
- **THEN** the resulting state is navigating

#### Scenario: Confirmed off-route requires suspected off-route first
- **WHEN** the reducer receives a confirm-off-route event while navigating
- **THEN** the state does not become confirmed off-route

#### Scenario: Returning on track starts from suspected or confirmed off-route
- **WHEN** the reducer receives a return-on-track event while suspected or confirmed off-route
- **THEN** the resulting state is returning on track

#### Scenario: Ended navigation is terminal
- **WHEN** the reducer receives any navigation event after ended
- **THEN** the state remains ended

### Requirement: Safety copy is constrained
The system SHALL provide domain-level safety copy for off-route and emergency surfaces that avoids unsafe promises.

#### Scenario: Confirmed off-route copy avoids direct-return promises
- **WHEN** confirmed off-route guidance text is generated
- **THEN** it does not contain promises such as “安全路线”, “保证安全”, or “直行返回”

#### Scenario: Confirmed off-route copy uses constrained compass direction
- **WHEN** confirmed off-route guidance text is generated
- **THEN** the direction input is selected from a domain compass direction rather than free-form caller text

#### Scenario: Emergency card copy avoids rescue promises
- **WHEN** emergency card helper text is generated
- **THEN** it does not claim that rescue was automatically contacted or is on the way

### Requirement: Legacy product scope stays absent
The system SHALL NOT introduce route planning, equipment, community, marketplace, or complex pre-trip-check domain models in this slice.

#### Scenario: Domain package excludes deprecated surfaces
- **WHEN** the domain source tree is inspected
- **THEN** it does not contain planner, equipment, community, marketplace, or pretrip-check packages or model classes
