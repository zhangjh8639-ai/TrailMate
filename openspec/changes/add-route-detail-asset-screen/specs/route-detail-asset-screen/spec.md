## ADDED Requirements

### Requirement: Route assets expose detail inspection
The system SHALL allow a route asset card to open a read-only route detail view.

#### Scenario: Open platform route detail
- **WHEN** the user opens detail for a bundled or platform route asset
- **THEN** the detail view shows the route name, region, source, offline status, distance, elevation gain, estimated duration, difficulty, confidence, and risk tags

### Requirement: Imported route detail preserves import boundaries
The system SHALL show imported GPX/KML route details as private, local, track-only, and unverified by default.

#### Scenario: Open imported route detail
- **WHEN** the user opens detail for a saved imported route asset
- **THEN** the detail view indicates the route is a local private import, only the track is available, confidence is待确认, and the import does not include a commercial or full offline map basemap

### Requirement: Route detail does not start fake navigation
The system MUST NOT expose a start-navigation side effect from route detail until active navigation is implemented.

#### Scenario: Detail screen remains read-only
- **WHEN** the route detail screen is shown in this change
- **THEN** the visible detail actions are limited to returning to the route asset center and inspecting route information

### Requirement: Route detail preserves route tab state
The system SHALL return from route detail to the Routes tab without clearing the active import state.

#### Scenario: Back from detail preserves import preview
- **WHEN** an import preview is visible and the user opens and closes a route detail
- **THEN** the original import preview and route asset list remain available

### Requirement: Route detail avoids deprecated surfaces
The system MUST NOT add planning, equipment, community, marketplace, or pretrip-check surfaces to the route detail view.

#### Scenario: Detail visible copy stays within route asset scope
- **WHEN** route detail visible text is inspected
- **THEN** it does not contain deprecated product surfaces such as 规划, 装备, 社区, 商城, or 出发前检查
