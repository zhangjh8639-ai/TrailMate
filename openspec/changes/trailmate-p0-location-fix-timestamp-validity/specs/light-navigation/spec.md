## ADDED Requirements

### Requirement: Location Fix Timestamp Validity

TrailMate SHALL NOT treat future or non-positive location fix timestamps as reliable field evidence.

#### Scenario: Future location fix does not unlock field use

- **GIVEN** a located fix has acceptable accuracy
- **AND** its timestamp is later than the current evaluation time
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** the fix MUST NOT be considered fresh
- **AND** the fix MUST NOT unlock departure, track recording, or precise map confidence

#### Scenario: Non-positive location fix timestamp does not unlock field use

- **GIVEN** a located fix has acceptable accuracy
- **AND** its timestamp is zero or negative
- **WHEN** TrailMate evaluates field-use reliability
- **THEN** the fix MUST NOT be considered fresh
- **AND** its age MUST be outside the reliable fix window

#### Scenario: Android provider timestamps remain auditable

- **GIVEN** Android location providers emit a zero, negative, or future timestamp
- **WHEN** TrailMate converts provider fixes into app snapshots or recorded track points
- **THEN** TrailMate MUST preserve the provider timestamp for reliability validation
- **AND** MUST NOT replace it with the current device time

#### Scenario: Invalid located fix timestamp is visible but not trusted

- **GIVEN** a located fix has coordinates and acceptable accuracy
- **AND** its timestamp is zero, negative, or later than the current evaluation time
- **WHEN** TrailMate presents location reliability, GPS watch, or map user-location confidence
- **THEN** the fix MUST be presented as requiring recalibration
- **AND** TrailMate MUST NOT label the fix as recently updated
- **AND** TrailMate MUST NOT present the map user-location marker as precise

#### Scenario: Invalid recorded point timestamp does not poison the track

- **GIVEN** track recording is active
- **AND** Android provides a recorded point whose timestamp is zero, negative, or later than the current evaluation time
- **WHEN** TrailMate appends the point to the recorded track
- **THEN** the point MUST be ignored
- **AND** later valid points MUST still be accepted

#### Scenario: Invalid fix timestamp does not advance route progress

- **GIVEN** a hike session is active
- **AND** a projected route fix has acceptable distance, cross-track error, and accuracy
- **AND** its timestamp is zero, negative, or later than the current evaluation time
- **WHEN** TrailMate applies the fix to route progress
- **THEN** TrailMate MUST NOT advance reached checkpoints
- **AND** TrailMate MUST present the fix as a location-time anomaly

#### Scenario: Invalid breadcrumb timestamp does not make backtrack ready

- **GIVEN** a recorded breadcrumb trail contains enough distance and points for backtrack reference
- **AND** the latest point timestamp is zero, negative, or later than the current evaluation time
- **WHEN** TrailMate presents backtrack breadcrumb guidance
- **THEN** TrailMate MUST NOT present backtrack reference as ready
- **AND** TrailMate MUST NOT label the latest breadcrumb as recently updated
