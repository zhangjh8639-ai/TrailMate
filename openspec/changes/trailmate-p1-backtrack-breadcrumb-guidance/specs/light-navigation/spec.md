## ADDED Requirements

### Requirement: Backtrack Breadcrumb Guidance

TrailMate SHALL tell the hiker whether the recorded walked track is currently usable as a conservative backtracking reference.

#### Scenario: Fresh breadcrumb is usable

- **GIVEN** track recording is active
- **AND** at least two recorded track points exist
- **AND** the latest recorded track point is fresh
- **AND** the recorded distance is meaningful
- **WHEN** TrailMate presents Route tab safety guidance
- **THEN** it MUST show that the walked track can be used as a backtracking reference
- **AND** it MUST show recorded distance and point count
- **AND** it MUST ask the hiker to retrace the visible walked path instead of taking shortcuts

#### Scenario: Breadcrumb is still warming up

- **GIVEN** track recording is active
- **AND** fewer than two recorded track points exist or recorded distance is not meaningful
- **WHEN** TrailMate presents Route tab safety guidance
- **THEN** it MUST say that the walked track is still forming
- **AND** it MUST NOT present the breadcrumb as reliable return evidence

#### Scenario: Latest breadcrumb is stale

- **GIVEN** track recording is active
- **AND** at least two recorded track points exist
- **AND** the latest recorded track point is stale
- **WHEN** TrailMate presents Route tab safety guidance
- **THEN** it MUST warn that the backtracking reference may be out of date
- **AND** it MUST ask the hiker to refresh positioning and verify the offline map, trail markers, and visible path

#### Scenario: Recording is paused

- **GIVEN** track recording is paused
- **AND** at least two recorded track points exist
- **WHEN** TrailMate presents Route tab safety guidance
- **THEN** it MUST state that the breadcrumb only covers the recorded section
- **AND** it MUST avoid implying that movement after pausing has been captured

#### Scenario: No breadcrumb exists

- **GIVEN** track recording is idle or no recorded points exist
- **WHEN** TrailMate presents Route tab safety guidance
- **THEN** it MUST state that no walked-track return evidence is available
- **AND** it MUST ask the hiker to rely on the saved route, offline map, trail markers, and visible path

#### Scenario: Safety boundary remains advisory

- **GIVEN** TrailMate shows backtrack breadcrumb guidance
- **WHEN** the hiker reads or acts on it
- **THEN** TrailMate MUST NOT imply turn-by-turn navigation
- **AND** it MUST NOT imply nearest-road guidance, automatic rescue, automatic contact, guaranteed safety, or medical diagnosis
