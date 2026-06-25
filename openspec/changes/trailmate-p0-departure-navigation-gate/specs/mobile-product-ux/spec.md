## ADDED Requirements

### Requirement: Departure Gate Blocks Field Start Until Ready

TrailMate SHALL prevent field-start actions from appearing while required departure checks are incomplete.

#### Scenario: Offline base map is required and missing

- **GIVEN** a target route has usable geometry
- **AND** the offline route pack, GPS, and route-critical gear checks are ready
- **AND** the route assessment requires an offline base map
- **AND** the target offline base map is missing
- **WHEN** the route cockpit computes its primary action
- **THEN** the primary action SHALL open or import the offline base map
- **AND** it SHALL NOT be `开始徒步并记录轨迹`

#### Scenario: Offline base map coverage has not been verified without network

- **GIVEN** a target route has usable geometry
- **AND** the target offline base map appears to cover the route
- **AND** the offline base-map tiles have not been verified without network
- **WHEN** the route cockpit computes its primary action
- **THEN** the primary action SHALL guide the user to verify the offline base map
- **AND** it SHALL NOT start hiking or start recording

#### Scenario: All departure checks are complete

- **GIVEN** the route, offline route pack, offline base map, GPS, and route-critical gear checks are ready
- **WHEN** the route cockpit computes its primary action before departure
- **THEN** the primary action MAY be `开始徒步并记录轨迹`

### Requirement: Route Entry Copy Distinguishes Preview From Field Start

TrailMate SHALL label route entry points as route preview and departure check surfaces until the field-start gate passes.

#### Scenario: Imported route appears in the route workspace

- **GIVEN** a target route has been imported
- **WHEN** the route workspace shows the current route card
- **THEN** the primary route-card action SHALL mention route viewing or departure checks
- **AND** it SHALL NOT use vague copy such as `继续准备`

