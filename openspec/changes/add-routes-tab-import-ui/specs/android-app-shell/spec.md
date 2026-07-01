## ADDED Requirements

### Requirement: Route tab dispatches to route feature screen
The Android app shell SHALL dispatch the fixed `路线` bottom navigation tab to the route asset center screen instead of placeholder copy.

#### Scenario: Route tab uses feature screen
- **WHEN** the user selects the `路线` bottom navigation item
- **THEN** the shell displays the route feature screen content for route assets and import preview

#### Scenario: Other shell tabs remain placeholder-ready
- **WHEN** the user selects `发现`, `导航`, `记录`, or `我的`
- **THEN** the shell continues to display their current scoped placeholder content until those feature slices are implemented
