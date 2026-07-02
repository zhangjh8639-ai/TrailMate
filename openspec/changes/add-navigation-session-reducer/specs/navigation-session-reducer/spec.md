## ADDED Requirements

### Requirement: Runtime reducer handles navigation lifecycle actions
The system SHALL provide a side-effect-free reducer that updates active navigation runtime state from explicit user actions.

#### Scenario: Start navigation creates active runtime state
- **WHEN** the reducer receives a start action with a private navigation session and route geometry
- **THEN** it returns runtime state with the session in `Navigating`, no snapshot yet, no off-route evidence yet, and no stored progress yet

#### Scenario: Pause and resume preserve prior navigation mode
- **WHEN** the reducer receives pause and then resume actions while the session is in a navigable state
- **THEN** it restores the state that was active before pause

#### Scenario: End navigation is terminal
- **WHEN** the reducer receives an end action
- **THEN** later location samples and resume actions MUST NOT move the session out of `Ended`

### Requirement: Runtime reducer converts location samples into snapshots
The system SHALL process reliable location samples through the navigation snapshot engine while carrying evidence and progress forward.

#### Scenario: Location sample updates snapshot and progress
- **WHEN** an active navigation runtime state receives a location sample
- **THEN** it stores the latest snapshot, off-route evidence, and route progress from the snapshot engine result

#### Scenario: Repeated off-route samples confirm deviation
- **WHEN** a later off-route location sample follows previous suspected off-route evidence for the configured duration and sample count
- **THEN** the runtime state reports a confirmed off-route snapshot

### Requirement: Runtime reducer respects safety-critical manual states
The system SHALL preserve user-selected safety states unless an explicit user action exits them.

#### Scenario: Original return is not overwritten by on-route location
- **WHEN** the user enters original-return mode and the next location sample is on the planned route
- **THEN** the runtime state remains `ReturningOnTrack`

#### Scenario: Poor GPS does not create nearest-route guidance
- **WHEN** a location sample has accuracy worse than the configured threshold
- **THEN** the runtime state keeps its session state and does not expose deviation distance or nearest-route guidance from that sample

### Requirement: Runtime reducer remains platform independent
The reducer MUST NOT request permissions, start services, access storage, access network, render maps, or read sensors directly.

#### Scenario: Pure reducer API
- **WHEN** the reducer is called with action objects and current state
- **THEN** it returns a new runtime state without performing Android, IO, network, map, or persistence side effects
