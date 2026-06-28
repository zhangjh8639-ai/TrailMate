## ADDED Requirements

### Requirement: Return ETA Watch

TrailMate SHALL present a local return-ETA watch for an active route recording when a planned route duration and recording start time are available.

#### Scenario: Route has not started

- **GIVEN** the hiker has not started recording the route
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST state that return timing begins after start
- **AND** it MUST NOT present an overdue warning

#### Scenario: Active hike is before planned finish

- **GIVEN** the hiker is recording a route
- **AND** the planned finish time is still in the future
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST show the planned return time
- **AND** it MUST show the remaining planned time
- **AND** it MUST keep the tone non-alarming

#### Scenario: Active hike has passed planned finish

- **GIVEN** the hiker is recording a route
- **AND** the current time is after the planned finish
- **AND** the current time is still within the configured confirmation grace window
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST warn that the hiker should reassess pace and conditions
- **AND** it MUST show how long remains before the confirmation window is exceeded

#### Scenario: Active hike exceeds confirmation window

- **GIVEN** the hiker is recording a route
- **AND** the current time is after planned finish plus the confirmation grace window
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST escalate to overdue guidance
- **AND** it MUST make fresh safety sharing the primary action
- **AND** it MUST NOT send any message automatically

#### Scenario: Overdue safety share preserves the original planned return time

- **GIVEN** the hiker is recording a route
- **AND** the current time is after the original planned finish time
- **WHEN** the hiker manually shares their current location from return-ETA overdue guidance
- **THEN** TrailMate MUST include the original planned finish time when that time is known
- **AND** it MUST NOT recalculate a new future finish time from the share click time
- **AND** it MUST indicate that the route is already past the planned finish time

#### Scenario: Planned duration is missing

- **GIVEN** TrailMate does not have a valid planned route duration
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST state that it cannot calculate a return ETA
- **AND** it MUST NOT invent a planned return time

#### Scenario: Route recording is finished

- **GIVEN** the route recording is finished
- **WHEN** TrailMate presents the return ETA watch
- **THEN** it MUST shift to wrap-up guidance
- **AND** it MUST NOT continue warning about overdue return
