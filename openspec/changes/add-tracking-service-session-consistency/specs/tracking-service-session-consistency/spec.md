## ADDED Requirements

### Requirement: Track foreground service runtime session
The system SHALL maintain local in-process runtime state for the currently running foreground tracking service session while the service is active.

#### Scenario: Foreground service starts
- **WHEN** the tracking foreground service starts location updates for a valid tracking request
- **THEN** runtime state contains that session id and route id

#### Scenario: Foreground service stops
- **WHEN** the tracking foreground service stops or is destroyed
- **THEN** runtime state no longer contains a running session

### Requirement: Prefer running service state over recovered local record
The Navigation tab SHALL show running foreground tracking copy instead of stale recovery copy when runtime state says a foreground tracking service is active.

#### Scenario: Runtime service is active and local session is unfinished
- **WHEN** the Navigation tab loads an unfinished local tracking session and runtime state contains the same running session
- **THEN** the Navigation tab shows foreground tracking service running copy and does not show the generic recovered-local-record copy

#### Scenario: Runtime service is inactive and local session is unfinished
- **WHEN** the Navigation tab loads an unfinished local tracking session and runtime state has no running session
- **THEN** the Navigation tab shows the recovered-local-record copy

### Requirement: Stop runtime service when ending local record
The system SHALL stop foreground tracking before ending a recovered or running local tracking record from Navigation UI.

#### Scenario: User ends recovered record while service may be active
- **WHEN** the user chooses to end the local tracking record from Navigation UI
- **THEN** the system requests foreground service stop and marks the local session ended

### Requirement: Avoid unsupported recovery claims
The Navigation tab SHALL NOT claim crash recovery, rescue, public sharing, or live GPS fix freshness from runtime state alone.

#### Scenario: Running foreground service copy is visible
- **WHEN** the Navigation tab shows running foreground tracking service state
- **THEN** the copy states that the foreground service is running and does not claim automatic rescue, successful crash recovery, public sharing, or a fresh GPS fix
