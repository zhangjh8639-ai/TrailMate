## ADDED Requirements

### Requirement: Recover latest unfinished local tracking session
The system SHALL detect the latest unfinished local tracking session during app startup and expose it to the Navigation tab as a recovery state.

#### Scenario: Unfinished session exists
- **WHEN** the app starts and `TrackingRecordingStore` returns an unfinished local tracking session
- **THEN** the Navigation tab shows a recovery state for that session

#### Scenario: No unfinished session exists
- **WHEN** the app starts and `TrackingRecordingStore` has no unfinished local tracking session
- **THEN** the Navigation tab does not show a recovery state

### Requirement: Recovery card uses safe local-only copy
The recovery state SHALL use Chinese copy that clearly describes an unfinished local record and SHALL NOT imply live GPS, automatic rescue, public sharing, or successful foreground-service restoration.

#### Scenario: Recovery state is visible
- **WHEN** the Navigation tab renders a recovered local tracking session
- **THEN** it displays copy equivalent to "发现未结束的本地记录", "本机私密", and an explicit end action

#### Scenario: Recovery state is not live tracking
- **WHEN** the Navigation tab renders a recovered local tracking session
- **THEN** it does not display copy claiming that live GPS tracking, automatic rescue, public sharing, or foreground-service recovery has already succeeded

### Requirement: User explicitly ends recovered session
The system SHALL let the user explicitly end a recovered local tracking session by marking that session ended in local storage.

#### Scenario: End recovered session
- **WHEN** the user chooses to end the recovered local tracking session
- **THEN** the system marks the session ended in `TrackingRecordingStore` and removes the recovery card from the Navigation tab

### Requirement: Active tracking takes precedence over recovery copy
The system SHALL avoid showing stale recovery copy when the current in-memory tracking state is actively recording.

#### Scenario: Active tracking is visible
- **WHEN** the normal tracking state is active
- **THEN** the Navigation tab does not show duplicate recovered-session copy for the same local record
