# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Off-route recovery shall present conservative field actions

TrailMate SHALL present a reliable off-route state as a concise set of conservative recovery actions grounded only in local route and GPS evidence.

#### Scenario: Reliable off-route fix shows action order

- **GIVEN** a hiker is actively following an imported GPX route
- **AND** TrailMate has a reliable GPS fix projected more than the deviation threshold from the route
- **WHEN** TrailMate displays recovery guidance
- **THEN** it SHALL show stopping and confirming as the first action
- **AND** it SHALL show returning to the nearest planned route segment as a conservative action
- **AND** it SHALL offer safety sharing only when a usable location share is available
- **AND** it SHALL include the approximate deviation distance

#### Scenario: Low-accuracy fix avoids precise recovery claims

- **GIVEN** the current GPS fix is not accurate enough for field recovery
- **WHEN** TrailMate displays recovery guidance
- **THEN** it SHALL ask the user to wait for stable positioning
- **AND** it SHALL NOT claim a precise off-route distance
- **AND** it SHALL NOT claim a nearest exit, road, or reroute

#### Scenario: Rejoined route shifts back to navigation

- **GIVEN** TrailMate recently showed an off-route episode
- **WHEN** a reliable fix returns near the planned route
- **THEN** TrailMate SHALL show that the user has rejoined the route
- **AND** it SHALL make continuing navigation the primary recovery action
