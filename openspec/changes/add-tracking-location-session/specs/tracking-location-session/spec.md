## ADDED Requirements

### Requirement: Foreground tracking starts real location subscription
The system SHALL start a real `TrailLocationProvider` subscription after the tracking foreground service accepts a permitted start action.

#### Scenario: Permitted start enters foreground before subscribing
- **WHEN** the tracking service receives the start action and foreground location permission is available
- **THEN** it starts foreground mode and then starts location updates through the provider

#### Scenario: Start without location permission does not subscribe
- **WHEN** the tracking service receives the start action without foreground location permission
- **THEN** it does not start location updates and stops itself

#### Scenario: Provider cannot start location updates
- **WHEN** foreground mode has started but the location provider immediately reports permission denied or disabled providers
- **THEN** the service releases the location subscription, stops foreground mode, and stops itself instead of continuing an active navigation notification

### Requirement: Tracking session state reflects only provider callbacks
The tracking location session SHALL expose in-memory state derived only from provider status callbacks and real provider location samples.

#### Scenario: Provider becomes ready
- **WHEN** the provider reports it is ready for location updates
- **THEN** the session records a listening state without fabricating a location sample

#### Scenario: Provider emits a location sample
- **WHEN** the provider emits a real `LocationSample`
- **THEN** the session stores it as the latest sample, increments the sample count, and marks the session located

#### Scenario: Provider reports unavailable status
- **WHEN** the provider reports permission denied, disabled providers, or invalid readings
- **THEN** the session records the corresponding status without creating fake track points

### Requirement: Tracking stop releases location subscription
The system MUST stop the active location subscription before shutting down foreground tracking.

#### Scenario: Stop action releases subscription
- **WHEN** the tracking service receives the stop action
- **THEN** it stops the active location subscription before stopping foreground mode and the service

#### Scenario: Duplicate stop is idempotent
- **WHEN** a tracking session is stopped more than once
- **THEN** the provider subscription is stopped at most once and the session remains idle

### Requirement: Location session excludes persistence and simulation
The tracking location session MUST NOT persist track records, upload location, synthesize GPS points, or mark route progress as matched.

#### Scenario: Session starts before first GPS fix
- **WHEN** the location provider has been subscribed but has not emitted a sample
- **THEN** the session has no latest sample, no track persistence side effect, and no simulated route progress
