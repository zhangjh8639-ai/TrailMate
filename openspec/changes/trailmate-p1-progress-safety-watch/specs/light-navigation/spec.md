## ADDED Requirements

### Requirement: Progress Safety Watch

TrailMate SHALL warn hikers when route progress is materially behind the planned checkpoint timeline and there is enough evidence to make the comparison.

#### Scenario: Missing route progress evidence

- **GIVEN** TrailMate does not have an active route recording start time or current route progress
- **WHEN** the route tab presents safety guidance
- **THEN** it MUST NOT show a progress safety warning

#### Scenario: Progress remains close enough to plan

- **GIVEN** the hiker is recording the route
- **AND** actual route progress is close enough to the planned checkpoint timeline
- **WHEN** TrailMate presents progress safety guidance
- **THEN** it MUST NOT show a warning card

#### Scenario: Progress is materially behind plan

- **GIVEN** the hiker is recording the route
- **AND** elapsed time is at least 60 minutes
- **AND** actual progress is at least 1 km and 25 percent behind planned route progress
- **WHEN** TrailMate presents progress safety guidance
- **THEN** it MUST show a caution card
- **AND** it MUST recommend stopping to review body condition, water/food, weather, and return plan
- **AND** it MUST warn against rushing or leaving the visible path to catch up

#### Scenario: Progress pressure is severe

- **GIVEN** the hiker is recording the route
- **AND** elapsed time is at least 90 minutes
- **AND** actual progress is at least 2 km and 40 percent behind planned route progress
- **AND** more than 3 km remains
- **WHEN** TrailMate presents progress safety guidance
- **THEN** it MUST show an alert card
- **AND** it MUST recommend shortening or exiting the route
- **AND** it MUST offer manual location sharing

#### Scenario: Safety boundary remains honest

- **GIVEN** TrailMate presents progress safety guidance
- **WHEN** the hiker reads the guidance
- **THEN** it MUST NOT diagnose fatigue or medical condition
- **AND** it MUST NOT imply TrailMate can automatically contact anyone, dispatch rescue, or guarantee safety
