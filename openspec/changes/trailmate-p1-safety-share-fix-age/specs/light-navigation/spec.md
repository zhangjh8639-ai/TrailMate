## ADDED Requirements

### Requirement: Safety Share Location Freshness

TrailMate SHALL only generate safety-share text when the shared location has a recent enough timestamp to avoid presenting an old cached position as current.

#### Scenario: Location timestamp is fresh

- **GIVEN** latitude, longitude, and horizontal accuracy are valid
- **AND** the location timestamp is within 2 minutes of the share action time
- **WHEN** TrailMate builds safety-share presentation
- **THEN** it MUST generate share text using the current coordinates
- **AND** existing route plan, expected finish, accuracy, and static-position disclosure copy MUST remain available

#### Scenario: Location timestamp is stale

- **GIVEN** latitude, longitude, and horizontal accuracy are valid
- **AND** the location timestamp is older than 2 minutes from the share action time
- **WHEN** TrailMate builds safety-share presentation
- **THEN** it MUST NOT generate share text
- **AND** it MUST ask the hiker to refresh GPS before sharing
- **AND** it MUST NOT present the stale position as a current or recording location

#### Scenario: Previously fresh location becomes stale before click

- **GIVEN** the route screen has already presented a safety-share action while the latest location timestamp was fresh
- **AND** the hiker leaves the screen open until that same timestamp is older than 2 minutes
- **WHEN** the hiker taps safety share
- **THEN** TrailMate MUST re-evaluate freshness using the click time
- **AND** it MUST NOT send the previously computed share text
- **AND** it MUST ask the hiker to refresh GPS before sharing

#### Scenario: Location timestamp is missing or invalid

- **GIVEN** latitude, longitude, and horizontal accuracy are valid
- **AND** the location timestamp is missing, zero, negative, or in the future beyond the share action time
- **WHEN** TrailMate builds safety-share presentation
- **THEN** it MUST NOT generate share text
- **AND** it MUST explain that TrailMate needs a fresh GPS fix before sharing
