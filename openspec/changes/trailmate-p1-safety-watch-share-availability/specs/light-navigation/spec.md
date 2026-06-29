## ADDED Requirements

### Requirement: Safety Watch Share Availability

TrailMate SHALL label safety-watch share buttons according to whether current safety-share text can be produced, and SHALL dispatch those buttons by explicit action kind.

#### Scenario: Field safety watch shares only when share text is available

- **GIVEN** a progress or daylight safety watch requires safety sharing
- **AND** current safety-share text is available
- **WHEN** TrailMate presents the safety-watch button
- **THEN** TrailMate MUST show the watch's share label
- **AND** the button MUST dispatch a share-location action

#### Scenario: Field safety watch requests location when share text is unavailable

- **GIVEN** a progress or daylight safety watch requires safety sharing
- **AND** current safety-share text is unavailable
- **WHEN** TrailMate presents the safety-watch button
- **THEN** TrailMate MUST show the current safety-share repair label
- **AND** the button MUST request location instead of attempting to share

#### Scenario: Return ETA watch requests location when share text is unavailable

- **GIVEN** the return-ETA watch requires safety sharing
- **AND** current safety-share text is unavailable
- **WHEN** TrailMate presents the return-ETA button
- **THEN** TrailMate MUST show the current safety-share repair label
- **AND** the button MUST request location instead of attempting to share
