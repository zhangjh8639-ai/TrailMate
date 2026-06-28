## ADDED Requirements

### Requirement: Departure Safety Start Gate

TrailMate SHALL route the main Route cockpit action to required departure repair steps before allowing a hike to start.

#### Scenario: Required offline base map is missing

- **GIVEN** the route is still ready to start
- **AND** the departure readiness summary requires an offline base map action
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** the primary action MUST be the offline base map repair action
- **AND** it MUST NOT be `START_HIKE`

#### Scenario: Required target region is not downloaded

- **GIVEN** the route is still ready to start
- **AND** the target offline base map region is missing
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** the primary action MUST guide the hiker to the offline base map repair action
- **AND** the readiness strip MUST still show the target-region issue

#### Scenario: Offline base map needs no-network verification

- **GIVEN** the route is still ready to start
- **AND** the target offline base map covers the route but has not been verified without network
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** the primary action MUST guide the hiker to verify the base map
- **AND** it MUST NOT start the hike

#### Scenario: Missing critical gear blocks start

- **GIVEN** the route is still ready to start
- **AND** departure readiness reports missing critical gear
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** the primary action MUST guide the hiker to the gear repair action
- **AND** it MUST NOT start the hike

#### Scenario: Optional offline base map remains advisory

- **GIVEN** the route is still ready to start
- **AND** the offline base map is only recommended rather than required
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** TrailMate MAY allow the start action
- **AND** the readiness strip MUST keep the optional base map gap visible

#### Scenario: Active recording controls remain available

- **GIVEN** recording is already active or paused
- **WHEN** TrailMate builds the Route cockpit primary action
- **THEN** the primary action MUST keep the recording control available
- **AND** it MUST NOT replace pause or resume with departure repair actions
