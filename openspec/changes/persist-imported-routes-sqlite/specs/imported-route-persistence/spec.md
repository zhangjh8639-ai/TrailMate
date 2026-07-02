## ADDED Requirements

### Requirement: Parsed imported routes persist locally
The system SHALL persist a successfully parsed GPX/KML route when the user saves it from the route import preview.

#### Scenario: Save parsed import
- **WHEN** a GPX/KML import has parsed route geometry and the user saves it
- **THEN** the imported route is written to local SQLite with route metadata and ordered geometry points

### Requirement: Failed imports are not persisted
The system MUST NOT persist unsupported, invalid, or geometry-less GPX/KML imports.

#### Scenario: Save unavailable for failed import
- **WHEN** an import result has no parsed route geometry
- **THEN** no saveable import record is produced and no SQLite write is attempted

### Requirement: Imported route safety boundaries persist
The system SHALL persist imported routes as private, track-only, and unverified by default.

#### Scenario: Persist import defaults
- **WHEN** a parsed imported route is converted into a persistent record
- **THEN** its visibility is Private, offline status is TrackOnly, and confidence is Unverified

### Requirement: Persisted imports restore on startup
The system SHALL load persisted imported routes when the Android app starts and show them in the route asset list.

#### Scenario: Restart restores saved import
- **WHEN** the app starts after an imported route was previously saved
- **THEN** the route tab shows the persisted imported route before bundled or sample routes

### Requirement: Duplicate saves are idempotent
The system SHALL upsert repeated saves of the same imported route identity instead of creating duplicate route assets.

#### Scenario: Repeated save replaces existing import
- **WHEN** the same parsed import identity is saved more than once
- **THEN** SQLite contains one imported route record for that identity and the route tab shows one imported route card

### Requirement: Imported route persistence does not imply map basemap
The system SHALL keep persisted imported routes labeled as track-only and not claim that a commercial or full offline map basemap has been saved.

#### Scenario: Restored import copy stays bounded
- **WHEN** a persisted imported route is rendered in the route tab
- **THEN** the card indicates only track data is available and does not expose route detail or start-navigation actions in this slice
