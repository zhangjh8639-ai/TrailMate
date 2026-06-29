## ADDED Requirements

### Requirement: Route Deviation Alert Fix Reliability

TrailMate SHALL NOT emit off-route, escalated off-route, silent off-route, or rejoined-route decisions from an unreliable `HikeLocationFix`.

#### Scenario: Stale off-route fix waits for reliable GPS

- **GIVEN** TrailMate is evaluating a route-deviation alert
- **AND** the location status says the user should check the route
- **AND** the fix is older than the field reliability window
- **WHEN** the policy evaluates the alert
- **THEN** TrailMate MUST wait for a reliable fix
- **AND** TrailMate MUST NOT notify or vibrate.

#### Scenario: Future rejoin fix preserves active deviation episode

- **GIVEN** TrailMate has an active deviation episode
- **AND** the location status says the user is back on route
- **AND** the fix timestamp is later than the current evaluation time
- **WHEN** the policy evaluates the alert
- **THEN** TrailMate MUST wait for a reliable fix
- **AND** TrailMate MUST preserve the active deviation episode until a reliable rejoin fix arrives.
