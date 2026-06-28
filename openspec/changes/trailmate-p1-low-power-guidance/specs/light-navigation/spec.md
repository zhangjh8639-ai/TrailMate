## ADDED Requirements

### Requirement: Low Power Hiking Guidance

TrailMate SHALL provide manual low-power guidance when the route screen detects low or critical phone battery.

#### Scenario: Battery is normal or unknown

- **GIVEN** phone battery is normal or unknown
- **WHEN** TrailMate presents route safety guidance
- **THEN** it MUST NOT add a low-power warning card

#### Scenario: Battery is low

- **GIVEN** phone battery is low but not critical
- **WHEN** TrailMate presents low-power guidance
- **THEN** it MUST recommend reducing screen use, confirming the return plan, preparing a power bank, and keeping offline route context available
- **AND** it MUST avoid telling the hiker to disable GPS tracking

#### Scenario: Battery is critical

- **GIVEN** phone battery is critical
- **WHEN** TrailMate presents low-power guidance
- **THEN** it MUST prioritize shortening or exiting the route
- **AND** it MUST recommend sharing or noting a final reliable position before the phone becomes unusable
- **AND** it MUST recommend connecting a power bank if available

#### Scenario: Safety boundary remains honest

- **GIVEN** TrailMate presents low-power guidance
- **WHEN** the hiker reads the guidance
- **THEN** it MUST state that TrailMate cannot extend battery life or guarantee navigation
- **AND** it MUST NOT imply TrailMate can automatically toggle system power settings, contact anyone, or dispatch rescue
