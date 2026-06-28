## ADDED Requirements

### Requirement: Manual Departure Brief Sharing

TrailMate SHALL provide a manual departure brief share for a selected route when a planned route duration is available.

#### Scenario: Route has not started and duration is available

- **GIVEN** the hiker has selected a route with distance, ascent, and planned duration
- **AND** the hiker has not started recording
- **WHEN** TrailMate presents the departure brief share
- **THEN** it MUST generate static itinerary text that includes route name, distance, ascent, planned start, expected finish, and a confirmation rule
- **AND** it MUST NOT require a GPS fix

#### Scenario: Route is actively recording

- **GIVEN** the hiker is recording the route
- **AND** TrailMate knows the recording start time and planned duration
- **WHEN** TrailMate presents the departure brief share
- **THEN** it MUST use the actual recording start time as the start time
- **AND** it MUST calculate expected finish from that original start time

#### Scenario: Planned duration is missing

- **GIVEN** TrailMate does not have a valid planned duration
- **WHEN** TrailMate presents the departure brief share
- **THEN** it MUST refuse to invent an expected finish time
- **AND** it MUST prompt the hiker to complete route assessment first

#### Scenario: Route recording or route session is finished

- **GIVEN** the route recording or route session is already finished
- **WHEN** TrailMate presents the departure brief share
- **THEN** it MUST stop presenting departure sharing as a primary action
- **AND** it MUST point the hiker to wrap-up or safety sharing instead

#### Scenario: Safety boundary remains honest

- **GIVEN** TrailMate generates departure brief share text
- **WHEN** a hiker shares it manually
- **THEN** the text MUST state that it is a static itinerary, not live tracking
- **AND** it MUST NOT imply TrailMate will automatically contact anyone, send alerts, or dispatch rescue
- **AND** the Android share chooser MUST label the action as departure briefing rather than current-location sharing
