## ADDED Requirements

### Requirement: Departure Brief Share Refresh Action

TrailMate SHALL recompute departure-brief share text when the hiker taps the send action, instead of relying on stale text captured when the panel was rendered.

#### Scenario: Departure brief is recomputed at click time

- **GIVEN** the route helper shows a sendable departure brief
- **AND** time has passed since the panel was rendered
- **WHEN** the hiker taps the departure-brief send action
- **THEN** TrailMate MUST regenerate the departure brief using the current click time
- **AND** the shared expected finish time MUST be based on that current departure time unless recording has already started

#### Scenario: Departure brief action does not share unavailable text

- **GIVEN** the departure brief can no longer generate reliable share text
- **WHEN** the hiker taps the departure-brief action
- **THEN** TrailMate MUST NOT share the stale previously rendered text
