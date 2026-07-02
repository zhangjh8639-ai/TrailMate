## ADDED Requirements

### Requirement: Foreground tracking requires real route context
The system SHALL only start local recording when the foreground tracking start request includes a selected route id and navigation session id created from user navigation intent.

#### Scenario: Start request carries route and session identity
- **WHEN** the user starts tracking from a selected route
- **THEN** the foreground service receives the route id, session id, start timestamp, and direction
- **AND** it does not create placeholder route or session identifiers internally

#### Scenario: Missing route context refuses recording
- **WHEN** the service receives a start action without route/session context
- **THEN** it does not start location updates
- **AND** it stops the foreground service path without creating a tracking session

### Requirement: Tracking sessions persist private metadata
The system SHALL persist active navigation recording session metadata locally with private visibility by default.

#### Scenario: Start recording session
- **WHEN** a navigation tracking session starts for a selected route
- **THEN** the local store creates or updates a tracking session row containing session id, route id, start timestamp, navigation state, direction, private visibility, and zero samples

#### Scenario: Active unfinished session can be recovered
- **WHEN** an unfinished tracking session exists in local storage
- **THEN** the local store can return that active session without requiring Compose UI state

### Requirement: Real location samples persist as ordered track points
The system SHALL append only real provider `LocationSample` updates as ordered track point rows for the active session.

#### Scenario: First provider sample is stored as point zero
- **WHEN** the provider emits the first real location sample for an active recording session
- **THEN** the store persists it as point index 0 with coordinate, optional elevation, GPS accuracy, recorded timestamp, optional speed, and optional bearing

#### Scenario: Multiple samples keep stable order
- **WHEN** the provider emits multiple real location samples for the same session
- **THEN** the store persists increasing point indexes and returns points ordered by index

#### Scenario: Resumed session appends after existing points
- **WHEN** a previously active session already has stored points
- **AND** recording resumes for the same session
- **THEN** the next real provider sample is appended after the highest existing point index
- **AND** existing points are not replaced

#### Scenario: No sample before first GPS fix
- **WHEN** tracking starts but the provider has not emitted a location sample
- **THEN** the store contains no track point for that session

#### Scenario: Provider startup failure does not create ended recording
- **WHEN** tracking is requested but the location provider reports disabled or permission denied before becoming ready
- **THEN** the local store does not create a fake ended recording session
- **AND** no track point is written

### Requirement: Recording stop marks session ended
The system SHALL mark a locally persisted tracking session ended when foreground tracking stops.

#### Scenario: Stop active recording
- **WHEN** foreground tracking stops for a persisted session
- **THEN** the store records an ended timestamp and no longer returns the session as active

#### Scenario: Service disposal does not end recoverable recording
- **WHEN** the Android service is destroyed without an explicit user stop
- **THEN** the provider subscription is released
- **AND** the session is not marked ended solely because of service disposal

### Requirement: Tracking database migrates imported-route version one data
The system SHALL preserve version 1 imported route databases while adding local tracking tables.

#### Scenario: Existing version one database starts tracking
- **GIVEN** a version 1 `trailmate.db` created before tracking tables existed
- **WHEN** the tracking store opens the database
- **THEN** it creates the tracking session and point tables without deleting imported route tables
- **AND** a real sample can be appended for a tracking session

### Requirement: Recording persistence excludes simulation and sharing
The local recording store MUST NOT synthesize GPS points, upload location, expose public visibility by default, or mark route progress as matched.

#### Scenario: Persisting a sample has no sharing side effects
- **WHEN** a real location sample is appended
- **THEN** the stored session remains private and no upload, live share, route progress simulation, or completed route record is created
