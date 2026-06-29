## ADDED Requirements

### Requirement: Off-route Recovery Safe-exit Option

TrailMate SHALL expose a safe-exit review option in reliable off-route recovery guidance.

#### Scenario: Reliable off-route fix exposes safe-exit review

- **GIVEN** TrailMate has a reliable off-route fix
- **AND** route guidance says the hiker should check the route
- **WHEN** recovery guidance is presented
- **THEN** the recovery actions MUST include an option to review safe-exit guidance
- **AND** the option MUST frame exit choice as a decision review, not turn-by-turn rescue routing.

#### Scenario: Low-accuracy recovery waits for GPS before exit choice

- **GIVEN** TrailMate only has a low-accuracy fix
- **WHEN** recovery guidance is presented
- **THEN** the recovery actions MUST focus on waiting for reliable GPS
- **AND** the recovery actions MUST NOT present a safe-exit choice as ready.
