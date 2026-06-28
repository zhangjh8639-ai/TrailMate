## ADDED Requirements

### Requirement: Conservative Route Exit Guidance

TrailMate SHALL provide a conservative route-exit guidance presentation that compares returning to the start with continuing to the next planned route reference when reliable location progress is available.

#### Scenario: Returning to start is closer

- **GIVEN** the hiker has reliable GPS progress on the active route
- **AND** the distance back to the route start is shorter than the distance to the next planned reference
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST recommend returning along the recorded or known route
- **AND** it MUST show the distance back to the start
- **AND** it MUST still show the next planned reference as a secondary option

#### Scenario: Next checkpoint is closer

- **GIVEN** the hiker has reliable GPS progress on the active route
- **AND** the next planned checkpoint is closer than returning to the start
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST recommend continuing to that checkpoint first
- **AND** it MUST ask the hiker to stop and reassess there before continuing
- **AND** it MUST still show returning to the start as a secondary option

#### Scenario: Route finish is the next reference

- **GIVEN** the hiker has reliable GPS progress near the end of the active route
- **AND** there are no remaining planned checkpoints before finish
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST use the finish as the next planned reference
- **AND** it MUST show the remaining distance to finish

#### Scenario: Route is already completed

- **GIVEN** TrailMate has marked the active route as completed
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST NOT ask the hiker to continue to the finish
- **AND** it MUST shift to route completion and return-trip wrap-up guidance

#### Scenario: Location is not reliable enough

- **GIVEN** location is waiting, missing, or low accuracy
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST NOT recommend a direction
- **AND** it MUST ask the hiker to refresh GPS before choosing an exit direction
- **AND** it MUST NOT present stale or low-confidence position as a safe exit basis

#### Scenario: Hiker is off the planned route

- **GIVEN** TrailMate has detected that the hiker is likely off the planned route
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST NOT recommend continuing to the next checkpoint as an exit choice
- **AND** it MUST ask the hiker to resolve the off-route state first
- **AND** it MUST keep the dedicated off-route recovery guidance as the primary recovery path

#### Scenario: No explicit exit or road data is available

- **GIVEN** TrailMate has only GPX route progress and planned checkpoints
- **WHEN** TrailMate presents route exit guidance
- **THEN** it MUST NOT claim to know the nearest road, exit, rescue point, or evacuation route
- **AND** it MUST keep the guidance scoped to route start, next checkpoint, or finish
