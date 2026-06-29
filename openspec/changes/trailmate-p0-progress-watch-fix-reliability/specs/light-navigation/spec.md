## ADDED Requirements

### Requirement: Progress Safety Watch Fix Reliability

TrailMate SHALL only present progress safety watch guidance when the route-progress fix is reliable enough for field safety comparison.

#### Scenario: Unreliable progress fix is not used for safety comparison

- **GIVEN** the hiker is recording the route
- **AND** the current route-progress fix is stale, future-dated, low-accuracy, negative, or non-finite
- **WHEN** TrailMate presents progress safety guidance
- **THEN** it MUST treat route progress evidence as missing
- **AND** it MUST NOT show a progress safety warning from that fix

#### Scenario: Fresh reliable progress fix keeps existing progress warning behavior

- **GIVEN** the hiker is recording the route
- **AND** the current route-progress fix is fresh, accurate, and internally valid
- **AND** actual progress is materially behind the planned checkpoint timeline
- **WHEN** TrailMate presents progress safety guidance
- **THEN** it MUST keep the existing caution and alert threshold behavior
